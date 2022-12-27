package com.hyun.betterspawner.utils;

import com.hyun.betterspawner.utils.nbt.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A class to extract and provide wrappers for various NBT data.
 *
 * The code, relying heavily on NMS, uses reflection, which
 * should make it somewhat version-independent. Wrappers are also
 * provided for all types of NBT so users don't have to deal with
 * any NMS.
 *
 * NOTE: This requires ReflectUtil.java
 *
 * @author AlvinB
 */
public class NBTUtil {

    public static final Class<?> TAG_COMPOUND_CLASS = ReflectUtil.getNMSClass("NBTTagCompound", "net.minecraft.nbt").getOrThrow();

    private static final Method AS_NMS_COPY = ReflectUtil.getMethod(ReflectUtil.getCBClass("inventory.CraftItemStack").getOrThrow(), "asNMSCopy", ItemStack.class).getOrThrow();
    private static final Field ITEM_STACK_TAG = ReflectUtil.getDeclaredFieldByType(ReflectUtil.getNMSClass("ItemStack", "net.minecraft.world.item").getOrThrow(), TAG_COMPOUND_CLASS, 0, true).getOrThrow();
    private static final Method AS_BUKKIT_COPY = ReflectUtil.getMethod(ReflectUtil.getCBClass("inventory.CraftItemStack").getOrThrow(), "asBukkitCopy", ReflectUtil.getNMSClass("ItemStack", "net.minecraft.world.item").getOrThrow()).getOrThrow();

    private static final Method ENTITY_GET_HANDLE = ReflectUtil.getMethod(ReflectUtil.getCBClass("entity.CraftEntity").getOrThrow(), "getHandle").getOrThrow();
    private static final Method ENTITY_SAVE_TO_NBT = ReflectUtil.getMethodByTypeAndParams(ReflectUtil.getNMSClass("Entity", "net.minecraft.world.entity").getOrThrow(), TAG_COMPOUND_CLASS, 0, TAG_COMPOUND_CLASS).getOrThrow();
    private static final Method ENTITY_LOAD_FROM_NBT = ReflectUtil.getMethodByPredicate(ReflectUtil.getNMSClass("Entity", "net.minecraft.world.entity").getOrThrow(), new ReflectUtil.MethodPredicate()
            .withParams(TAG_COMPOUND_CLASS).withoutModifiers(Modifier.ABSTRACT).withReturnType(void.class), 0).getOrThrow();

    private static final Class<?> TILE_ENTITY_CLASS = ReflectUtil.getNMSClass("TileEntity", "net.minecraft.world.level.block.entity").getOrThrow();
    private static final Method GET_TILE_ENTITY;
    private static final Method TILE_ENTITY_SAVE_TO_NBT = ReflectUtil.getMethodByTypeAndParams(TILE_ENTITY_CLASS, TAG_COMPOUND_CLASS, 0, TAG_COMPOUND_CLASS).getOrThrow();
    private static final Method TILE_ENTITY_LOAD_FROM_NBT = ReflectUtil.getMethodByTypeAndParams(TILE_ENTITY_CLASS, void.class, 0, TAG_COMPOUND_CLASS).getOrThrow();
    private static final Class<?> CRAFT_BLOCK_ENTITY_STATE_CLASS;

    static {
        if (ReflectUtil.isVersionHigherOrEqual(1, 12, 1)) {
            CRAFT_BLOCK_ENTITY_STATE_CLASS = ReflectUtil.getCBClass("block.CraftBlockEntityState").getOrThrow();
            GET_TILE_ENTITY = ReflectUtil.getDeclaredMethodByPredicate(CRAFT_BLOCK_ENTITY_STATE_CLASS, new ReflectUtil.MethodPredicate()
                    .withReturnType(TILE_ENTITY_CLASS).withName("getTileEntity"), 0, true).getOrThrow();
        } else {
            CRAFT_BLOCK_ENTITY_STATE_CLASS = null;
            GET_TILE_ENTITY = ReflectUtil.getMethodByType(ReflectUtil.getCBClass("block.CraftBlockState").getOrThrow(), TILE_ENTITY_CLASS, 0).getOrThrow();
        }
    }

    /**
     * Loads a compressed NBTTagCompound from the specified stream.
     *
     * The stream must start with a compound, and it should be compressed
     * using GZIP compression.
     *
     * NOTE: If an exception occurs doing the reading of the NBT data, it will be
     * rethrown as a RuntimeException.
     *
     * @param inputStream the uncompressed InputStream
     * @return the read NBTTagCompound
     */
    public static NBTTagCompound readCompressedNBTFromStream(InputStream inputStream) {
        try {
            return readUncompressedNBTFromStream(new GZIPInputStream(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads an NBTTagCompound from the specified stream.
     *
     * The stream must start with a compound, and it should be entirely
     * uncompressed. If the data is compressed, use readCompressedNBTFromStream().
     *
     * NOTE: If an exception occurs doing the reading of the NBT data, it will be
     * rethrown as a RuntimeException.
     *
     * @param inputStream the InputStream
     * @return the read NBTTagCompound
     */
    public static NBTTagCompound readUncompressedNBTFromStream(InputStream inputStream) {
        DataInputStream dataInputStream = (inputStream instanceof DataInputStream ? (DataInputStream) inputStream : new DataInputStream(inputStream));
        return (NBTTagCompound) readTag(dataInputStream, null);
    }

    private static NBTTagBase readTag(DataInputStream inputStream, NBTTagCompound compound) {
        try {
            int tagId = inputStream.readUnsignedByte();
            String name = null;
            // End Tags don't have a name
            if (tagId != 0) {
                name = inputStream.readUTF();
            }
            return readTagValue(inputStream, tagId, name, compound);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static NBTTagBase readTagValue(DataInputStream inputStream, int tagId, String name, NBTTagCompound compound) {
        try {
            NBTTagBase tag;
            switch (tagId) {
                case 0:
                    return new NBTTagEnd();
                case 1:
                    tag = new NBTTagByte(inputStream.readByte());
                    break;
                case 2:
                    tag = new NBTTagShort(inputStream.readShort());
                    break;
                case 3:
                    tag = new NBTTagInt(inputStream.readInt());
                    break;
                case 4:
                    tag = new NBTTagLong(inputStream.readLong());
                    break;
                case 5:
                    tag = new NBTTagFloat(inputStream.readFloat());
                    break;
                case 6:
                    tag = new NBTTagDouble(inputStream.readDouble());
                    break;
                case 7:
                    int length = inputStream.readInt();
                    byte[] value = new byte[length];
                    inputStream.readFully(value);
                    tag = new NBTTagByteArray(value);
                    break;
                case 8:
                    tag = new NBTTagString(inputStream.readUTF());
                    break;
                case 9:
                    int type = inputStream.readUnsignedByte();
                    int listLength = inputStream.readInt();
                    NBTTagList<NBTTagBase> listTag = new NBTTagList<>();
                    for (int i = 0; i < listLength; i++) {
                        listTag.add(readTagValue(inputStream, type, null, null));
                    }
                    tag = listTag;
                    break;
                case 10:
                    NBTTagCompound compoundTag = new NBTTagCompound();
                    NBTTagBase childTag = readTag(inputStream, compoundTag);
                    while (childTag != null && childTag.getId() != 0) {
                        childTag = readTag(inputStream, compoundTag);
                    }
                    tag = compoundTag;
                    break;
                case 11:
                    int arrayLength = inputStream.readInt();
                    int[] array = new int[arrayLength];
                    for (int i = 0; i < arrayLength; i++) {
                        array[i] = inputStream.readInt();
                    }
                    tag = new NBTTagIntArray(array);
                    break;
                default:
                    throw new RuntimeException("NBT tag id " + tagId + " is unknown!");
            }
            if (compound != null) {
                compound.set(name, tag);
            }
            return tag;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a compressed NBTTagCompound to the specified stream.
     *
     * NOTE: If an exception occurs doing the writing of the NBT data, it will be
     * rethrown as a RuntimeException.
     *
     * @param outputStream the uncompressed OutputStream
     */
    public static void writeCompressedNBTToStream(OutputStream outputStream, NBTTagCompound compound) {
        try {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            writeUncompressedNBTToStream(gzipOutputStream, compound);
            gzipOutputStream.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes an NBTTagCompound to the specified stream.
     *
     * NOTE: If an exception occurs doing the reading of the NBT data, it will be
     * rethrown as a RuntimeException.
     *
     * @param outputStream the OutputStream
     */
    public static void writeUncompressedNBTToStream(OutputStream outputStream, NBTTagCompound compound) {
        DataOutputStream dataOutputStream = (outputStream instanceof DataOutputStream ? (DataOutputStream) outputStream : new DataOutputStream(outputStream));
        writeTag(dataOutputStream, compound, "");
    }

    private static void writeTag(DataOutputStream outputStream, NBTTagBase tag, String name) {
        try {
            outputStream.writeByte(tag.getId());
            // End tags don't have a name
            if (tag.getId() != 0) {
                outputStream.writeUTF(name);
            }
            writeTagValue(outputStream, tag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeTagValue(DataOutputStream outputStream, NBTTagBase tag) {
        try {
            switch (tag.getId()) {
                case 0:
                    // End tags don't have any data
                    break;
                case 1:
                    outputStream.writeByte(((NBTTagByte) tag).getValue());
                    break;
                case 2:
                    outputStream.writeShort(((NBTTagShort) tag).getValue());
                    break;
                case 3:
                    outputStream.writeInt(((NBTTagInt) tag).getValue());
                    break;
                case 4:
                    outputStream.writeLong(((NBTTagLong) tag).getValue());
                    break;
                case 5:
                    outputStream.writeFloat(((NBTTagFloat) tag).getValue());
                    break;
                case 6:
                    outputStream.writeDouble(((NBTTagDouble) tag).getValue());
                    break;
                case 7:
                    byte[] value = ((NBTTagByteArray) tag).getValue();
                    outputStream.writeInt(value.length);
                    outputStream.write(value);
                    break;
                case 8:
                    outputStream.writeUTF(((NBTTagString) tag).getValue());
                    break;
                case 9:
                    @SuppressWarnings("unchecked") NBTTagList<NBTTagBase> listTag = (NBTTagList<NBTTagBase>) tag;
                    outputStream.write(listTag.getType() & 0xFF);
                    outputStream.writeInt(listTag.size());
                    for (NBTTagBase baseTag : listTag.getContents()) {
                        writeTagValue(outputStream, baseTag);
                    }
                    break;
                case 10:
                    NBTTagCompound compound = (NBTTagCompound) tag;
                    for (Map.Entry<String, NBTTagBase> entry : compound.getContents().entrySet()) {
                        writeTag(outputStream, entry.getValue(), entry.getKey());
                    }
                    writeTag(outputStream, new NBTTagEnd(), null);
                    break;
                case 11:
                    int[] array = ((NBTTagIntArray) tag).getValue();
                    outputStream.writeInt(array.length);
                    for (int i : array) {
                        outputStream.writeInt(i);
                    }
                    break;
                default:
                    throw new RuntimeException("tag id " + tag.getId() + " is unknown");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the NBT data from an ItemStack
     *
     * @param itemStack the ItemStack
     * @return an NBTTagCompound containing the NBT data.
     */
    public static NBTTagCompound getItemNBT(ItemStack itemStack) {
        Object handle = ReflectUtil.invokeMethod(null, AS_NMS_COPY, itemStack).getOrThrow();
        Object tagCompound = ReflectUtil.getFieldValue(handle, ITEM_STACK_TAG).getOrThrow();
        if (tagCompound == null) {
            return new NBTTagCompound();
        }
        return NBTTagCompound.fromHandle(tagCompound);
    }

    /**
     * Sets the NBT data to an ItemStack
     *
     * This may or may not set the NBT to the
     * ItemStack directly, depending on how the
     * ItemStack was created, which is why an
     * ItemStack is returned. It is recommended
     * that you only use the returned ItemStack,
     * as the ItemStack passed as the argument
     * might not have been affected.
     *
     * @param itemStack the ItemStack
     * @param compound the compound to set
     * @return the modified ItemStack
     */
    public static ItemStack setItemNBT(ItemStack itemStack, NBTTagCompound compound) {
        Object handle = ReflectUtil.invokeMethod(null, AS_NMS_COPY, itemStack).getOrThrow();
        ReflectUtil.setFieldValue(handle, ITEM_STACK_TAG, compound.getHandle()).getOrThrow();
        return (ItemStack) ReflectUtil.invokeMethod(null, AS_BUKKIT_COPY, handle).getOrThrow();
    }

    /**
     * Gets the NBT data from an Entity
     *
     * @param entity the entity
     * @return an NBTTagCompound containing the NBT data.
     */
    public static NBTTagCompound getEntityNBT(Entity entity) {
        Object handle = ReflectUtil.invokeMethod(entity, ENTITY_GET_HANDLE).getOrThrow();
        Object tagCompound = new NBTTagCompound().getHandle();
        ReflectUtil.invokeMethod(handle, ENTITY_SAVE_TO_NBT, tagCompound).getOrThrow();
        return NBTTagCompound.fromHandle(tagCompound);
    }

    /**
     * Sets the NBT data to an Entity
     *
     * @param entity the Entity
     * @param compound the compound to set
     */
    public static void setEntityNBT(Entity entity, NBTTagCompound compound) {
        Object handle = ReflectUtil.invokeMethod(entity, ENTITY_GET_HANDLE).getOrThrow();
        Object tagCompound = compound.getHandle();
        ReflectUtil.invokeMethod(handle, ENTITY_LOAD_FROM_NBT, tagCompound).getOrThrow();
    }

    /**
     * Gets the NBT data from a TileEntity
     *
     * NOTE: If the BlockState is not connected to a TileEntity,
     * null is returned.
     *
     * @param blockState the TileEntity's BlockState
     * @return an NBTTagCompound containing the NBT data.
     */
    public static NBTTagCompound getTileEntityNBT(BlockState blockState) {
        Object handle = null;
        if (!ReflectUtil.isVersionHigherOrEqual(1, 12, 1)) {
            handle = ReflectUtil.invokeMethod(blockState, GET_TILE_ENTITY).getOrThrow();
        } else if (CRAFT_BLOCK_ENTITY_STATE_CLASS.isAssignableFrom(blockState.getClass())) {
            handle = ReflectUtil.invokeMethod(blockState, GET_TILE_ENTITY).getOrThrow();
        }
        if (handle == null) {
            return null;
        }
        Object tagCompound = new NBTTagCompound().getHandle();
        ReflectUtil.invokeMethod(handle, TILE_ENTITY_SAVE_TO_NBT, tagCompound).getOrThrow();
        return NBTTagCompound.fromHandle(tagCompound);
    }

    /**
     * Sets the NBT data to a TileEntity
     *
     * NOTE: If the BlockState is not connected to a TileEntity,
     * nothing will happen.
     *
     * @param blockState the TileEntity's BlockState
     * @param compound the compound to set
     */
    public static void setTileEntityNBT(BlockState blockState, NBTTagCompound compound)  {
        Object handle = null;
        if (!ReflectUtil.isVersionHigherOrEqual(1, 12, 1)) {
            handle = ReflectUtil.invokeMethod(blockState, GET_TILE_ENTITY).getOrThrow();
        } else if (CRAFT_BLOCK_ENTITY_STATE_CLASS.isAssignableFrom(blockState.getClass())) {
            handle = ReflectUtil.invokeMethod(blockState, GET_TILE_ENTITY).getOrThrow();
        }
        if (handle == null) {
            return;
        }
        Object tagCompound = compound.getHandle();
        ReflectUtil.invokeMethod(handle, TILE_ENTITY_LOAD_FROM_NBT, tagCompound).getOrThrow();
    }


    private static final List<String> METHOD_NAMES = Arrays.asList("equals", "hashCode", "toString", "getTypeId");;
    public static Method findValueMethod(Class<?> clazz, Class<?> type) {
        int index = 0;
        while (true) {
            Method method = ReflectUtil.getMethodByType(clazz, type, index++).getOrThrow();
            if (Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (METHOD_NAMES.contains(method.getName())) {
                continue;
            }
            return method;
        }
    }
}