package net.minecraftforge.fml.relauncher;

public class FMLLaunchHandler {
    public static Side side() {
        return Side.CLIENT;
    }

    public static boolean isDeobfuscatedEnvironment() {
        return false;
    }
}
