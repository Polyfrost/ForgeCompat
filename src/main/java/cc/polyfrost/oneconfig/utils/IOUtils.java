package cc.polyfrost.oneconfig.utils;

import net.minecraft.client.gui.screen.Screen;

public final class IOUtils {
    private IOUtils() {
    }

    public static void copyStringToClipboard(String text) {
        Screen.setClipboard(text);
    }

    public static String getStringFromClipboard() {
        return Screen.getClipboard();
    }
}
