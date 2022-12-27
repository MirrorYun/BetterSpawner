package com.hyun.betterspawner.utils.nbt;


import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagByte
 * <p>
 * Bytes are an 8-bit data type, like in Java. They are also used for booleans,
 * with 0 representing false and 1 representing true.
 */
public class NBTTagByte extends NBTTagBase {
    private static final Executable TAG_BYTE_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagByte", "net.minecraft.nbt").getOrThrow();
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_BYTE_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, byte.class).getOrThrow();
        } else {
            TAG_BYTE_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, byte.class).getOrThrow();
        }
        GET_VALUE = findValueMethod(clazz, byte.class);
    }
    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagByte fromHandle(Object handle) {
        byte b = (byte) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagByte(b);
    }

    private byte value;

    /**
     * Initializes this byte tag with the specified value
     *
     * @param value the byte
     */
    public NBTTagByte(byte value) {
        this.value = value;
    }

    /**
     * Sets the value of this byte tag
     *
     * @param value the byte
     */
    public void setValue(byte value) {
        this.value = value;
    }

    /**
     * Gets the value of this byte tag
     *
     * @return the byte
     */
    public byte getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_BYTE_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_BYTE_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_BYTE_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String toString() {
        return "NBTTagByte{value=" + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagByte)) {
            return false;
        }
        return this.value == ((NBTTagByte) obj).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
