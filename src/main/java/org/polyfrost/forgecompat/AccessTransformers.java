package org.polyfrost.forgecompat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.lib.classtweaker.api.ClassTweakerReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class AccessTransformers {
    private static final Logger LOGGER = LogManager.getLogger("ForgeCompat");

    private AccessTransformers() {
    }

    static void apply() throws IOException {
        boolean any = false;
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(FabricLoaderImpl.INSTANCE.getModsDirectory().toPath(), "*.jar")) {
            for (Path jar : jars) {
                try (ZipFile zip = new ZipFile(jar.toFile())) {
                    Attributes manifest = EarlyInit.manifest(zip);
                    if (!EarlyInit.supported(zip, manifest)) continue;
                    any = true;
                    String at = manifest.getValue("FMLAT");
                    if (at != null) accessTransformers(zip, at);
                }
            }
        }
        if (!any) return;
        try (InputStream in = AccessTransformers.class.getResourceAsStream("/forge_at.cfg")) {
            accessTransformer(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        String tabs = Mappings.get().mcpToRuntime("net/minecraft/creativetab/CreativeTabs");
        accessWidener("mutable field " + tabs + " " + Mappings.get().srgToRuntime("field_78032_a") + " [L" + tabs + ";\n");
    }

    private static void accessTransformers(ZipFile zip, String names) throws IOException {
        for (String name : names.trim().split("\\s+")) {
            ZipEntry entry = zip.getEntry("META-INF/" + name);
            if (entry == null) continue;
            try (InputStream in = zip.getInputStream(entry)) {
                accessTransformer(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    private static void accessTransformer(String text) {
        Mappings mappings = Mappings.get();
        StringBuilder out = new StringBuilder();
        for (String line : text.split("\n")) {
            int hash = line.indexOf('#');
            String[] p = (hash < 0 ? line : line.substring(0, hash)).trim().split("\\s+");
            if (p.length < 2 || p[0].startsWith("private") || p.length > 2 && p[2].startsWith("*")) continue;
            String mcpOwner = p[1].replace('.', '/');
            String owner = mappings.mcpToRuntime(mcpOwner);
            if (owner == null) {
                LOGGER.warn("Skipping access transformer for unknown class: {}", line);
                continue;
            }
            boolean definal = p[0].endsWith("-f");
            if (p.length == 2) {
                out.append("accessible class ").append(owner).append('\n');
                if (definal) out.append("extendable class ").append(owner).append('\n');
                continue;
            }
            int paren = p[2].indexOf('(');
            if (paren < 0) {
                String field = mappings.srgToRuntime(p[2]);
                String desc = field == null ? null : mappings.fieldDesc(owner, field);
                if (desc == null) {
                    LOGGER.warn("Skipping access transformer for unknown field: {}", line);
                    continue;
                }
                String target = " field " + owner + " " + field + " " + desc + "\n";
                out.append("accessible").append(target);
                if (definal) out.append("mutable").append(target);
            } else {
                String srgDesc = p[2].substring(paren);
                String target = " method " + owner + " " + mappings.toInt.mapMethodName(mcpOwner, p[2].substring(0, paren), srgDesc) + " " + mappings.toInt.mapMethodDesc(srgDesc) + "\n";
                out.append("accessible").append(target);
                if (definal) out.append("extendable").append(target);
            }
        }
        accessWidener(out.toString());
    }

    private static void accessWidener(String entries) {
        String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
        String text = "accessWidener v2 " + namespace + "\n" + entries;
        ClassTweakerReader.create(FabricLoaderImpl.INSTANCE.getClassTweaker()).read(text.getBytes(StandardCharsets.UTF_8), namespace);
    }
}
