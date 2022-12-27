package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagString
 * <p>
 * A String of UTF-8 characters with any length from 0 to roughly 2^15
 */
public class NBTTagString extends NBTTagBase {
    private static final Executable TAG_STRING_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagString", "net.minecraft.nbt").getOrThrow();
        GET_VALUE = findValueMethod(clazz, String.class);
        if(ReflectUtil.isVersionHigherOrEqual(1, 15)) {
            TAG_STRING_CONSTRUCTOR = ReflectUtil.getMethodByTypeAndParams(clazz, clazz, 0, String.class).getOrThrow();
        } else {
            TAG_STRING_CONSTRUCTOR = ReflectUtil.getConstructor(ReflectUtil.getNMSClass("NBTTagString", "net.minecraft.nbt").getOrThrow(), String.class).getOrThrow();
        }
    }
    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagString fromHandle(Object handle) {
        String s = (String) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagString(s);
    }

    private String value;

    /**
     * Initializes this String tag with the specified value
     *
     * @param value the String
     */
    public NBTTagString(String value) {
        this.value = value;
    }

    /**
     * Sets the value of this String tag
     *
     * @param value the String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of this String tag
     *
     * @return the String
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        if(TAG_STRING_CONSTRUCTOR instanceof Constructor) {
            return ReflectUtil.invokeConstructor((Constructor<?>) TAG_STRING_CONSTRUCTOR, value).getOrThrow();
        } else {
            return ReflectUtil.invokeMethod(null, (Method) TAG_STRING_CONSTRUCTOR, value).getOrThrow();
        }
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 8;
    }

    @Override
    public String toString() {
        return "NBTTagString{value=" + this.value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagString)) {
            return false;
        }
        return this.value.equals(((NBTTagString) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
