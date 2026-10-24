package net.minecraftforge.fml.client;

import org.polyfrost.forgecompat.ForgeCompat;

public class FMLClientHandler {
    private static final FMLClientHandler INSTANCE = new FMLClientHandler();

    public static FMLClientHandler instance() {
        return INSTANCE;
    }

    public void onInitializationComplete() {
        ForgeCompat.initMods();
    }
}
