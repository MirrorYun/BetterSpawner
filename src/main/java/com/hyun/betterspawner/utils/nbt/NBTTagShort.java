package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagShort
 * <p>
 * Shorts are 16-bit numbers, like in Java. They should only be used
 * if you are sure that the number won't exceed 16 bits. If you aren't, use
 * long or int instead.
 */
public class NBTTagShort extends NBTTagBase {
    private static final Executable TAG_SHORT_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagShort", "net.minecraft.nbt").getOrThrow();
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_SHORT_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, short.class).getOrThrow();
        } else {
            TAG_SHORT_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, short.class).getOrThrow();
        }
        GET_VALUE = findValueMethod(clazz, short.class);
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagShort fromHandle(Object handle) {
        short s = (short) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagShort(s);
    }

    private short value;

    /**
     * Initializes this short tag with the specified value
     *
     * @param value the short
     */
    public NBTTagShort(short value) {
        this.value = value;
    }

    /**
     * Sets the value of this short tag
     *
     * @param value the short
     */
    public void setValue(short value) {
        this.value = value;
    }

    /**
     * Gets the value of this short tag
     *
     * @return the short
     */
    public short getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_SHORT_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_SHORT_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_SHORT_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 2;
    }

    @Override
    public String toString() {
        return "NBTTagShort{value=" + value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagShort)) {
            return false;
        }
        return this.value == ((NBTTagShort) obj).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
