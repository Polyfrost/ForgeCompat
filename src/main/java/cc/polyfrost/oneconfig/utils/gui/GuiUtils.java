package cc.polyfrost.oneconfig.utils.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public final class GuiUtils {
    private GuiUtils() {
    }

    public static void displayScreen(Screen screen) {
        Minecraft mc = Minecraft.getInstance();
        mc.executeTask(() -> mc.openScreen(screen));
    }

    public static void closeScreen() {
        displayScreen(null);
    }
}
