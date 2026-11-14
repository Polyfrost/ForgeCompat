package org.polyfrost.forgecompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.apache.logging.log4j.LogManager;

public final class EarlyInit implements LanguageAdapter {
    static final Set<String> BLACKLIST = Set.of(
            "skinlayers3d", "apec", "armorhud", "autogg_reimagined", "bedwar", "bettersprint", "bfu", "com.chocohead.biab.BornInABarn", "chattriggers", "cpsdisplay",
            "craftify", "crash_assistant", "custom-crosshair-mod", "customnpcs", "Danker's Skyblock Mod", "dungeons_guide_loader",
            "entityculling.mixins.json", "epiczoomer", "euphoria_patcher", "fancywarpmenu", "foamfix", "fpsmaster", "cloudmc",
            "io.framesplus.forge.FMLLoadingPlugin", "fullbright", "great-scrollable-tooltips", "net.glease.healer.Healer", "heightlimitmod", "hotshirtlessmen",
            "hypixel_mod_api", "hytools", "i18nupdatemod.launchwrapper.LaunchWrapperTweaker", "idletweaks", "lomka",
            "lunarkeystrokes", "mcmouser", "microoptimizations", "mixinbooter", "modelfix", "modernsound", "mousebindfix", "notenoughupdates", "oldainmations", "oldinput",
            "optibye", "optifine.OptiFineForgeTweaker", "packcore", "particlesenhanced", "partlysaneskies", "phosphor-lighting", "pingfix", "pylon", "rawinput", "reflex", "reportplus",
            "ResourcePackOrganizer", "scathapro", "screenshotmessageenhancer", "secretroutesmod",
            "io.dogboy.serializationisbad.legacyforge.SerializationIsBadCoreMod", "serverlistbufferfixer", "simpletogglesprint", "sk1er_fullscreen",
            "text_overflow_scroll", "skyblocktweaks", "sbaunofficial", "skycofl", "skyguide", "skyhanni", "smooththirdpersoncamera",
            "stella", "titaniumrewrite", "toggle_perspective_fixed", "veloxcaelo", "viaforge", "waveycapes",
            // Polyfrost, microcontrollers, SkyClient, Chromatic and OneClient mods with Fabric versions
            "20_20_20", "overflowanimations", "autorespawn", "autoskyblockrejoin", "behindyouv3", "betternightvision", "chatting",
            "colorsaturation", "crashpatch", "polycrosshair", "damagetint", "entityglow", "evergreenhud", "freelook",
            "glintcolorizer", "polyhitbox", "hybedwars", "hybridge", "hychatter", "hyinfo", "hylobby", "hytils-reborn",
            "polyfrostkeystrokes", "legacy-chunk-borders", "mixmetica", "oneconfigbootstrap", "overflowparticles", "patcher",
            "pitdontwalkondeath", "polyblur", "polynametag", "polysprint", "polytime", "polyweather", "quickconfig", "quickquit",
            "redaction", "skyclientcosmetics", "skyblockclientupdater", "shy", "tcpdelaymod", "countdown", "vanillahud", "vignettetweaks"
    );
    private static final Set<String> FORGE_PREFERRED = Set.of("customskinloader-bootstrap", "zume");
    private static final List<String> BLACKLISTED_PACKAGES = List.of(
            "org/polyfrost/", "cc/woverflow/", "dev/microcontrollers/", "me/microcontrollers/", "com/github/microcontrollersdev/",
            "com/github/chromaticforge/", "co/skyclient/", "mynameisjeff/skyblockclientupdater/"
    );
    static final String ONECONFIG_V0_TWEAKER = "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker";
    private static final Pattern MODID = Pattern.compile("\"modid\"\\s*:\\s*\"([^\"]+)\"");

    public EarlyInit() throws IOException {
        Set<Path> hidden = new HashSet<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) continue;
            if (FORGE_PREFERRED.contains(mod.getMetadata().getId())) dropFabricHalf(mod);
            for (Path path : mod.getOrigin().getPaths()) {
                if (!Files.isRegularFile(path)) continue;
                try (ZipFile zip = new ZipFile(path.toFile())) {
                    if (!supported(zip, manifest(zip))) continue;
                }
                for (Path source : ((ModContainerImpl) mod).getCodeSourcePaths()) hidden.add(source.toRealPath());
            }
        }
        if (!hidden.isEmpty()) hide(hidden);
        AccessTransformers.apply();
    }

    private static void hide(Set<Path> hidden) throws IOException {
        ClassLoader knot = FabricLauncherBase.getLauncher().getTargetClassLoader();
        try {
            Field field = knot.getClass().getDeclaredField("urlLoader");
            field.setAccessible(true);
            URLClassLoader old = (URLClassLoader) field.get(knot);
            List<URL> urls = new ArrayList<>();
            for (URL url : old.getURLs()) {
                Path path = Path.of(url.toURI());
                if (!Files.exists(path) || !hidden.contains(path.toRealPath())) urls.add(url);
            }
            Constructor<?> constructor = field.getType().getDeclaredConstructor(URL[].class);
            constructor.setAccessible(true);
            field.set(knot, constructor.newInstance((Object) urls.toArray(new URL[0])));
        } catch (ReflectiveOperationException | URISyntaxException e) {
            throw new RuntimeException("Failed to hide Fabric-loaded Forge mods " + hidden, e);
        }
        LogManager.getLogger("ForgeCompat").info("Removed Fabric-loaded Forge mod jars from the class path: {}", hidden);
    }

    private static void dropFabricHalf(ModContainer mod) {
        try {
            Object metadata = ((ModContainerImpl) mod).getMetadata();
            Field mixins = metadata.getClass().getDeclaredField("mixins");
            mixins.setAccessible(true);
            mixins.set(metadata, new ArrayList<>());
            Field entrypoints = metadata.getClass().getDeclaredField("entrypoints");
            entrypoints.setAccessible(true);
            entrypoints.set(metadata, new HashMap<>());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to drop the Fabric half of " + mod.getMetadata().getId(), e);
        }
    }

    static Attributes manifest(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry("META-INF/MANIFEST.MF");
        if (entry == null) return new Attributes();
        try (InputStream in = zip.getInputStream(entry)) {
            return new Manifest(in).getMainAttributes();
        }
    }

    static List<String> mixinConfigs(Attributes manifest) {
        String configs = manifest.getValue("MixinConfigs");
        return configs == null ? new ArrayList<>() : Arrays.asList(configs.trim().split("\\s*,\\s*"));
    }

    static boolean isForgeMod(ZipFile zip, Attributes manifest) {
        if (zip.getEntry("mcmod.info") == null && zip.getEntry("META-INF/mods.toml") != null) return false;
        return manifest.getValue("TweakClass") != null || manifest.getValue("FMLCorePlugin") != null || manifest.getValue("MixinConfigs") != null || zip.getEntry("mcmod.info") != null;
    }

    static boolean supported(ZipFile zip, Attributes manifest) throws IOException {
        if (fabricOwned(zip) || !isForgeMod(zip, manifest) || blacklisted(zip, manifest)) return false;
        return !ONECONFIG_V0_TWEAKER.equals(manifest.getValue("TweakClass")) || FabricLoader.getInstance().isModLoaded("oneconfigv1");
    }

    static boolean fabricOwned(ZipFile zip) throws IOException {
        ZipEntry entry = zip.getEntry("fabric.mod.json");
        if (entry == null) return false;
        JsonObject json;
        try (InputStream in = zip.getInputStream(entry)) {
            json = new JsonParser().parse(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (RuntimeException e) {
            return true;
        }
        if (json.has("id") && FORGE_PREFERRED.contains(json.get("id").getAsString())) return false;
        return json.has("entrypoints") && !json.getAsJsonObject("entrypoints").entrySet().isEmpty()
                || json.has("mixins") && !json.getAsJsonArray("mixins").isEmpty();
    }

    private static boolean listed(String value) {
        return value != null && BLACKLIST.contains(value);
    }

    static boolean blacklisted(ZipFile zip, Attributes manifest) throws IOException {
        if (listed(manifest.getValue("TweakClass")) || listed(manifest.getValue("FMLCorePlugin"))
                || mixinConfigs(manifest).stream().anyMatch(BLACKLIST::contains) || modIds(zip).stream().anyMatch(BLACKLIST::contains)) return true;
        return zip.stream().map(ZipEntry::getName).filter(name -> name.endsWith(".class")).anyMatch(name -> BLACKLISTED_PACKAGES.stream().anyMatch(name::startsWith)
                || name.startsWith("cc/polyfrost/") && !name.startsWith("cc/polyfrost/oneconfig"));
    }

    static boolean hasMixinConfig(String name) {
        return anyModJar(zip -> mixinConfigs(manifest(zip)).contains(name));
    }

    static boolean hasEntry(String... names) {
        return anyModJar(zip -> Arrays.stream(names).anyMatch(name -> zip.getEntry(name) != null));
    }

    private interface JarTest {
        boolean test(ZipFile zip) throws IOException;
    }

    private static boolean anyModJar(JarTest test) {
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(FabricLoaderImpl.INSTANCE.getModsDirectory().toPath(), "*.jar")) {
            for (Path jar : jars) {
                try (ZipFile zip = new ZipFile(jar.toFile())) {
                    if (test.test(zip)) return true;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return false;
    }

    static List<String> modIds(ZipFile zip) throws IOException {
        List<String> ids = new ArrayList<>();
        ZipEntry entry = zip.getEntry("mcmod.info");
        if (entry == null) return ids;
        try (InputStream in = zip.getInputStream(entry)) {
            Matcher matcher = MODID.matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            while (matcher.find()) ids.add(matcher.group(1));
        }
        return ids;
    }

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) {
        throw new UnsupportedOperationException("forgecompat_early is not a real language adapter");
    }
}
