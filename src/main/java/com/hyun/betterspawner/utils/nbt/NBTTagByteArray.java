package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagByteArray
 * <p>
 * Array of bytes with any length from 0 to roughly 2^31
 */
public class NBTTagByteArray extends NBTTagBase {
    private static final Constructor<?> TAG_BYTE_ARRAY_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagByteArray", "net.minecraft.nbt").getOrThrow();
        TAG_BYTE_ARRAY_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, byte[].class).getOrThrow();
        GET_VALUE = findValueMethod(clazz, byte[].class);
    }
    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagByteArray fromHandle(Object handle) {
        byte[] b = (byte[]) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagByteArray(b);
    }

    private byte[] value;

    /**
     * Initializes this byte array tag with the specified value
     *
     * @param value the byte array
     */
    public NBTTagByteArray(byte[] value) {
        this.value = value;
    }

    /**
     * Sets the value of this byte array tag
     *
     * @param value the byte array
     */
    public void setValue(byte[] value) {
        this.value = value;
    }

    /**
     * Gets the value of this byte array tag
     *
     * @return the byte array
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        return ReflectUtil.invokeConstructor(TAG_BYTE_ARRAY_CONSTRUCTOR, (Object) this.value).getOrThrow();
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 7;
    }

    @Override
    public String toString() {
        return "NBTTagByteArray{value=" + Arrays.toString(this.value) + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagByteArray)) {
            return false;
        }
        return Arrays.equals(this.value, ((NBTTagByteArray) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) this.value);
    }
}
