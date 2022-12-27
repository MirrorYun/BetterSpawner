package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static com.hyun.betterspawner.utils.NBTUtil.findValueMethod;

/**
 * A wrapper for NBTTagList
 * <p>
 * A List containing NBT tags with a maximum length of roughly 2^31
 *
 * @param <T> the type of this list
 */
public class NBTTagList<T extends NBTTagBase> extends NBTTagBase {
    private static final Constructor<?> TAG_LIST_CONSTRUCTOR;
    private static final Field LIST;
    private static final Method ADD_METHOD;

    static {
        final var clazz = ReflectUtil.getNMSClass("NBTTagList", "net.minecraft.nbt").getOrThrow();
        TAG_LIST_CONSTRUCTOR = ReflectUtil.getConstructor(clazz).getOrThrow();
        LIST = ReflectUtil.getDeclaredFieldByType(clazz, List.class, 0, true).getOrThrow();
        try {
            ADD_METHOD = List.class.getDeclaredMethod("add", Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the NBT wrapper equivalent to this NMS NBT tag
     *
     * @param handle the NMS NBT tag
     * @return the NBT Wrapper
     */
    public static NBTTagList<NBTTagBase> fromHandle(Object handle) {
        List<?> list = (List<?>) ReflectUtil.getFieldValue(handle, LIST).getOrThrow();
        NBTTagList<NBTTagBase> tagList = new NBTTagList<>();
        for (Object o : list) {
            tagList.add(NBTTagBase.fromHandle(o));
        }
        return tagList;
    }

    private final List<T> value;
    private int type = -1;

    /**
     * Initializes this List tag with the specified value
     *
     * @param value the List
     */
    public NBTTagList(List<T> value) {
        this.value = value;
        if (value.size() > 0) {
            type = value.get(0).getId();
        }
        for (NBTTagBase tag : value) {
            if (tag.getId() != type) {
                throw new RuntimeException("id " + tag.getId() + " does not match List type " + type);
            }
        }
    }

    /**
     * Initializes this List tag with the specified value
     *
     * @param value the contents
     */
    @SafeVarargs
    public NBTTagList(T... value) {
        // Arrays.asList() returns an immutable List, so we make a new, mutable one.
        this.value = new ArrayList<>(Arrays.asList(value));
        if (this.value.size() > 0) {
            type = this.value.get(0).getId();
        }
        for (NBTTagBase tag : value) {
            if (tag.getId() != type) {
                throw new RuntimeException("id " + tag.getId() + " does not match List type " + type);
            }
        }
    }

    /**
     * Adds an element to the List
     *
     * @param t the element to add
     * @return whether the List was changed (see {@link List#add(Object)})
     */
    public boolean add(T t) {
        if (t == null) {
            throw new IllegalArgumentException("element to add cannot be null");
        }
        if (type == -1) {
            type = t.getId();
        } else if (t.getId() != type) {
            throw new IllegalArgumentException("id " + t.getId() + " does not match List type " + type);
        }
        return value.add(t);
    }

    /**
     * Removes an element from the List
     *
     * @param t the element to remove
     * @return whether the List contained this element (see {@link List#remove(Object)})
     */
    public boolean remove(T t) {
        return value.remove(t);
    }

    /**
     * Gets the element at the specified index
     *
     * @param index the index
     * @return the element
     */
    public T get(int index) {
        return value.get(index);
    }

    /**
     * Sets the element at the specified index
     * @param index the index
     * @param t the element to set
     * @return the element previously at the index
     */
    public T set(int index, T t) {
        if (t == null) {
            throw new IllegalArgumentException("element to add cannot be null");
        }
        if (type == -1) {
            type = t.getId();
        } else if (t.getId() != type) {
            throw new IllegalArgumentException("id " + t.getId() + " does not match List type " + type);
        }
        return value.set(index, t);
    }

    /**
     * Gets the size of the List
     *
     * @return the size
     */
    public int size() {
        return value.size();
    }

    /**
     * Clears the List
     */
    public void clear() {
        this.value.clear();
    }

    /**
     * Adds all elements in the specified collection
     *
     * @param collection the elements
     * @return whether the List was changed (see {@link List#addAll(Collection)})
     */
    public boolean addAll(Collection<? extends T> collection) {
        if (collection == null) {
            throw new IllegalArgumentException("collection to add cannot be null");
        }
        if (type == -1 && collection.size() > 0) {
            type = collection.iterator().next().getId();
        }
        for (T t : collection) {
            if (t.getId() != type) {
                throw new IllegalArgumentException("id " + t.getId() + " does not match List type " + type);
            }
        }
        return this.value.addAll(collection);
    }

    /**
     * Gets the contents of this List
     *
     * @return an unmodifiable List with the contents of this List tag
     */
    public List<T> getContents() {
        return Collections.unmodifiableList(this.value);
    }

    /**
     * Gets the type of the elements contained in this List
     *
     * @return the id, or -1 if the list does not yet have an id.
     */
    public int getType() {
        return this.type;
    }

    /**
     * Gets the NMS equivalent to this NBT wrapper.
     *
     * @return the NMS NBT tag
     */
    @Override
    public Object getHandle() {
        Object handle = ReflectUtil.invokeConstructor(TAG_LIST_CONSTRUCTOR).getOrThrow();
        for (NBTTagBase baseTag : this.value) {
            ReflectUtil.invokeMethod(handle, ADD_METHOD, baseTag.getHandle()).getOrThrow();
        }
        return handle;
    }

    /**
     * Gets the id of this tag
     *
     * @return the id
     */
    @Override
    public int getId() {
        return 9;
    }

    @Override
    public String toString() {
        return "NBTTagList{value=" + this.value + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NBTTagList)) {
            return false;
        }
        return this.value.equals(((NBTTagList) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }
}
