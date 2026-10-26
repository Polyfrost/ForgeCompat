package org.polyfrost.forgecompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.spongepowered.asm.mixin.Mixins;

final class Essential {
    static final String TWEAKER = "gg.essential.loader.stage0.EssentialSetupTweaker";
    private static final String MOD_JSON = "essential.mod.json";

    static void load(Path jar, ZipFile zip, Path outDir) throws IOException {
        Path dir = outDir.resolve("essential");
        Files.createDirectories(dir);
        Path modJar = update(jar, zip, dir);
        FileTime mtime = Files.getLastModifiedTime(modJar);
        try (ZipFile mod = new ZipFile(modJar.toFile())) {
            JsonObject desc;
            try (InputStream in = mod.getInputStream(mod.getEntry(MOD_JSON))) {
                desc = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            }
            for (JsonElement element : desc.getAsJsonArray("jars")) {
                JsonObject lib = element.getAsJsonObject();
                String file = lib.get("file").getAsString();
                Path out = dir.resolve(file.substring(file.lastIndexOf('/') + 1));
                if (!isCached(out, mtime)) {
                    ZipEntry entry = mod.getEntry(file);
                    try (InputStream in = mod.getInputStream(entry)) {
                        ForgeCompat.remapJar(ForgeCompat.readJar(ForgeCompat.readAll(in)), modJar, out, mtime);
                    }
                }
                FabricLauncherBase.getLauncher().addToClassPath(out);
            }

            Path out = dir.resolve("essential.jar");
            if (isCached(out, mtime)) ForgeCompat.scanMods(mod, modJar);
            else ForgeCompat.remapJar(ForgeCompat.readJar(mod), modJar, out, mtime);
            FabricLauncherBase.getLauncher().addToClassPath(out);
        }

        Mixins.addConfiguration("mixins.essential.json");
        Mixins.registerErrorHandlerClass("gg.essential.mixins.MixinErrorHandler");
        try {
            Class.forName("gg.essential.api.tweaker.EssentialTweaker").getField("initialized").setBoolean(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize Essential", e);
        }
    }

    private static boolean isCached(Path out, FileTime mtime) throws IOException {
        return Files.exists(out) && Files.getLastModifiedTime(out).toMillis() == mtime.toMillis();
    }

    @SuppressWarnings("unchecked")
    private static Path update(Path container, ZipFile zip, Path dir) throws IOException {
        byte[] stage1;
        try (InputStream in = zip.getInputStream(zip.getEntry("gg/essential/loader/stage0/stage1.jar"))) {
            stage1 = ForgeCompat.readAll(in);
        }
        Path stage2 = dir.resolve("stage2.jar");
        Files.write(stage2, ForgeCompat.readJar(stage1).get("gg/essential/loader/stage1/stage2.jar"));
        try (URLClassLoader loader = new URLClassLoader(new URL[]{stage2.toUri().toURL(), container.toUri().toURL()}, Essential.class.getClassLoader())) {
            String result = ((Supplier<String>) loader.loadClass("gg.essential.loader.stage2.EssentialModUpdater").getConstructor().newInstance()).get();
            for (JsonElement element : new JsonParser().parse(result).getAsJsonArray()) {
                JsonObject mod = element.getAsJsonObject();
                if (mod.get("id").getAsString().equals("essential")) return Path.of(mod.get("file").getAsString());
            }
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to run Essential loader", e);
        }
        throw new IOException("Essential loader did not provide Essential");
    }
}
