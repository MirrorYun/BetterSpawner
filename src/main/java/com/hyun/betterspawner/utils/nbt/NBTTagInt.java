package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagInt
 * <p>
 * Ints are 32-bit numbers, like in Java, and are the most common data type for storing integer
 * values.
 */
public class NBTTagInt extends NBTTagBase {
    private static final Executable TAG_INT_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagInt", "net.minecraft.nbt").getOrThrow();
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_INT_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, int.class).getOrThrow();
        } else {
            TAG_INT_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, int.class).getOrThrow();
        }
        GET_VALUE = findValueMethod(clazz, int.class);
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagInt fromHandle(Object handle) {
        int i = (int) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagInt(i);
    }

    private int value;

    /**
     * Initializes this int tag with the specified value
     *
     * @param value the int
     */
    public NBTTagInt(int value) {
        this.value = value;
    }

    /**
     * Sets the value of this int tag
     *
     * @param value the int
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Gets the value of this int tag
     *
     * @return the int
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_INT_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_INT_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_INT_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String toString() {
        return "NBTTagInt{value=" + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagInt)) {
            return false;
        }
        return this.value == ((NBTTagInt) obj).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
