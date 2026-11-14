package org.polyfrost.forgecompat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.jar.Attributes;
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
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

/**
 * Finds Forge mods in the mods folder, remaps them to intermediary, puts them on the game class
 * path and collects their tweakers, coremod transformers and @Mod classes.
 */
public class ForgeCompat implements PreLaunchEntrypoint {
    static final Logger LOGGER = LogManager.getLogger("ForgeCompat");
    public static final Map<String, ModContainer> MODS = new ConcurrentHashMap<>();
    public static final Map<String, Object> INSTANCES = new ConcurrentHashMap<>();
    public static final ASMDataTable ASM_DATA = new ASMDataTable();
    private static final Map<String, Object> LOADED_MODS = new LinkedHashMap<>();
    static final Map<String, String> GUI_FACTORIES = new ConcurrentHashMap<>();
    /** remapped Forge mod jars, loaded as resource packs like FML does */
    public static final List<Path> RESOURCE_JARS = new CopyOnWriteArrayList<>();
    static final Map<Path, Path> ORIGINAL_JARS = new ConcurrentHashMap<>();
    private static final List<IClassTransformer> TRANSFORMERS = new CopyOnWriteArrayList<>();
    private static final Set<IClassTransformer> SRG_TRANSFORMERS = ConcurrentHashMap.newKeySet();
    private static final Set<String> SKIPPED_TRANSFORMERS = Set.of("keystrokesmod.tw.ct", "keystrokesmod.tweaker.ASMTransformerClass");
    // only bootstraps Mixin and adds its manifest MixinConfigs, which Fabric Mixin already handles
    private static final Set<String> SKIPPED_TWEAKERS = Set.of(ForgeCompat.MIXIN_TWEAKER, "com.seosean.showspawntime.tweaker.FMLLoadingPlugin", EarlyInit.ONECONFIG_V0_TWEAKER);
    private static final Map<String, Path> PLUGIN_JARS = new HashMap<>();
    private static final Set<String> FORGE_CLASSES = ConcurrentHashMap.newKeySet();
    /** class name -> modid */
    private static final Map<String, String> MOD_CLASSES = new LinkedHashMap<>();
    private static final String HOOKS = "org/polyfrost/forgecompat/Hooks";
    private static final String MIXIN_TWEAKER = "org.spongepowered.asm.launch.MixinTweaker";
    private static final Pattern DESC_CLASS = Pattern.compile("L([\\w/$]+);");
    private static final Pattern SRG_NAME = Pattern.compile("\\b(?:func|field)_\\d+_[a-zA-Z]+_?\\b");
    private static final Pattern REFMAP_VALUE = Pattern.compile("(:\\s*\")([^\"]*)\"");
    private static final Pattern REFMAP_KEY = Pattern.compile("\"([^\"]*)\"\\s*:\\s*\"");
    private static final String FORGE_DRAW_SCREEN = "Lnet/minecraftforge/client/ForgeHooksClient;drawScreen(Lnet/minecraft/client/gui/GuiScreen;IIF)V";
    private static final String FORGE_FONT_COLOR = "Lnet/minecraft/client/gui/FontRenderer;setColor(FFFF)V";
    private static final String FORGE_ITEM_STACK = "<init>(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V";
    private static final String MOD_DESC ="L" + Mod.class.getName().replace('.', '/') + ";";
    private static final String OVERWRITE_MIXINS = "net/ccbluex/liquidbounce/injection/forge/mixins/";
    // Fabric mods can't inject into an overwritten method, so these are stripped and redone as injectors in mixin.overwrite
    static final Map<String, Set<String>> REPLACED_OVERWRITES = Map.of(
            OVERWRITE_MIXINS + "gui/MixinGuiConnecting.class", Set.of("func_73863_a"),
            OVERWRITE_MIXINS + "render/MixinRenderPlayer.class", Set.of("func_177137_d"),
            OVERWRITE_MIXINS + "render/MixinLayerHeldItem.class", Set.of("func_177141_a"),
            OVERWRITE_MIXINS + "client/MixinResourcePackRepository.class", Set.of("func_183028_i")
    );
    private static final String EVENT_BUS = "net/minecraftforge/fml/common/eventhandler/EventBus";
    private static final String LISTENER_LIST = "net/minecraftforge/fml/common/eventhandler/ListenerList";
    private static final String MINECRAFT = "ave";
    private static final String START_GAME = "am";
    private static boolean active;
    private static boolean modsInitialized;

    @Override
    public void onPreLaunch() {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        String version = FabricLoader.getInstance().getModContainer("forgecompat").get().getMetadata().getVersion().getFriendlyString();
        Path outDir = gameDir.resolve(".forgecompat").resolve(version);
        List<String> tweakers = new ArrayList<>();
        List<String> manifestMixinConfigs = new ArrayList<>();
        List<String> plugins = new ArrayList<>();
        List<Path> essentialJars = new ArrayList<>();
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(FabricLoaderImpl.INSTANCE.getModsDirectory().toPath(), "*.jar")) {
            for (Path jar : jars) {
                try (ZipFile zip = new ZipFile(jar.toFile())) {
                    Attributes manifest = EarlyInit.manifest(zip);
                    if (!EarlyInit.isForgeMod(zip, manifest)) continue;
                    if (!EarlyInit.supported(zip, manifest)) {
                        if (EarlyInit.fabricOwned(zip)) continue;
                        if (EarlyInit.blacklisted(zip, manifest)) LOGGER.warn("Skipping blacklisted Forge mod {}", jar.getFileName());
                        else LOGGER.warn("Skipping Forge mod {}: it needs OneConfig installed", jar.getFileName());
                        continue;
                    }
                    String tweaker = manifest.getValue("TweakClass");
                    String plugin = manifest.getValue("FMLCorePlugin");
                    List<String> mixinConfigs = EarlyInit.mixinConfigs(manifest);

                    if (!active) {
                        active = true;
                        Files.createDirectories(outDir);
                    }
                    Path remapped = outDir.resolve(jar.getFileName());
                    long start = System.nanoTime();
                    FileTime mtime = Files.getLastModifiedTime(jar);
                    boolean cached = Files.exists(remapped) && Files.getLastModifiedTime(remapped).toMillis() == mtime.toMillis();
                    if (Essential.TWEAKER.equals(tweaker)) {
                        essentialJars.add(jar);
                        if (zip.getEntry("essential.mod.json") != null) continue;
                        tweaker = null;
                    }
                    if (cached) scanMods(zip, jar);
                    else remapJar(readJar(zip), jar, remapped, mtime);
                    FabricLauncherBase.getLauncher().addToClassPath(remapped);
                    RESOURCE_JARS.add(remapped);
                    ORIGINAL_JARS.put(remapped.toAbsolutePath().normalize(), jar);
                    if (tweaker != null && !SKIPPED_TWEAKERS.contains(tweaker)) tweakers.add(tweaker);
                    if (plugin != null) {
                        plugins.add(plugin);
                        PLUGIN_JARS.put(plugin, jar);
                    }
                    manifestMixinConfigs.addAll(mixinConfigs);
                    LOGGER.info("Loaded Forge mod {} in {} ms{}", jar.getFileName(), (System.nanoTime() - start) / 1_000_000, cached ? " (cached)" : "");
                }
            }
            if (!essentialJars.isEmpty()) {
                long start = System.nanoTime();
                Essential.load(essentialJars, outDir);
                LOGGER.info("Loaded Essential in {} ms", (System.nanoTime() - start) / 1_000_000);
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
                Object instance = newInstance(name);
                if (!(instance instanceof IFMLLoadingPlugin plugin)) {
                    LOGGER.warn("Skipping FMLCorePlugin {}: not an IFMLLoadingPlugin", name);
                    continue;
                }
                plugin.injectData(pluginData(gameDir, plugin));
                String[] transformers = plugin.getASMTransformerClass();
                IFMLLoadingPlugin.SortingIndex index = plugin.getClass().getAnnotation(IFMLLoadingPlugin.SortingIndex.class);
                boolean srg = index != null && index.value() > 1000;
                if (transformers != null) for (String transformer : transformers) addTransformer(transformer, srg);
            }
            for (String config : manifestMixinConfigs) addMixinConfig(config);
            if (active) hookModClasses();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load Forge coremod", e);
        }
        LOGGER.info("Set up Forge tweakers and coremods in {} ms", (System.nanoTime() - start) / 1_000_000);
    }

    private static Map<String, Object> pluginData(Path gameDir, IFMLLoadingPlugin plugin) {
        Map<String, Object> data = new HashMap<>();
        data.put("mcLocation", gameDir.toFile());
        data.put("coremodList", new ArrayList<>());
        data.put("runtimeDeobfuscationEnabled", true);
        Path source = PLUGIN_JARS.get(plugin.getClass().getName());
        data.put("coremodLocation", source == null ? null : source.toFile());
        return data;
    }

    public static void addTransformer(String name) {
        addTransformer(name, false);
    }

    private static void addTransformer(String name, boolean srg) {
        if (SKIPPED_TRANSFORMERS.contains(name)) return;
        try {
            IClassTransformer transformer = (IClassTransformer) newInstance(name);
            TRANSFORMERS.add(transformer);
            if (srg) SRG_TRANSFORMERS.add(transformer);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load Forge transformer " + name, e);
        }
    }

    private static final Set<String> MIXIN_CONFIGS = ConcurrentHashMap.newKeySet();

    public static void addMixinConfig(String name) {
        if (!MIXIN_CONFIGS.add(name)) return;
        Mixins.addConfiguration(name);
        try {
            for (Config config : Mixins.getConfigs()) {
                if (!config.getName().equals(name)) continue;
                IMixinConfig mixinConfig = config.getConfig();
                mixinConfig.decorate(FabricUtil.KEY_COMPATIBILITY, FabricUtil.COMPATIBILITY_0_17_1);
                Field priority = mixinConfig.getClass().getDeclaredField("mixinPriority");
                priority.setAccessible(true);
                if (priority.getInt(mixinConfig) == IMixinConfig.DEFAULT_PRIORITY) priority.setInt(mixinConfig, IMixinConfig.DEFAULT_PRIORITY - 1);
            }
            Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
            Field processorField = transformer.getClass().getDeclaredField("processor");
            processorField.setAccessible(true);
            Object processor = processorField.get(transformer);
            Field transformedCount = processor.getClass().getDeclaredField("transformedCount");
            transformedCount.setAccessible(true);
            transformedCount.setInt(processor, 0);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to select Forge mixin config " + name, e);
        }
    }

    private static void hookModClasses() throws ReflectiveOperationException {
        Field delegateField = FabricLauncherBase.getLauncher().getClass().getDeclaredField("classLoader");
        delegateField.setAccessible(true);
        Object delegate = delegateField.get(FabricLauncherBase.getLauncher());
        Field field = delegate.getClass().getDeclaredField("mixinTransformer");
        field.setAccessible(true);
        IMixinTransformer mixin = (IMixinTransformer) field.get(delegate);
        String fontRenderer = mappings().mcpToRuntime("net/minecraft/client/gui/FontRenderer").replace('/', '.');
        field.set(delegate, Proxy.newProxyInstance(IMixinTransformer.class.getClassLoader(), new Class<?>[]{IMixinTransformer.class}, (proxy, method, args) -> {
            if (method.getName().equals("transformClassBytes") && args[2] instanceof byte[] bytes) {
                if (FORGE_CLASSES.contains((String) args[0])) args[2] = transformModClass((String) args[1], bytes);
                else if (fontRenderer.equals(args[0])) args[2] = ForgePatches.applyFontRenderer(bytes);
            }
            try {
                return method.invoke(mixin, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }));
    }

    // ponytail: mod classes are passed as-is, without remapping their Minecraft references to notch names; remap if a coremod needs them
    private static final ThreadLocal<Boolean> TRANSFORMING = ThreadLocal.withInitial(() -> false);

    private static byte[] transformModClass(String name, byte[] bytes) {
        if (TRANSFORMING.get()) return bytes;
        TRANSFORMING.set(true);
        try {
            for (IClassTransformer transformer : TRANSFORMERS) {
                try {
                    bytes = transformer.transform(name, name, bytes);
                } catch (Throwable t) {
                    LOGGER.error("Forge transformer {} failed on {}", transformer.getClass().getName(), name, t);
                }
            }
        } finally {
            TRANSFORMING.set(false);
        }
        return bytes;
    }

    static boolean active() {
        return active;
    }

    static Mappings mappings() {
        return Mappings.get();
    }

    static byte[] transform(String intName, byte[] bytes) {
        Mappings mappings = mappings();
        String notch = mappings.intToNotch.getOrDefault(intName, intName);
        String mcp = mappings.intToMcp.getOrDefault(intName, intName);
        bytes = remap(bytes, mappings.toNotch, false, true);
        if (notch.equals(MINECRAFT)) bytes = addInitHook(bytes);
        boolean srg = false;
        boolean changed = false;
        for (IClassTransformer transformer : TRANSFORMERS) {
            if (SRG_TRANSFORMERS.contains(transformer) != srg) {
                srg = !srg;
                bytes = srg ? remap(remap(bytes, mappings.toInt, false, false), mappings.toSrg, false, false)
                        : remap(remap(bytes, mappings.fromSrg, false, false), mappings.toNotch, false, false);
            }
            try {
                byte[] out = transformer.transform(notch.replace('/', '.'), mcp.replace('/', '.'), bytes);
                if (out != bytes && !Arrays.equals(out, bytes)) changed = true;
                bytes = out;
            } catch (Throwable t) {
                LOGGER.error("Forge transformer {} failed on {}", transformer.getClass().getName(), mcp, t);
            }
        }
        if (!changed && !notch.equals(MINECRAFT)) return null;
        return remap(bytes, srg ? mappings.fromSrg : mappings.toInt, false, false);
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
        ForgeCompatCommand command = new ForgeCompatCommand();
        ClientCommandHandler.instance.register(command);
        MinecraftForge.EVENT_BUS.register(command);
        Map<String, Object> mods = new LinkedHashMap<>();
        MOD_CLASSES.forEach((name, modid) -> {
            try {
                injectProxies(Class.forName(name, false, ForgeCompat.class.getClassLoader()));
                Object mod = newInstance(name);
                INSTANCES.put(modid, mod);
                mods.put(name, mod);
            } catch (Throwable t) {
                LOGGER.error("Failed to construct Forge mod {}", name, t);
            }
        });
        mods.entrySet().removeIf(entry -> {
            try {
                injectInstances(entry.getValue(), MOD_CLASSES.get(entry.getKey()));
                fire(entry.getValue(), new FMLPreInitializationEvent(MOD_CLASSES.get(entry.getKey())));
                return false;
            } catch (Throwable t) {
                LOGGER.error("Failed to pre-initialize Forge mod {}", entry.getKey(), t);
                return true;
            }
        });
        mods.forEach((name, mod) -> {
            long start = System.nanoTime();
            try {
                fire(mod, new FMLInitializationEvent());
            } catch (Throwable t) {
                LOGGER.error("Failed to initialize Forge mod {}", name, t);
            }
            LOGGER.info("Initialized Forge mod {} in {} ms", name, (System.nanoTime() - start) / 1_000_000);
        });
        LOADED_MODS.putAll(mods);
        fireAll(new net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent());
        fireAll(new FMLPostInitializationEvent());
        fireAll(new FMLLoadCompleteEvent());
        RavenHooks.modsInitialized();
    }

    public static boolean modsInitialized() {
        return modsInitialized;
    }

    public static void fireAll(Object event) {
        LOADED_MODS.forEach((name, mod) -> {
            try {
                fire(mod, event);
            } catch (Throwable t) {
                LOGGER.error("Forge mod {} failed on {}", name, event.getClass().getSimpleName(), t);
            }
        });
    }

    private static void injectProxies(Class<?> type) throws ReflectiveOperationException {
        for (Field field : type.getDeclaredFields()) {
            SidedProxy proxy = field.getAnnotation(SidedProxy.class);
            if (proxy == null || !Modifier.isStatic(field.getModifiers())) continue;
            String name = proxy.clientSide().isEmpty() ? field.getType().getName() : proxy.clientSide();
            field.setAccessible(true);
            field.set(null, newInstance(name));
        }
    }

    private static void injectInstances(Object mod, String modid) throws ReflectiveOperationException {
        for (Field field : mod.getClass().getDeclaredFields()) {
            Mod.Instance annotation = field.getAnnotation(Mod.Instance.class);
            if (annotation == null) continue;
            Object instance = INSTANCES.get(annotation.value().isEmpty() ? modid : annotation.value());
            if (instance == null) continue;
            field.setAccessible(true);
            field.set(Modifier.isStatic(field.getModifiers()) ? null : mod, instance);
        }
    }

    private static void fire(Object mod, Object event) throws ReflectiveOperationException {
        for (Method method : declaredMethods(mod.getClass())) {
            Class<?>[] params = method.getParameterTypes();
            if (method.isAnnotationPresent(Mod.EventHandler.class) && params.length == 1 && params[0] == event.getClass()) {
                method.setAccessible(true);
                method.invoke(mod, event);
            }
        }
    }

    private static final Map<Class<?>, Method[]> DECLARED = new ConcurrentHashMap<>();

    private static Method[] declaredMethods(Class<?> type) {
        return DECLARED.computeIfAbsent(type, Class::getDeclaredMethods);
    }

    private static Object newInstance(String name) throws ReflectiveOperationException {
        Class<?> type = Class.forName(name, true, ForgeCompat.class.getClassLoader());
        for (String holder : new String[]{"INSTANCE", "MODULE$"}) try {
            Field instance = type.getDeclaredField(holder);
            if (Modifier.isStatic(instance.getModifiers()) && instance.getType() == type) {
                instance.setAccessible(true);
                Object value = instance.get(null);
                if (value != null) return value;
            }
        } catch (NoSuchFieldException ignored) {
        }
        java.lang.reflect.Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    static void scanMods(ZipFile zip, Path source) throws IOException {
        for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements(); ) {
            ZipEntry entry = it.nextElement();
            if (!entry.getName().endsWith(".class")) continue;
            addForgeClass(entry.getName());
            try (InputStream in = zip.getInputStream(entry)) {
                scanMod(new ClassReader(in), source);
            }
        }
    }

    private static void addForgeClass(String path) {
        FORGE_CLASSES.add(path.substring(0, path.length() - ".class".length()).replace('/', '.'));
    }

    private static void scanMod(ClassReader reader, Path source) {
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                String className = reader.getClassName().replace('/', '.');
                if (!desc.equals(MOD_DESC)) {
                    Map<String, Object> info = new HashMap<>();
                    ASM_DATA.addASMData(Type.getType(desc).getClassName(), className, className, info);
                    return new AnnotationVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(String key, Object value) {
                            info.put(key, value);
                        }
                    };
                }
                return new AnnotationVisitor(Opcodes.ASM9) {
                    private String modid;
                    private String name = "";
                    private String version = "";
                    private String guiFactory = "";

                    @Override
                    public void visit(String key, Object value) {
                        if (key.equals("modid")) modid = (String) value;
                        else if (key.equals("name")) name = (String) value;
                        else if (key.equals("version")) version = (String) value;
                        else if (key.equals("guiFactory")) guiFactory = (String) value;
                    }

                    @Override
                    public void visitEnd() {
                        if (modid == null) return;
                        if (!guiFactory.isEmpty()) GUI_FACTORIES.put(modid, guiFactory);
                        MOD_CLASSES.put(reader.getClassName().replace('/', '.'), modid);
                        MODS.put(modid, new Loader.Container(modid, name.isEmpty() ? modid : name, version, source.toFile()));
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
                || name.startsWith("org/spongepowered/") || name.startsWith("org/objectweb/asm/") || name.startsWith("META-INF/services/org.spongepowered.") || name.equals("META-INF/services/javax.annotation.processing.Processor"));
        mappings.modClasses.clear();
        mappings.mixinTargets.clear();
        mappings.capturedFields.clear();
        mappings.uniqueMethods.clear();
        mappings.shadowMembers.clear();
        mappings.refmapKeys.clear();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (!isRefmap(entry.getKey())) continue;
            Matcher matcher = REFMAP_KEY.matcher(new String(entry.getValue(), StandardCharsets.UTF_8));
            while (matcher.find()) mappings.refmapKeys.add(matcher.group(1));
        }
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (!entry.getKey().endsWith(".class")) continue;
            addForgeClass(entry.getKey());
            Set<String> replaced = REPLACED_OVERWRITES.get(entry.getKey());
            if (replaced != null) entry.setValue(stripMethods(entry.getValue(), replaced));
            entry.setValue(patchEventRegister(entry.getValue()));
            ClassReader reader = new ClassReader(entry.getValue());
            mappings.modClasses.put(reader.getClassName(), new Mappings.ClassInfo(reader));
            scanMod(reader, source);
            scanMixin(reader, mappings);
        }
        Path tmp = out.resolveSibling(out.getFileName() + ".tmp");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmp))) {
            Set<String> dirs = new java.util.TreeSet<>();
            for (String name : entries.keySet()) {
                for (int i = name.indexOf('/'); i > 0; i = name.indexOf('/', i + 1)) dirs.add(name.substring(0, i + 1));
            }
            for (String dir : dirs) {
                if (entries.containsKey(dir)) continue;
                zos.putNextEntry(new ZipEntry(dir));
                zos.closeEntry();
            }
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                byte[] bytes = entry.getValue();
                if (entry.getKey().endsWith(".class")) bytes = remap(bytes, mappings.toInt, true, false);
                else if (isRefmap(entry.getKey())) bytes = remapRefmap(bytes, mappings);
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(bytes);
                zos.closeEntry();
            }
        }
        Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
        Files.setLastModifiedTime(out, mtime);
    }

    private static boolean isRefmap(String name) {
        return name.endsWith(".json") && name.substring(name.lastIndexOf('/') + 1).contains("refmap");
    }

    private static byte[] stripMethods(byte[] bytes, Set<String> names) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        node.methods.removeIf(method -> names.contains(method.name));
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] patchEventRegister(byte[] bytes) {
        if (!new String(bytes, StandardCharsets.ISO_8859_1).contains(LISTENER_LIST)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        node.methods.removeIf(method -> Arrays.stream(method.instructions.toArray()).anyMatch(insn -> insn instanceof MethodInsnNode call && call.owner.equals(LISTENER_LIST)));
        for (MethodNode method : node.methods) {
            if ((method.access & Opcodes.ACC_STATIC) == 0 || !method.desc.equals("(L" + EVENT_BUS + ";Ljava/lang/Object;)V")) continue;
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            method.localVariables = null;
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT_BUS, "register", "(Ljava/lang/Object;)V", false));
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            method.maxStack = 2;
            method.maxLocals = 2;
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static void scanMixin(ClassReader reader, Mappings mappings) {
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.SKIP_CODE);
        if (node.invisibleAnnotations == null) return;
        for (AnnotationNode annotation : node.invisibleAnnotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") || annotation.values == null) continue;
            for (Object value : annotation.values) {
                if (value instanceof List<?> list && !list.isEmpty()) {
                    mappings.mixinTargets.put(node.name, list.get(0) instanceof Type type ? type.getInternalName() : ((String) list.get(0)).replace('.', '/'));
                }
            }
        }
        if (mappings.mixinTargets.containsKey(node.name)) {
            for (MethodNode method : node.methods) {
                if (isShadow(method.visibleAnnotations) || isShadow(method.invisibleAnnotations)) mappings.shadowMembers.add(node.name + "/" + method.name);
                if (!method.name.startsWith("<") && !isMixinMember(method.visibleAnnotations) && !isMixinMember(method.invisibleAnnotations)) {
                    mappings.uniqueMethods.add(node.name + "/" + method.name);
                }
            }
        }
        for (FieldNode field : node.fields) {
            if (mappings.mixinTargets.containsKey(node.name) && isShadow(field.visibleAnnotations)) mappings.shadowMembers.add(node.name + "/" + field.name);
            if (field.visibleAnnotations == null) continue;
            for (AnnotationNode annotation : field.visibleAnnotations) {
                if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;") && annotation.values != null && annotation.values.toString().contains("val$")) {
                    mappings.capturedFields.add(node.name + "/" + field.name);
                }
            }
        }
    }

    private static boolean isShadow(List<AnnotationNode> annotations) {
        return annotations != null && annotations.stream().anyMatch(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;") || a.desc.equals("Lorg/spongepowered/asm/mixin/Overwrite;"));
    }

    private static boolean isMixinMember(List<AnnotationNode> annotations) {
        return annotations != null && annotations.stream().anyMatch(annotation -> annotation.desc.startsWith("Lorg/spongepowered/") && !annotation.desc.equals("Lorg/spongepowered/asm/mixin/Unique;"));
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
        new ClassReader(bytes).accept(new ClassRemapper(dropDuplicateMethods(next), remapper), 0);
        return writer.toByteArray();
    }

    private static ClassVisitor dropDuplicateMethods(ClassVisitor next) {
        return new ClassVisitor(Opcodes.ASM9, next) {
            private final Set<String> seen = new java.util.HashSet<>();

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return seen.add(name + desc) ? super.visitMethod(access, name, desc, signature, exceptions) : null;
            }
        };
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
                // the vanilla call stays in place so Fabric mixins can target it, see GameRendererMixin
                if (FORGE_DRAW_SCREEN.equals(value)) {
                    value = "L" + mappings().mcpToRuntime("net/minecraft/client/gui/GuiScreen") + ";" + mappings().srgToRuntime("func_73863_a") + "(IIF)V";
                } else if (FORGE_FONT_COLOR.equals(value)) {
                    value = "L" + mappings().mcpToRuntime("net/minecraft/client/renderer/GlStateManager") + ";" + mappings().srgToRuntime("func_179131_c") + "(FFFF)V";
                } else if (FORGE_ITEM_STACK.equals(value)) {
                    value = "<init>(L" + mappings().mcpToRuntime("net/minecraft/item/Item") + ";II)V";
                } else if (value instanceof String s && (s.contains("net/minecraftforge/") || s.contains("net/minecraft/") && !mappings().refmapKeys.contains(s))) {
                    // targets on Forge-added members are left out of refmaps, so their MCP class names are mapped here
                    Matcher matcher = DESC_CLASS.matcher(s);
                    StringBuilder out = new StringBuilder();
                    while (matcher.find()) {
                        String mapped = mappings().mcpToRuntime(matcher.group(1));
                        matcher.appendReplacement(out, Matcher.quoteReplacement("L" + (mapped == null ? matcher.group(1) : mapped) + ";"));
                    }
                    value = SRG_NAME.matcher(matcher.appendTail(out).toString()).replaceAll(m -> {
                        String mapped = mappings().srgToRuntime(m.group());
                        return Matcher.quoteReplacement(mapped == null ? m.group() : mapped);
                    });
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

    private static final Set<String> DROPPED_MIXIN_CALLS = Set.of(
            "org/spongepowered/asm/launch/MixinBootstrap.init", "org/spongepowered/asm/launch/platform/MixinPlatformManager.addContainer",
            "org/spongepowered/asm/mixin/MixinEnvironment.setObfuscationContext", "org/spongepowered/asm/mixin/MixinEnvironment.setSide"
    );

    private static Type[] reverse(Type[] types) {
        Type[] out = new Type[types.length];
        for (int i = 0; i < types.length; i++) out[i] = types[types.length - 1 - i];
        return out;
    }

    private static final class HookVisitor extends ClassVisitor {
        private final String handshake = mappings().mcpToRuntime("net/minecraft/network/handshake/client/C00Handshake");
        private final String handshakeDesc = "(ILjava/lang/String;IL" + mappings().mcpToRuntime("net/minecraft/network/EnumConnectionState") + ";";
        private final String simpleResourceManager = mappings().mcpToRuntime("net/minecraft/client/resources/SimpleReloadableResourceManager");
        private final String reloadableResourceManager = mappings().mcpToRuntime("net/minecraft/client/resources/IReloadableResourceManager");
        private final String spawnEgg = mappings().mcpToRuntime("net/minecraft/item/ItemMonsterPlacer");
        private final String creativeTab = mappings().mcpToRuntime("net/minecraft/creativetab/CreativeTabs");

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
            if (mixinTarget == null || !owner.equals(this.name) || !name.startsWith("this$") && !mappings().capturedFields.contains(owner + "/" + name)) return name;
            String mapped = mappings().fieldByDesc(mixinTarget, desc);
            return mapped == null ? name : mapped;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            name = outerInstance(this.name, name, desc);
            if (mixinTarget != null && value == null && (access & Opcodes.ACC_STATIC) != 0 && mappings().fieldDesc(mixinTarget, name) == null) access &= ~Opcodes.ACC_FINAL;
            return super.visitField(access, name, desc, signature, value);
        }

        private String unique(String owner, String name, String desc) {
            if (mixinTarget == null || !owner.equals(this.name) || !mappings().uniqueMethods.contains(owner + "/" + name) || !mappings().hasMethod(mixinTarget, name, desc)) return name;
            return name + "$forgecompat";
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            name = unique(this.name, name, desc);
            if (mixinTarget == null) return hooks(access, name, desc, signature, exceptions);
            return new MethodNode(Opcodes.ASM9, access, name, desc, signature, exceptions) {
                @Override
                public void visitEnd() {
                    if ((access & Opcodes.ACC_PRIVATE) == 0 && (isOverwrite(visibleAnnotations) || isOverwrite(invisibleAnnotations))) {
                        access = access & ~Opcodes.ACC_PROTECTED | Opcodes.ACC_PUBLIC;
                    }
                    accept(hooks(access, name, desc, signature, exceptions.toArray(new String[0])));
                }
            };
        }

        private static boolean isOverwrite(List<AnnotationNode> annotations) {
            return annotations != null && annotations.stream().anyMatch(annotation -> annotation.desc.equals("Lorg/spongepowered/asm/mixin/Overwrite;"));
        }

        private MethodVisitor hooks(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                private int extraStack;

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    AnnotationVisitor next = super.visitAnnotation(desc, visible);
                    return desc.startsWith("Lorg/spongepowered/") || desc.startsWith("Lcom/llamalad7/") ? forgeTargets(next) : next;
                }

                @Override
                public void visitMaxs(int maxStack, int maxLocals) {
                    super.visitMaxs(maxStack + extraStack, maxLocals);
                }

                private void pushDefault(Type type) {
                    switch (type.getSort()) {
                        case Type.VOID -> {
                        }
                        case Type.LONG -> super.visitInsn(Opcodes.LCONST_0);
                        case Type.FLOAT -> super.visitInsn(Opcodes.FCONST_0);
                        case Type.DOUBLE -> super.visitInsn(Opcodes.DCONST_0);
                        case Type.OBJECT, Type.ARRAY -> super.visitInsn(Opcodes.ACONST_NULL);
                        default -> super.visitInsn(Opcodes.ICONST_0);
                    }
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (opcode == Opcodes.GETFIELD && name.equals("eyeHeight") && desc.equals("F") && mappings().intToNotch.containsKey(owner)) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "eyeHeight", "(Ljava/lang/Object;)F", false);
                        return;
                    }
                    if (opcode == Opcodes.GETFIELD && name.equals("delegate") && desc.equals("Lnet/minecraftforge/fml/common/registry/RegistryDelegate;")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "delegate", "(Ljava/lang/Object;)" + desc, false);
                        return;
                    }
                    super.visitFieldInsn(opcode, owner, outerInstance(owner, name, desc), desc);
                }

                @Override
                public void visitTypeInsn(int opcode, String type) {
                    super.visitTypeInsn(opcode, opcode == Opcodes.CHECKCAST && type.equals(simpleResourceManager) ? reloadableResourceManager : type);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    name = unique(owner, name, desc);
                    if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals(simpleResourceManager)) {
                        super.visitMethodInsn(Opcodes.INVOKEINTERFACE, reloadableResourceManager, name, desc, true);
                    } else if (opcode == Opcodes.INVOKESPECIAL && owner.equals(handshake) && desc.equals(handshakeDesc + "Z)V")) {
                        super.visitInsn(Opcodes.POP);
                        super.visitMethodInsn(opcode, owner, name, handshakeDesc + ")V", itf);
                    } else if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/Class") && name.equals("forName")) {
                        super.visitMethodInsn(opcode, HOOKS, name, desc, false);
                    } else if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/lang/Class") && name.equals("getDeclaredField")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "getDeclaredField", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
                    } else if ((opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) && mappings().intToNotch.containsKey(owner) && Hooks.FORGE_METHODS.contains(name + "/" + Type.getArgumentCount(desc))
                            && !desc.startsWith("(I)")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, name, "(Ljava/lang/Object;" + desc.substring(1), false);
                    } else if (opcode == Opcodes.INVOKESPECIAL && owner.equals(creativeTab) && name.equals("<init>") && desc.equals("(Ljava/lang/String;)V")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, "nextCreativeTabId", "()I", false);
                        super.visitInsn(Opcodes.SWAP);
                        super.visitMethodInsn(opcode, owner, name, "(ILjava/lang/String;)V", itf);
                        extraStack = 1;
                    } else if (owner.equals("org/spongepowered/asm/mixin/Mixins") && name.equals("addConfiguration") && desc.equals("(Ljava/lang/String;)V")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/polyfrost/forgecompat/ForgeCompat", "addMixinConfig", desc, false);
                    } else if (DROPPED_MIXIN_CALLS.contains(owner + "." + name)) {
                        for (Type arg : reverse(Type.getArgumentTypes(desc))) super.visitInsn(arg.getSize() == 2 ? Opcodes.POP2 : Opcodes.POP);
                        if (opcode != Opcodes.INVOKESTATIC) super.visitInsn(Opcodes.POP);
                        pushDefault(Type.getReturnType(desc));
                    } else if (owner.equals("org/apache/logging/log4j/spi/LoggerContext") && name.equals("getLogger") && desc.endsWith(")Lorg/apache/logging/log4j/Logger;")) {
                        super.visitMethodInsn(opcode, owner, name, desc.replace(")Lorg/apache/logging/log4j/Logger;", ")Lorg/apache/logging/log4j/spi/ExtendedLogger;"), itf);
                    } else if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/security/ProtectionDomain") && name.equals("getCodeSource")) {
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, HOOKS, name, "(Ljava/security/ProtectionDomain;)Ljava/security/CodeSource;", false);
                    } else if (opcode == Opcodes.INVOKESTATIC && owner.equals("javax/swing/UIManager") && name.equals("setLookAndFeel")) {
                        super.visitMethodInsn(opcode, HOOKS, name, desc, false);
                    } else if (opcode == Opcodes.INVOKESTATIC && owner.equals(spawnEgg) && name.equals("getEntityName")) {
                        super.visitMethodInsn(opcode, HOOKS, name, desc, false);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }
                }
            };
        }
    }
}
