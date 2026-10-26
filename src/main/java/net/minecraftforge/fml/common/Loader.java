package net.minecraftforge.fml.common;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.polyfrost.forgecompat.ForgeCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;

public class Loader {
    private static final Loader INSTANCE = new Loader();

    public static Loader instance() {
        return INSTANCE;
    }

    public static boolean isModLoaded(String modid) {
        return ForgeCompat.MODS.containsKey(modid) || FabricLoader.getInstance().isModLoaded(modid);
    }

    public List<ModContainer> getModList() {
        return new ArrayList<>(getIndexedModList().values());
    }

    public Map<String, ModContainer> getIndexedModList() {
        Map<String, ModContainer> mods = new LinkedHashMap<>(ForgeCompat.MODS);
        for (net.fabricmc.loader.api.ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();
            ModOrigin origin = mod.getOrigin();
            File source = origin.getKind() == ModOrigin.Kind.PATH && !origin.getPaths().isEmpty() ? origin.getPaths().get(0).toFile() : new File("");
            mods.putIfAbsent(meta.getId(), new Container(meta.getId(), meta.getName(), meta.getVersion().getFriendlyString(), source));
        }
        return mods;
    }

    public record Container(String getModId, String getName, String getVersion, File getSource) implements ModContainer {
    }
}
