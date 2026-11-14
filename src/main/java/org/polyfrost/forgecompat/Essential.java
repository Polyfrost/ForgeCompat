package org.polyfrost.forgecompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
    private static final String STAGE2_UPDATES = "https://downloads.essential.gg/v1/mods/essential/loader-stage2/updates/stable/forge_1-8-9/";

    static void load(List<Path> jars, Path outDir) throws IOException {
        Path dir = outDir.resolve("essential");
        Files.createDirectories(dir);
        Path modJar = update(jars, dir);
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

        ForgeCompat.addMixinConfig("mixins.essential.json");
        Mixins.registerErrorHandlerClass("gg.essential.mixins.MixinErrorHandler");
        try {
            Class.forName("gg.essential.api.tweaker.EssentialTweaker").getField("initialized").setBoolean(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize Essential", e);
        }
    }

    static void loadApi(Path outDir) throws IOException {
        Path dir = outDir.resolve("essential-api");
        Files.createDirectories(dir);
        for (String name : List.of("universalcraft", "elementa", "vigilance", "essential-api")) {
            Path out = dir.resolve(name + ".jar");
            if (!Files.exists(out)) {
                try (InputStream in = Essential.class.getResourceAsStream("/forgecompat/essential/" + name + ".jar")) {
                    ForgeCompat.remapJar(ForgeCompat.readJar(ForgeCompat.readAll(in)), out, out, FileTime.fromMillis(0));
                }
            }
            FabricLauncherBase.getLauncher().addToClassPath(out);
        }
    }

    private static boolean isCached(Path out, FileTime mtime) throws IOException {
        return Files.exists(out) && Files.getLastModifiedTime(out).toMillis() == mtime.toMillis();
    }

    @SuppressWarnings("unchecked")
    private static Path update(List<Path> jars, Path dir) throws IOException {
        Path stage2 = dir.resolve("stage2.jar");
        Path container = jars.get(0);
        boolean embedded = false;
        for (Path jar : jars) {
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                if (zip.getEntry(MOD_JSON) != null) container = jar;
                if (embedded) continue;
                byte[] stage1;
                try (InputStream in = zip.getInputStream(zip.getEntry("gg/essential/loader/stage0/stage1.jar"))) {
                    stage1 = ForgeCompat.readAll(in);
                }
                byte[] bytes = ForgeCompat.readJar(stage1).get("gg/essential/loader/stage1/stage2.jar");
                if (bytes == null) continue;
                Files.write(stage2, bytes);
                embedded = true;
            }
        }
        if (!embedded) downloadStage2(stage2);
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

    private static void downloadStage2(Path stage2) throws IOException {
        Path download = stage2.resolveSibling("stage2-download.jar");
        JsonObject meta = null;
        try (InputStream in = new URL(STAGE2_UPDATES).openStream()) {
            meta = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            if (!Files.exists(download)) throw e;
        }
        byte[] bytes = Files.exists(download) ? Files.readAllBytes(download) : null;
        if (meta != null && (bytes == null || !md5(bytes).equals(meta.get("checksum").getAsString()))) {
            try (InputStream in = new URL(meta.get("url").getAsString()).openStream()) {
                bytes = ForgeCompat.readAll(in);
            }
            if (!md5(bytes).equals(meta.get("checksum").getAsString())) throw new IOException("Essential loader checksum mismatch");
            Files.write(download, bytes);
        }
        byte[] real = ForgeCompat.readJar(bytes).get("gg/essential/loader/stage2/real-stage2.jar");
        Files.write(stage2, real != null ? real : bytes);
    }

    private static String md5(byte[] bytes) {
        try {
            return String.format("%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(bytes)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
