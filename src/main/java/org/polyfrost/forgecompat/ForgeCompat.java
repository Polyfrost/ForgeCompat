package org.polyfrost.forgecompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Mixins;

/**
 * Finds Forge mods in the mods folder, remaps them to intermediary, puts them on the game class
 * path and collects their tweakers, coremod transformers and @Mod classes.
 */
public class ForgeCompat implements PreLaunchEntrypoint {
    static final Logger LOGGER = LogManager.getLogger("ForgeCompat");
    public static final Map<String, ModContainer> MODS = new ConcurrentHashMap<>();
    private static final List<IClassTransformer> TRANSFORMERS = new CopyOnWriteArrayList<>();
    private static final List<String> MOD_CLASSES = new ArrayList<>();
    private static final List<String> SUPPORTED = Arrays.asList(
            "com.spiderfrog.gadgets.main.OAMTweaker", // OldAnimationsMod Cosmetics
            "de.hdskins.forge.v1855.HdSkinsPlugin", // HDSkins
            "mixins.hitdelayfix.json", // HitDelayFix
            Essential.TWEAKER
    );
    private static final String MIXIN_TWEAKER = "org.spongepowered.asm.launch.MixinTweaker";
    private static final Pattern DESC_CLASS = Pattern.compile("L([\\w/$]+);");
    private static final Pattern REFMAP_VALUE = Pattern.compile("(:\\s*\")([^\"]*)\"");
    private static final String MOD_DESC ="L" + Mod.class.getName().replace('.', '/') + ";";
    private static final String MINECRAFT = "ave";
    private static final String START_GAME = "am";
    private static Mappings mappings;
    private static boolean active;
    private static boolean modsInitialized;

    @Override
    public void onPreLaunch() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        String version = FabricLoader.getInstance().getModContainer("forgecompat").get().getMetadata().getVersion().getFriendlyString();
        Path outDir = gameDir.resolve(".forgecompat").resolve(version);
        List<String> tweakers = new ArrayList<>();
        List<String> plugins = new ArrayList<>();
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(FabricLoaderImpl.INSTANCE.getModsDirectory().toPath(), "*.jar")) {
            for (Path jar : jars) {
                try (ZipFile zip = new ZipFile(jar.toFile())) {
                    if (zip.getEntry("fabric.mod.json") != null) continue;
                    ZipEntry manifestEntry = zip.getEntry("META-INF/MANIFEST.MF");
                    Attributes manifest = new Attributes();
                    if (manifestEntry != null) {
                        try (InputStream in = zip.getInputStream(manifestEntry)) {
                            manifest = new Manifest(in).getMainAttributes();
                        }
                    }
                    String tweaker = manifest.getValue("TweakClass");
                    String plugin = manifest.getValue("FMLCorePlugin");
                    String configs = manifest.getValue("MixinConfigs");
                    List<String> mixinConfigs = configs == null ? new ArrayList<>() : Arrays.asList(configs.trim().split("\\s*,\\s*"));
                    if (tweaker == null && plugin == null && configs == null && zip.getEntry("mcmod.info") == null) continue;
                    if (!SUPPORTED.contains(tweaker) && !SUPPORTED.contains(plugin) && mixinConfigs.stream().noneMatch(SUPPORTED::contains)) {
                        LOGGER.warn("Skipping unsupported Forge mod {}", jar.getFileName());
                        continue;
                    }

                    if (!active) {
                        active = true;
                        Files.createDirectories(outDir);
                    }
                    Path remapped = outDir.resolve(jar.getFileName());
                    long start = System.nanoTime();
                    FileTime mtime = Files.getLastModifiedTime(jar);
                    boolean cached = Files.exists(remapped) && Files.getLastModifiedTime(remapped).toMillis() == mtime.toMillis();
                    if (Essential.TWEAKER.equals(tweaker)) {
                        Essential.load(jar, zip, outDir);
                        LOGGER.info("Loaded Essential from {} in {} ms", jar.getFileName(), (System.nanoTime() - start) / 1_000_000);
                        continue;
                    }
                    if (cached) scanMods(zip, jar);
                    else remapJar(readJar(zip), jar, remapped, mtime);
                    FabricLauncherBase.getLauncher().addToClassPath(remapped);
                    if (tweaker != null && !tweaker.equals(MIXIN_TWEAKER)) tweakers.add(tweaker);
                    if (plugin != null) plugins.add(plugin);
                    for (String config : mixinConfigs) Mixins.addConfiguration(config);
                    LOGGER.info("Loaded Forge mod {} in {} ms{}", jar.getFileName(), (System.nanoTime() - start) / 1_000_000, cached ? " (cached)" : "");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Forge mods", e);
        }

        long start = System.nanoTime();
        try {
            for (String name : tweakers) {
                ITweaker tweaker = (ITweaker) newInstance(name);
                tweaker.acceptOptions(new ArrayList<>(), gameDir.toFile(), null, null);
                tweaker.injectIntoClassLoader(new LaunchClassLoader());
            }
            for (String name : plugins) {
                IFMLLoadingPlugin plugin = (IFMLLoadingPlugin) newInstance(name);
                plugin.injectData(new HashMap<>());
                String[] transformers = plugin.getASMTransformerClass();
                if (transformers != null) for (String transformer : transformers) addTransformer(transformer);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load Forge coremod", e);
        }
        LOGGER.info("Set up Forge tweakers and coremods in {} ms", (System.nanoTime() - start) / 1_000_000);
    }

    public static void addTransformer(String name) {
        try {
            TRANSFORMERS.add((IClassTransformer) newInstance(name));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load Forge transformer " + name, e);
        }
    }

    static boolean active() {
        return active;
    }

    static synchronized Mappings mappings() {
        if (mappings == null) {
            long start = System.nanoTime();
            try (InputStream in = ForgeCompat.class.getResourceAsStream("/forgecompat_mappings.gz")) {
                mappings = new Mappings(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load ForgeCompat mappings", e);
            }
            LOGGER.info("Loaded mappings in {} ms", (System.nanoTime() - start) / 1_000_000);
        }
        return mappings;
    }

    static byte[] transform(String intName, byte[] bytes) {
        Mappings mappings = mappings();
        String notch = mappings.intToNotch.getOrDefault(intName, intName);
        String mcp = mappings.intToMcp.getOrDefault(intName, intName);
        bytes = remap(bytes, mappings.toNotch, false, true);
        if (notch.equals(MINECRAFT)) bytes = addInitHook(bytes);
        for (IClassTransformer transformer : TRANSFORMERS) {
            try {
                bytes = transformer.transform(notch.replace('/', '.'), mcp.replace('/', '.'), bytes);
            } catch (Throwable t) {
                LOGGER.error("Forge transformer {} failed on {}", transformer.getClass().getName(), mcp, t);
            }
        }
        return remap(bytes, mappings.toInt, false, false);
    }

    private static byte[] addInitHook(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        String handler = "net/minecraftforge/fml/client/FMLClientHandler";
        for (MethodNode method : node.methods) {
            if (!method.name.equals(START_GAME) || !method.desc.equals("()V")) continue;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != Opcodes.RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, handler, "instance", "()L" + handler + ";", false));
                hook.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, handler, "onInitializationComplete", "()V", false));
                method.instructions.insertBefore(insn, hook);
            }
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static void initMods() {
        if (modsInitialized) return;
        modsInitialized = true;
        for (String name : MOD_CLASSES) {
            long start = System.nanoTime();
            try {
                Object mod = newInstance(name);
                for (Method method : mod.getClass().getDeclaredMethods()) {
                    Class<?>[] params = method.getParameterTypes();
                    if (method.isAnnotationPresent(Mod.EventHandler.class) && params.length == 1 && params[0] == FMLInitializationEvent.class) {
                        method.setAccessible(true);
                        method.invoke(mod, new FMLInitializationEvent());
                    }
                }
            } catch (Throwable t) {
                LOGGER.error("Failed to initialize Forge mod {}", name, t);
            }
            LOGGER.info("Initialized Forge mod {} in {} ms", name, (System.nanoTime() - start) / 1_000_000);
        }
    }

    private static Object newInstance(String name) throws ReflectiveOperationException {
        return Class.forName(name, true, ForgeCompat.class.getClassLoader()).getDeclaredConstructor().newInstance();
    }

    static void scanMods(ZipFile zip, Path source) throws IOException {
        for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
            ZipEntry entry = it.nextElement();
            if (!entry.getName().endsWith(".class")) continue;
            try (InputStream in = zip.getInputStream(entry)) {
                scanMod(new ClassReader(in), source);
            }
        }
    }

    private static void scanMod(ClassReader reader, Path source) {
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (!desc.equals(MOD_DESC)) return null;
                MOD_CLASSES.add(reader.getClassName().replace('/', '.'));
                return new AnnotationVisitor(Opcodes.ASM9) {
                    private String modid;
                    private String name = "";
                    private String version = "";

                    @Override
                    public void visit(String key, Object value) {
                        if (key.equals("modid")) modid = (String) value;
                        else if (key.equals("name")) name = (String) value;
                        else if (key.equals("version")) version = (String) value;
                    }

                    @Override
                    public void visitEnd() {
                        if (modid != null) MODS.put(modid, new Loader.Container(modid, name.isEmpty() ? modid : name, version, source.toFile()));
                    }
                };
            }
        }, ClassReader.SKIP_CODE);
    }

    static Map<String, byte[]> readJar(ZipFile zip) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
            ZipEntry entry = it.nextElement();
            if (entry.isDirectory()) continue;
            try (InputStream in = zip.getInputStream(entry)) {
                entries.put(entry.getName(), readAll(in));
            }
        }
        return entries;
    }

    static Map<String, byte[]> readJar(byte[] jar) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(jar))) {
            for (ZipEntry entry; (entry = in.getNextEntry()) != null; ) {
                if (!entry.isDirectory()) entries.put(entry.getName(), readAll(in));
            }
        }
        return entries;
    }

    static void remapJar(Map<String, byte[]> entries, Path source, Path out, FileTime mtime) throws IOException {
        Mappings mappings = mappings();
        entries.keySet().removeIf(name -> name.startsWith("META-INF/") && (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA"))
                || name.startsWith("org/spongepowered/") || name.startsWith("META-INF/services/org.spongepowered.") || name.equals("META-INF/services/javax.annotation.processing.Processor"));
        mappings.modClasses.clear();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (!entry.getKey().endsWith(".class")) continue;
            ClassReader reader = new ClassReader(entry.getValue());
            mappings.modClasses.put(reader.getClassName(), new Mappings.ClassInfo(reader));
            scanMod(reader, source);
        }
        Path tmp = out.resolveSibling(out.getFileName() + ".tmp");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmp))) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                byte[] bytes = entry.getValue();
                if (entry.getKey().endsWith(".class")) bytes = remap(bytes, mappings.toInt, true, false);
                else if (entry.getKey().endsWith(".refmap.json") || entry.getKey().endsWith("-refmap.json")) bytes = remapRefmap(bytes, mappings);
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(bytes);
                zos.closeEntry();
            }
        }
        Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
        Files.setLastModifiedTime(out, mtime);
    }

    private static byte[] remapRefmap(byte[] bytes, Mappings mappings) {
        Matcher matcher = REFMAP_VALUE.matcher(new String(bytes, StandardCharsets.UTF_8));
        StringBuffer out = new StringBuffer();
        while (matcher.find()) matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group(1) + mappings.mapReference(matcher.group(2)) + "\""));
        matcher.appendTail(out);
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] remap(byte[] bytes, Remapper remapper, boolean hooks, boolean legacy) {
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor next = hooks ? new HookVisitor(writer) : legacy ? new LegacyVisitor(writer) : writer;
        new ClassReader(bytes).accept(new ClassRemapper(next, remapper), 0);
        return writer.toByteArray();
    }

    static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int n; (n = in.read(buffer)) != -1; ) out.write(buffer, 0, n);
        return out.toByteArray();
    }

    private static final class LegacyVisitor extends ClassVisitor {
        LegacyVisitor(ClassVisitor next) {
            super(Opcodes.ASM9, next);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                public void visitParameter(String name, int access) {
                }

                @Override
                public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                    return null;
                }

                @Override
                public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                    return null;
                }

                @Override
                public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
                    return null;
                }

                @Override
                public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
                    return null;
                }
            };
        }
    }

    private static AnnotationVisitor forgeTargets(AnnotationVisitor next) {
        return next == null ? null : new AnnotationVisitor(Opcodes.ASM9, next) {
            @Override
            public void visit(String name, Object value) {
                if (value instanceof String s && s.contains("net/minecraftforge/")) {
                    Matcher matcher = DESC_CLASS.matcher(s);
                    StringBuilder out = new StringBuilder();
                    while (matcher.find()) {
                        String mapped = mappings().mcpToRuntime(matcher.group(1));
                        matcher.appendReplacement(out, Matcher.quoteReplacement("L" + (mapped == null ? matcher.group(1) : mapped) + ";"));
                    }
                    value = matcher.appendTail(out).toString();
                }
                super.visit(name, value);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return forgeTargets(super.visitAnnotation(name, desc));
            }

            @Override
            public AnnotationVisitor visitArray(String name) {
                return forgeTargets(super.visitArray(name));
            }
        };
    }

    private static final class HookVisitor extends ClassVisitor {
        private final String handshake = mappings().mcpToRuntime("net/minecraft/network/handshake/client/C00Handshake");
        private final String handshakeDesc = "(ILjava/lang/String;IL" + mappings().mcpToRuntime("net/minecraft/network/EnumConnectionState") + ";";

        private String name;
        private String mixinTarget;

        HookVisitor(ClassVisitor next) {
            super(Opcodes.ASM9, next);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor next = super.visitAnnotation(desc, visible);
            if (!desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) return next;
            return new AnnotationVisitor(Opcodes.ASM9, next) {
                @Override
                public AnnotationVisitor visitArray(String key) {
                    return new AnnotationVisitor(Opcodes.ASM9, super.visitArray(key)) {
                        @Override
                        public void visit(String key, Object value) {
                            if (mixinTarget == null) mixinTarget = value instanceof Type type ? type.getInternalName() : mappings().mcpToRuntime(((String) value).replace('.', '/'));
                            super.visit(key, value);
                        }
                    };
                }
            };
        }

        private String outerInstance(String owner, String name, String desc) {
            if (mixinTarget == null || !owner.equals(this.name) || !name.startsWith("this$")) return name;
            String mapped = mappings().fieldByDesc(mixinTarget, desc);
            return mapped == null ? name : mapped;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return super.visitField(access, outerInstance(this.name, name, desc), desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    return forgeTargets(super.visitAnnotation(desc, visible));
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    super.visitFieldInsn(opcode, owner, outerInstance(owner, name, desc), desc);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    if (opcode == Opcodes.INVOKESPECIAL && owner.equals(handshake) && desc.equals(handshakeDesc + "Z)V")) {
                        super.visitInsn(Opcodes.POP);
                        super.visitMethodInsn(opcode, owner, name, handshakeDesc + ")V", itf);
                    } else if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/lang/Class") && name.equals("getDeclaredField")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/polyfrost/forgecompat/Hooks", "getDeclaredField", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }
                }
            };
        }
    }
}
