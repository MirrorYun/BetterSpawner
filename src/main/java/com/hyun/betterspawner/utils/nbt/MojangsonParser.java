package com.hyun.betterspawner.utils.nbt;

import com.hyun.betterspawner.utils.ReflectUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.hyun.betterspawner.utils.NBTUtil.TAG_COMPOUND_CLASS;

public class MojangsonParser {
    private static final Method PARSE_METHOD = ReflectUtil.getMethodByTypeAndParams(ReflectUtil.getNMSClass("MojangsonParser", "net.minecraft.nbt").getOrThrow(), TAG_COMPOUND_CLASS, 0, String.class).getOrThrow();

    public static class MojangsonParseException extends Exception {
        public MojangsonParseException(String message) {
            super(message);
        }
    }
    /**
     * Parses a Mojangson string into an NBT tag
     *
     * @param mojangson the Mojangson string
     * @return the NBT tag
     */
    public static NBTTagCompound parse(String mojangson) throws MojangsonParseException {
        try {
            return NBTTagCompound.fromHandle(ReflectUtil.invokeMethod(null, PARSE_METHOD, mojangson).getOrThrow());
        } catch (ReflectUtil.ReflectionException e) {
            if(e.getCause() instanceof InvocationTargetException) throw new MojangsonParseException(e.getCause().getCause().getMessage());
            throw e;
        }
    }
}