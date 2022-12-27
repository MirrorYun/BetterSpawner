package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagFloat
 * <p>
 * Floats are 32 bit floating point numbers, like in Java. They should only be used if you are sure
 * the number will not exceed 32 bits. If you aren't, use double instead.
 */
public class NBTTagFloat extends NBTTagBase {
    private static final Executable TAG_FLOAT_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagFloat", "net.minecraft.nbt").getOrThrow();
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_FLOAT_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, float.class).getOrThrow();
        } else {
            TAG_FLOAT_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, float.class).getOrThrow();
        }
        GET_VALUE = findValueMethod(clazz, float.class);
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagFloat fromHandle(Object handle) {
        float f = (float) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagFloat(f);
    }

    private float value;

    /**
     * Initializes this float tag with the specified value
     *
     * @param value the float
     */
    public NBTTagFloat(float value) {
        this.value = value;
    }

    /**
     * Sets the value of this float tag
     *
     * @param value the float
     */
    public void setValue(float value) {
        this.value = value;
    }

    /**
     * Gets the value of this float tag
     *
     * @return the float
     */
    public float getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_FLOAT_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_FLOAT_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_FLOAT_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 5;
    }

    @Override
    public String toString() {
        return "NBTTagFloat{value=" + this.value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagFloat)) {
            return false;
        }
        return this.value == ((NBTTagFloat) obj).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
