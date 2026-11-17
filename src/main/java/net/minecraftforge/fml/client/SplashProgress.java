package net.minecraftforge.fml.client;

import net.minecraft.client.render.texture.TextureManager;
import net.minecraft.resource.Identifier;
import org.lwjgl.opengl.GL11;

public class SplashProgress {
    public static void start() {
    }

    public static void pause() {
    }

    public static void resume() {
    }

    public static void finish() {
    }

    public static int getMaxTextureSize() {
        return GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
    }

    public static void drawVanillaScreen(TextureManager renderEngine) {
    }

    public static void clearVanillaResources(TextureManager renderEngine, Identifier mojangLogo) {
    }

    public static void checkGLError(String where) {
    }
}
