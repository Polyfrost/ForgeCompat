package net.minecraftforge.fml.common;

import org.polyfrost.forgecompat.ForgeCompat;
import net.fabricmc.loader.api.FabricLoader;

public class Loader {
    public static boolean isModLoaded(String modid) {
        return ForgeCompat.MOD_IDS.contains(modid) || FabricLoader.getInstance().isModLoaded(modid);
    }
}
