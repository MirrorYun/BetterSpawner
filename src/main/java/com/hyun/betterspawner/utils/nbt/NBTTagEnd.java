package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;

import static com.hyun.betterspawner.utils.NBTUtil.TAG_COMPOUND_CLASS;

/**
 * A wrapper for NBTTagEnd
 * <p>
 * An end tag contains no data and is only used in serialization of NBT. It's only included
 * for completeness purposes.
 */
public class NBTTagEnd extends NBTTagBase {
    private static final Constructor<?> TAG_END_CONSTRUCTOR = ReflectUtil.getConstructor(TAG_COMPOUND_CLASS).getOrThrow();

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        return ReflectUtil.invokeConstructor(TAG_END_CONSTRUCTOR).getOrThrow();
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String toString() {
        return "NBTTagEnd";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NBTTagEnd;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
