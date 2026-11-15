package net.minecraftforge.fml.common;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.polyfrost.forgecompat.ForgeCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;

public class Loader {
    public static final String MC_VERSION = "1.8.9";
    private static final Loader INSTANCE = new Loader();
    private static final MinecraftDummyContainer MINECRAFT = new MinecraftDummyContainer("1.8.9");

    public static Loader instance() {
        return INSTANCE;
    }

    public static boolean isModLoaded(String modid) {
        return ForgeCompat.MODS.containsKey(modid) || FabricLoader.getInstance().isModLoaded(modid);
    }

    public List<ModContainer> getModList() {
        return new ArrayList<>(getIndexedModList().values());
    }

    public MinecraftDummyContainer getMinecraftModContainer() {
        return MINECRAFT;
    }

    public String getMCVersionString() {
        return "Minecraft 1.8.9";
    }

    public String getMCPVersionString() {
        return "MCP 9.19";
    }

    public boolean hasReachedState(LoaderState state) {
        return ForgeCompat.loaderState().ordinal() >= state.ordinal();
    }

    public boolean isInState(LoaderState state) {
        return getLoaderState() == state;
    }

    public final LoaderState getLoaderState() {
        return ForgeCompat.loaderState();
    }

    public String getFMLVersionString() {
        return "8.0.99.99";
    }

    public Map<String, String> getCustomModProperties(String modId) {
        ModContainer mod = getIndexedModList().get(modId);
        return mod == null ? ModContainer.EMPTY_PROPERTIES : mod.getCustomModProperties();
    }

    public BiMap<ModContainer, Object> getModObjectList() {
        BiMap<ModContainer, Object> objects = HashBiMap.create();
        for (ModContainer mod : ForgeCompat.MODS.values()) {
            Object instance = mod.getMod();
            if (instance != null) objects.forcePut(mod, instance);
        }
        return objects;
    }

    public BiMap<Object, ModContainer> getReversedModObjectList() {
        return getModObjectList().inverse();
    }

    public void runtimeDisableMod(String modId) {
    }

    public ModContainer activeModContainer() {
        return null;
    }

    public File getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().toFile();
    }

    public List<ModContainer> getActiveModList() {
        return getModList();
    }

    public Map<String, ModContainer> getIndexedModList() {
        Map<String, ModContainer> mods = new LinkedHashMap<>(ForgeCompat.MODS);
        for (net.fabricmc.loader.api.ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();
            ModOrigin origin = mod.getOrigin();
            File source = origin.getKind() == ModOrigin.Kind.PATH && !origin.getPaths().isEmpty() ? origin.getPaths().get(0).toFile() : new File("");
            mods.putIfAbsent(meta.getId(), new Container(meta.getId(), meta.getName(), meta.getVersion().getFriendlyString(), source, ModContainer.EMPTY_PROPERTIES));
        }
        return mods;
    }

    public record Container(String getModId, String getName, String getVersion, File getSource, Map<String, String> getCustomModProperties) implements ModContainer {
    }
}
