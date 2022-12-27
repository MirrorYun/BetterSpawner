package com.hyun.betterspawner.utils.nbt;


import com.google.common.collect.Maps;
import com.hyun.betterspawner.utils.NBTUtil;
import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hyun.betterspawner.utils.NBTUtil.TAG_COMPOUND_CLASS;

/**
 * A wrapper for NBTTagCompound
 * <p>
 * Compounds contain several other tags nested inside them and are
 * usually what the objects using NBT use to store the data.
 */
public class NBTTagCompound extends NBTTagBase {
    private static final Constructor<?> TAG_COMPOUND_CONSTRUCTOR = ReflectUtil.getConstructor(TAG_COMPOUND_CLASS).getOrThrow();
    private static final Field MAP = ReflectUtil.getDeclaredFieldByType(TAG_COMPOUND_CLASS, Map.class, 0, true).getOrThrow();

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagCompound fromHandle(Object handle) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        //noinspection unchecked
        Map<String, ?> map = (Map<String, ?>) ReflectUtil.getFieldValue(handle, MAP).getOrThrow();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            tagCompound.set(entry.getKey(), NBTTagBase.fromHandle(entry.getValue()));
        }
        return tagCompound;
    }

    private final Map<String, NBTTagBase> values = Maps.newHashMap();

    /**
     * Initializes an empty NBTTagCompound
     */
    public NBTTagCompound() {

    }

    /**
     * Initializes an NBTTagCompound using the values in the map provided
     *
     * @param map the map containing the values to carry over
     */
    public NBTTagCompound(Map<String, NBTTagBase> map) {
        values.putAll(map);
    }

    /**
     * Sets a key to a certain value
     *
     * @param key the key
     * @param value the value
     */
    public void set(String key, NBTTagBase value) {
        values.put(key, value);
    }

    /**
     * Sets a key to a certain byte
     *
     * @param key the key
     * @param value the byte
     */
    public void setByte(String key, byte value) {
        this.set(key, new NBTTagByte(value));
    }

    /**
     * Sets a key to a certain byte
     * <p>
     * NOTE: There is no boolean tag, so this is internally stored as a byte with either 1 (representing true)
     * or 0 (representing false) as its value. Any byte value other than zero will be interpreted as true.
     *
     * @param key the key
     * @param value the boolean
     */
    public void setBoolean(String key, boolean value) {
        this.set(key, new NBTTagByte((byte) (value ? 1 : 0)));
    }

    /**
     * Sets a key to a certain short
     *
     * @param key the key
     * @param value the short
     */
    public void setShort(String key, short value) {
        this.set(key, new NBTTagShort(value));
    }

    /**
     * Sets a key to a certain int
     *
     * @param key the key
     * @param value the int
     */
    public void setInt(String key, int value) {
        this.set(key, new NBTTagInt(value));
    }

    /**
     * Sets a key to a certain long
     *
     * @param key the key
     * @param value the long
     */
    public void setLong(String key, long value) {
        this.set(key, new NBTTagLong(value));
    }

    /**
     * Sets a key to a certain float
     *
     * @param key the key
     * @param value the float
     */
    public void setFloat(String key, float value) {
        this.set(key, new NBTTagFloat(value));
    }

    /**
     * Sets a key to a certain double
     *
     * @param key the key
     * @param value the double
     */
    public void setDouble(String key, double value) {
        this.set(key, new NBTTagDouble(value));
    }

    /**
     * Sets a key to a certain byte array
     *
     * @param key the key
     * @param value the byte array
     */
    public void setByteArray(String key, byte[] value) {
        this.set(key, new NBTTagByteArray(value));
    }

    /**
     * Sets a key to a certain String
     *
     * @param key the key
     * @param value the String
     */
    public void setString(String key, String value) {
        this.set(key, new NBTTagString(value));
    }

    /**
     * Sets a key to a certain List
     *
     * @param key the key
     * @param value the List
     */
    public void setList(String key, List<NBTTagBase> value) {
        this.set(key, new NBTTagList<>(value));
    }

    /**
     * Sets a key to a certain int array
     *
     * @param key the key
     * @param value the int array
     */
    public void setIntArray(String key, int[] value) {
        this.set(key, new NBTTagIntArray(value));
    }

    /**
     * Gets the value for a certain key
     *
     * NOTE: If the key doesn't correspond to a value, an exception will be raised.
     *
     * @param key the key
     * @return the value
     */
    public NBTTagBase get(String key) {
        return values.get(key);
    }

    /**
     * Gets the byte value for a certain key
     *
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type byte, an exception will be raised.
     *
     * @param key the key
     * @return the byte
     */
    public byte getByte(String key) {
        return ((NBTTagByte) this.get(key)).getValue();
    }

    /**
     * Gets the boolean value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type byte, an exception will be raised.
     * <p>
     * NOTE #2: There is no boolean tag, so this is internally stored as a byte with either 1 (representing true)
     * or 0 (representing false) as its value. Any byte value other than zero will be interpreted as true.
     *
     * @param key the key
     * @return the byte
     */
    public boolean getBoolean(String key) {
        return ((NBTTagByte) this.get(key)).getValue() != 0;
    }

    /**
     * Gets the short value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type short, an exception will be raised.
     *
     * @param key the key
     * @return the short
     */
    public short getShort(String key) {
        return ((NBTTagShort) this.get(key)).getValue();
    }

    /**
     * Gets the int value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type int, an exception will be raised.
     *
     * @param key the key
     * @return the int
     */
    public int getInt(String key) {
        return ((NBTTagInt) this.get(key)).getValue();
    }

    /**
     * Gets the long value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type long, an exception will be raised.
     *
     * @param key the key
     * @return the long
     */
    public long getLong(String key) {
        return ((NBTTagLong) this.get(key)).getValue();
    }

    /**
     * Gets the float value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type float, an exception will be raised.
     *
     * @param key the key
     * @return the float
     */
    public float getFloat(String key) {
        return ((NBTTagFloat) this.get(key)).getValue();
    }

    /**
     * Gets the double value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type double, an exception will be raised.
     *
     * @param key the key
     * @return the double
     */
    public double getDouble(String key) {
        return ((NBTTagDouble) this.get(key)).getValue();
    }

    /**
     * Gets the byte array value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type byte array, an exception will be raised.
     *
     * @param key the key
     * @return the byte array
     */
    public byte[] getByteArray(String key) {
        return ((NBTTagByteArray) this.get(key)).getValue();
    }

    /**
     * Gets the String value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type String, an exception will be raised.
     *
     * @param key the key
     * @return the String
     */
    public String getString(String key) {
        return ((NBTTagString) this.get(key)).getValue();
    }

    /**
     * Gets the List value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type List, an exception will be raised.
     *
     * @param key the key
     * @return the List
     */
    public List<NBTTagBase> getList(String key) {
        //noinspection unchecked
        return ((NBTTagList<NBTTagBase>) this.get(key)).getContents();
    }

    /**
     * Gets the int array value for a certain key
     * <p>
     * NOTE: If the key doesn't correspond to a value or the value is not of
     * type int array, an exception will be raised.
     *
     * @param key the key
     * @return the int array
     */
    public int[] getIntArray(String key) {
        return ((NBTTagIntArray) this.get(key)).getValue();
    }

    /**
     * Returns an unmodifiable map with the current contents of this compound
     *
     * @return the map
     */
    public Map<String, NBTTagBase> getContents() {
        return Collections.unmodifiableMap(this.values);
    }

    /**
     * Tells whether the compound currently has a specified key
     *
     * @param key the key
     * @return whether the key exists
     */
    public boolean hasKey(String key) {
        return this.values.containsKey(key);
    }

    /**
     * Tells whether the compound currently has a specified key with a specified type
     *
     * @param key the key
     * @param type the type
     * @return whether the key exists and is of the correct type
     */
    public boolean hasKeyWithType(String key, Class<? extends NBTTagBase> type) {
        return this.values.containsKey(key) && this.values.get(key).getClass() == type;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        Object nbtTagCompound = ReflectUtil.invokeConstructor(TAG_COMPOUND_CONSTRUCTOR).getOrThrow();
        //noinspection unchecked
        Map<String, Object> handleMap = (Map<String, Object>) ReflectUtil.getFieldValue(nbtTagCompound, MAP).getOrThrow();
        for (Map.Entry<String, NBTTagBase> entry : this.values.entrySet()) {
            handleMap.put(entry.getKey(), entry.getValue().getHandle());
        }
        return nbtTagCompound;
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 10;
    }

    @Override
    public String toString() {
        return "NBTTagCompound{values=" + this.values + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagCompound)) {
            return false;
        }
        return this.values.equals(((NBTTagCompound) obj).values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.values);
    }
}
