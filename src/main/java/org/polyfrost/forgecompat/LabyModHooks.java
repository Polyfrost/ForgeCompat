package org.polyfrost.forgecompat;

import net.minecraft.client.gui.screen.menu.TitleScreen;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public final class LabyModHooks {
    private static boolean done;

    private LabyModHooks() {
    }

    public static void afterGuiOpenEvent() {
        if (done || !ForgeCompat.modsInitialized()) return;
        try {
            Class<?> labyMod = Class.forName("net.labymod.main.LabyMod", false, LabyModHooks.class.getClassLoader());
            Object instance = labyMod.getMethod("getInstance").invoke(null);
            Object listener = instance == null ? null : labyMod.getMethod("getGuiOpenListener").invoke(instance);
            if (listener == null) return;
            done = true;
            Class<?> type = listener.getClass();
            Field replaced = type.getDeclaredField("replacedThings");
            Field mainMenu = type.getDeclaredField("mainMenu");
            Field lastScreen = type.getDeclaredField("lastScreen");
            AccessibleObject.setAccessible(new Field[]{replaced, mainMenu, lastScreen}, true);
            if (replaced.getBoolean(listener)) return;
            Object menu = mainMenu.get(listener);
            Object last = lastScreen.get(listener);
            try {
                type.getMethod("onGuiOpen", GuiOpenEvent.class).invoke(listener, new GuiOpenEvent(new TitleScreen()));
            } finally {
                mainMenu.set(listener, menu);
                lastScreen.set(listener, last);
            }
        } catch (ClassNotFoundException e) {
            done = true;
        } catch (ReflectiveOperationException | LinkageError e) {
            done = true;
            ForgeCompat.LOGGER.warn("Failed to set up LabyMod cosmetics", e);
        }
    }
}
