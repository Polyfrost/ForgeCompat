package net.minecraftforge.fml.relauncher;

import net.fabricmc.loader.api.FabricLoader;

public class FMLInjectionData {
    public static Object[] data() {
        return new Object[]{"11", "15", "1", "2318", "1.8.9", "9.19", FabricLoader.getInstance().getGameDir().toFile(), null};
    }
}
