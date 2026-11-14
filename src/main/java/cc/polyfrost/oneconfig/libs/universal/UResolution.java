package cc.polyfrost.oneconfig.libs.universal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Window;

public final class UResolution {
    private UResolution() {
    }

    public static int getWindowWidth() {
        return Minecraft.getInstance().width;
    }

    public static int getWindowHeight() {
        return Minecraft.getInstance().height;
    }

    public static int getScaledWidth() {
        return new Window(Minecraft.getInstance()).getWidth();
    }

    public static int getScaledHeight() {
        return new Window(Minecraft.getInstance()).getHeight();
    }

    public static double getScaleFactor() {
        return new Window(Minecraft.getInstance()).getScale();
    }
}
