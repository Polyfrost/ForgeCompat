package net.minecraft.launchwrapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;

public class Launch {
    public static File minecraftHome = FabricLoader.getInstance().getGameDir().toFile();
    public static LaunchClassLoader classLoader = new LaunchClassLoader();
    public static Map<String, Object> blackboard = new HashMap<>(Map.of(
            "fml.deobfuscatedEnvironment", false,
            "essential.loader.stage2.stage0tweakers", Set.of("gg.essential.loader.stage0.EssentialSetupTweaker")
    ));
}
