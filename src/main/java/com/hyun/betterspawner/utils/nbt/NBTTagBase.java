package com.hyun.betterspawner.utils.nbt;


/**
 * The abstract base of all the NBT wrappers.
 */
public abstract class NBTTagBase {

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    public abstract Object getHandle();

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    public abstract int getId();

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagBase fromHandle(Object handle) {
        return switch (handle.getClass().getSimpleName()) {
            case "NBTTagEnd" -> new NBTTagEnd();
            case "NBTTagByte" -> NBTTagByte.fromHandle(handle);
            case "NBTTagShort" -> NBTTagShort.fromHandle(handle);
            case "NBTTagInt" -> NBTTagInt.fromHandle(handle);
            case "NBTTagLong" -> NBTTagLong.fromHandle(handle);
            case "NBTTagFloat" -> NBTTagFloat.fromHandle(handle);
            case "NBTTagDouble" -> NBTTagDouble.fromHandle(handle);
            case "NBTTagByteArray" -> NBTTagByteArray.fromHandle(handle);
            case "NBTTagString" -> NBTTagString.fromHandle(handle);
            case "NBTTagList" -> NBTTagList.fromHandle(handle);
            case "NBTTagCompound" -> NBTTagCompound.fromHandle(handle);
            case "NBTTagIntArray" -> NBTTagIntArray.fromHandle(handle);
            default -> null;
        };
    }
}
