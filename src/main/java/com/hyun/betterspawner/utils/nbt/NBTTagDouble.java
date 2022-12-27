package com.hyun.betterspawner.utils.nbt;


import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagDouble
 * <p>
 * Doubles are 64 bit floating point numbers, like in Java, and are the most
 * common data type to store floating point numbers.
 */
public class NBTTagDouble extends NBTTagBase {
    private static final Executable TAG_DOUBLE_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagDouble", "net.minecraft.nbt").getOrThrow();
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_DOUBLE_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, double.class).getOrThrow();
        } else {
            TAG_DOUBLE_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, double.class).getOrThrow();
        }
        GET_VALUE = findValueMethod(clazz, double.class);
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagDouble fromHandle(Object handle) {
        double d = ((double) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow());
        return new NBTTagDouble(d);
    }

    private double value;

    /**
     * Initializes this double tag with the specified value
     *
     * @param value the double
     */
    public NBTTagDouble(double value) {
        this.value = value;
    }

    /**
     * Sets the value of this double tag
     *
     * @param value the double
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Gets the value of this double tag
     *
     * @return the double
     */
    public double getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_DOUBLE_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_DOUBLE_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_DOUBLE_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 6;
    }

    @Override
    public String toString() {
        return "NBTTagDouble{value=" + this.value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagDouble)) {
            return false;
        }
        return this.value == ((NBTTagDouble) obj).value;
    }
}
