package org.polyfrost.forgecompat;

import java.lang.reflect.Field;

/** called from remapped Forge mods */
public final class Hooks {
    private static int modifiers;

    public static Field getDeclaredField(Class<?> owner, String name) throws NoSuchFieldException {
        try {
            return owner.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (owner == Field.class && name.equals("modifiers")) return Hooks.class.getDeclaredField("modifiers");
            throw e;
        }
    }
}
