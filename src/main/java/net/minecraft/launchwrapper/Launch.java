package net.minecraft.launchwrapper;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Launch {
    public static File minecraftHome = FabricLoader.getInstance().getGameDir().toFile();
    public static LaunchClassLoader classLoader = new LaunchClassLoader();
    public static Map<String, Object> blackboard = new HashMap<>(Map.of(
            "fml.deobfuscatedEnvironment", false,
            "TweakClasses", new ArrayList<>(List.of("org.spongepowered.asm.launch.MixinTweaker")),
            "Tweaks", new ArrayList<Object>(),
            "essential.loader.stage2.stage0tweakers", Set.of("gg.essential.loader.stage0.EssentialSetupTweaker")
    ));
}
