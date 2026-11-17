package org.polyfrost.forgecompat;

import net.minecraft.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

public final class RavenHooks {
    private static final MethodHandle TEXT;
    private static final MethodHandle SAFE_WALK;
    private static final MethodHandle SLOWDOWN;
    private static final MethodHandle KNOCKBACK;
    private static final MethodHandle PICK;
    private static final boolean PRESENT;
    private static volatile boolean ready;

    static {
        MethodHandle[] hooks = find("keystrokesmod.tweaker.ASMEventHandler", "getUnformattedTextForChat", "onEntityMove", "onLivingUpdate", "onAttackTargetEntityWithCurrentItem", "onTick");
        if (hooks == null) hooks = find("keystrokesmod.tw.ev", "r", "edg", "sl", "sl2", "gmo");
        PRESENT = hooks != null;
        TEXT = PRESENT ? hooks[0] : null;
        SAFE_WALK = PRESENT ? hooks[1] : null;
        SLOWDOWN = PRESENT ? hooks[2] : null;
        KNOCKBACK = PRESENT ? hooks[3] : null;
        PICK = PRESENT ? hooks[4] : null;
    }

    private RavenHooks() {
    }

    static void modsInitialized() {
        ready = PRESENT;
    }

    public static boolean active() {
        return ready;
    }

    private static MethodHandle[] find(String owner, String text, String safeWalk, String slowdown, String knockback, String pick) {
        Class<?> handler;
        try {
            handler = Class.forName(owner, false, RavenHooks.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        try {
            return new MethodHandle[]{
                    lookup.findStatic(handler, text, methodType(String.class, String.class)),
                    lookup.findStatic(handler, safeWalk, methodType(boolean.class, Entity.class)),
                    lookup.findStatic(handler, slowdown, methodType(void.class)),
                    lookup.findStatic(handler, knockback, methodType(void.class, Entity.class)),
                    lookup.findStatic(handler, pick, methodType(void.class))
            };
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unsupported Raven version, " + owner + " is missing a hook", e);
        }
    }

    public static String text(String text) {
        if (!ready) return text;
        try {
            return (String) TEXT.invokeExact(text);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static boolean safeWalk(Entity entity) {
        try {
            return (boolean) SAFE_WALK.invokeExact(entity);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void slowdown() {
        try {
            SLOWDOWN.invokeExact();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void knockback(Entity target) {
        try {
            KNOCKBACK.invokeExact(target);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void pick() {
        try {
            PICK.invokeExact();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
