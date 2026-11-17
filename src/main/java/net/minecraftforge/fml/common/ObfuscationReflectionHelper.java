package net.minecraftforge.fml.common;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ObfuscationReflectionHelper {
    public static <T, E> T getPrivateValue(Class<? super E> owner, E instance, String... names) {
        return ReflectionHelper.getPrivateValue(owner, instance, names);
    }

    public static <T, E> void setPrivateValue(Class<? super T> owner, T instance, E value, String... names) {
        ReflectionHelper.setPrivateValue(owner, instance, value, names);
    }

    public static <T, E> T getPrivateValue(Class<? super E> owner, E instance, int fieldIndex) {
        return ReflectionHelper.getPrivateValue(owner, instance, fieldIndex);
    }

    public static <T, E> void setPrivateValue(Class<? super T> owner, T instance, E value, int fieldIndex) {
        ReflectionHelper.setPrivateValue(owner, instance, value, fieldIndex);
    }

    public static String[] remapFieldNames(String className, String... fieldNames) {
        return fieldNames;
    }
}
