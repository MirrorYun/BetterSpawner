package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagLong
 * <p>
 * Longs are 64-bit numbers, like in Java, and are most commonly used to store timestamps.
 */
public class NBTTagLong extends NBTTagBase {
    private static final Executable TAG_LONG_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagLong", "net.minecraft.nbt").getOrThrow();
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_LONG_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, long.class).getOrThrow();
        } else {
            TAG_LONG_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, long.class).getOrThrow();
        }
        GET_VALUE = findValueMethod(clazz, long.class);
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagLong fromHandle(Object handle) {
        long l = (long) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagLong(l);
    }

    private long value;

    /**
     * Initializes this long tag with the specified value
     *
     * @param value the long
     */
    public NBTTagLong(long value) {
        this.value = value;
    }

    /**
     * Sets the value of this long tag
     *
     * @param value the long
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * Gets the value of this long tag
     *
     * @return the long
     */
    public long getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_LONG_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_LONG_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_LONG_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 4;
    }

    @Override
    public String toString() {
        return "NBTTagLong{value=" + this.value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagLong)) {
            return false;
        }
        return this.value == ((NBTTagLong) obj).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
