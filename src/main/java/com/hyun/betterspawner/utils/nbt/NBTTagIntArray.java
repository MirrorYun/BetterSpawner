package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagIntArray
 * <p>
 * Array of ints with any length from 0 to roughly 2^31
 */
public class NBTTagIntArray extends NBTTagBase {
    private static final Constructor<?> TAG_INT_ARRAY_CONSTRUCTOR;
    private static final Method GET_VALUE;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagIntArray", "net.minecraft.nbt").getOrThrow();
        TAG_INT_ARRAY_CONSTRUCTOR = ReflectUtil.getConstructor(clazz, int[].class).getOrThrow();
        GET_VALUE = findValueMethod(clazz, int[].class);
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagIntArray fromHandle(Object handle) {
        int[] i = (int[]) ReflectUtil.invokeMethod(handle, GET_VALUE).getOrThrow();
        return new NBTTagIntArray(i);
    }

    private int[] value;

    /**
     * Initializes this int array tag with the specified value
     *
     * @param value the int array
     */
    public NBTTagIntArray(int[] value) {
        this.value = value;
    }

    /**
     * Sets the value of this int array tag
     *
     * @param value the int array
     */
    public void setValue(int[] value) {
        this.value = value;
    }

    /**
     * Gets the value of this int array tag
     *
     * @return the int array
     */
    public int[] getValue() {
        return value;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        return ReflectUtil.invokeConstructor(TAG_INT_ARRAY_CONSTRUCTOR, (Object) this.value).getOrThrow();
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 11;
    }

    @Override
    public String toString() {
        return "NBTTagIntArray{value=" + Arrays.toString(this.value) + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagIntArray)) {
            return false;
        }
        return Arrays.equals(this.value, ((NBTTagIntArray) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) this.value);
    }
}
