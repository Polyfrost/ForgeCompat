package org.polyfrost.forgecompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

final class Mappings {
    final Map<String, String> notchToInt = new HashMap<>();
    final Map<String, String> intToNotch = new HashMap<>();
    final Map<String, String> intToMcp = new HashMap<>();
    private final Map<String, String> mcpToInt = new HashMap<>();
    private final Map<String, String> srgToInt = new HashMap<>();
    /** [runtime owner/]srg name + runtime desc -> name, as a srg name can cover several unrelated methods */
    private final Map<String, String> srgMethods = new HashMap<>();
    private final Map<String, String> mcpFields = new HashMap<>();
    // "owner/name desc" -> name
    private final Map<String, String> notchMethods = new HashMap<>();
    private final Map<String, String> notchFields = new HashMap<>();
    private final Map<String, String> intMethods = new HashMap<>();
    private final Map<String, String> intFields = new HashMap<>();
    private final Map<String, String> fieldDescs = new HashMap<>();
    /** classes of the Forge jar being remapped, before remapping */
    final Map<String, ClassInfo> modClasses = new ConcurrentHashMap<>();
    final Map<String, String> mixinTargets = new ConcurrentHashMap<>();
    final Set<String> capturedFields = ConcurrentHashMap.newKeySet();
    final Set<String> uniqueMethods = ConcurrentHashMap.newKeySet();
    final Set<String> refmapKeys = ConcurrentHashMap.newKeySet();
    final Set<String> shadowMembers = ConcurrentHashMap.newKeySet();
    private Map<String, List<String>> mcpToSrg;
    private final Map<String, ClassInfo> intInfoCache = new ConcurrentHashMap<>();

    private static Mappings instance;

    final Remapper toInt;
    final Remapper toNotch;
    final Remapper toSrg;
    final Remapper fromSrg;

    static synchronized Mappings get() {
        if (instance == null) {
            long start = System.nanoTime();
            try (InputStream in = Mappings.class.getResourceAsStream("/forgecompat_mappings.gz")) {
                instance = new Mappings(in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load ForgeCompat mappings", e);
            }
            LogManager.getLogger("ForgeCompat").info("Loaded mappings in {} ms", (System.nanoTime() - start) / 1_000_000);
        }
        return instance;
    }

    Mappings(InputStream in) throws IOException {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        boolean identity = resolver.getCurrentRuntimeNamespace().equals("intermediary");
        Map<String, String> notchToIntermediary = new HashMap<>();
        Map<String, String> notchToMcp = new HashMap<>();
        List<String[]> members = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\t");
                if (p[0].equals("C")) {
                    String runtime = identity ? p[2] : resolver.mapClassName("intermediary", p[2].replace('/', '.')).replace('.', '/');
                    notchToIntermediary.put(p[1], p[2]);
                    notchToMcp.put(p[1], p[3]);
                    notchToInt.put(p[1], runtime);
                    intToNotch.put(runtime, p[1]);
                    mcpToInt.put(p[3], runtime);
                    intToMcp.put(runtime, p[3]);
                } else {
                    members.add(p);
                }
            }
        }
        SimpleRemapper intermediaryDescs = new SimpleRemapper(notchToIntermediary);
        SimpleRemapper runtimeDescs = new SimpleRemapper(notchToInt);
        SimpleRemapper mcpDescs = new SimpleRemapper(notchToMcp);
        Map<String, String> toSrgMethods = new HashMap<>();
        Map<String, String> toSrgFields = new HashMap<>();
        Map<String, String> fromSrgMethods = new HashMap<>();
        Map<String, String> fromSrgFields = new HashMap<>();
        for (String[] p : members) {
            String owner = notchToIntermediary.getOrDefault(p[1], p[1]).replace('/', '.');
            String intOwner = notchToInt.getOrDefault(p[1], p[1]);
            String mcpOwner = notchToMcp.getOrDefault(p[1], p[1]);
            if (p[0].equals("M")) {
                String name = identity ? p[4] : resolver.mapMethodName("intermediary", owner, p[4], intermediaryDescs.mapMethodDesc(p[3]));
                String runtimeDesc = runtimeDescs.mapMethodDesc(p[3]);
                notchMethods.put(p[1] + "/" + p[2] + p[3], name);
                intMethods.put(intOwner + "/" + name + runtimeDesc, p[2]);
                if (!p[5].equals("-")) {
                    toSrgMethods.put(intOwner + "/" + name + runtimeDesc, p[5]);
                    fromSrgMethods.put(mcpOwner + "/" + p[5] + mcpDescs.mapMethodDesc(p[3]), name);
                }
                if (p[5].startsWith("func_")) {
                    srgToInt.put(p[5], name);
                    srgMethods.put(p[5] + runtimeDesc, name);
                    srgMethods.put(intOwner + "/" + p[5] + runtimeDesc, name);
                }
            } else {
                String name = identity ? p[4] : resolver.mapFieldName("intermediary", owner, p[4], intermediaryDescs.mapDesc(p[3]));
                notchFields.put(p[1] + "/" + p[2] + p[3], name);
                intFields.put(intOwner + "/" + name + runtimeDescs.mapDesc(p[3]), p[2]);
                fieldDescs.put(intOwner + "/" + name, runtimeDescs.mapDesc(p[3]));
                if (!p[5].equals("-")) {
                    toSrgFields.put(intOwner + "/" + name + runtimeDescs.mapDesc(p[3]), p[5]);
                    fromSrgFields.put(mcpOwner + "/" + p[5] + mcpDescs.mapDesc(p[3]), name);
                }
                if (p[5].startsWith("field_")) srgToInt.put(p[5], name);
                else if (!p[5].equals("-")) mcpFields.put(notchToMcp.getOrDefault(p[1], p[1]) + "/" + p[5], name);
            }
        }
        toInt = new HierarchyRemapper(notchToInt, notchMethods, notchFields, this::notchInfo, true);
        toNotch = new HierarchyRemapper(intToNotch, intMethods, intFields, this::intInfo, false);
        toSrg = new HierarchyRemapper(intToMcp, toSrgMethods, toSrgFields, this::intInfo, false);
        fromSrg = new HierarchyRemapper(mcpToInt, fromSrgMethods, fromSrgFields, this::mcpInfo, false);
    }

    String mcpToRuntime(String mcpName) {
        return mcpToInt.get(mcpName);
    }

    String srgToRuntime(String srg) {
        return srgToInt.get(srg);
    }

    boolean hasMethod(String owner, String name, String desc) {
        return intMethods.containsKey(owner + "/" + name + desc);
    }

    String fieldDesc(String owner, String name) {
        return fieldDescs.get(owner + "/" + name);
    }

    String fieldByDesc(String owner, String desc) {
        String found = null;
        for (String key : intFields.keySet()) {
            if (!key.startsWith(owner) || key.charAt(owner.length()) != '/' || !key.endsWith(desc)) continue;
            String name = key.substring(owner.length() + 1, key.length() - desc.length());
            if (name.indexOf('/') >= 0) continue;
            if (found != null) return null;
            found = name;
        }
        return found;
    }

    String mapReference(String ref) {
        int semi = ref.startsWith("L") ? ref.indexOf(';') : -1;
        String owner = semi < 0 ? "" : ref.substring(1, semi);
        String member = ref.substring(semi + 1);
        int paren = member.indexOf('(');
        int colon = member.indexOf(':');
        if (semi < 0 && paren < 0 && colon < 0) return toInt.mapType(ref);
        String mapped;
        if (paren >= 0) {
            String desc = member.substring(paren);
            mapped = toInt.mapMethodName(owner, member.substring(0, paren), desc) + toInt.mapMethodDesc(desc);
        } else if (colon >= 0) {
            String desc = member.substring(colon + 1);
            mapped = toInt.mapFieldName(owner, member.substring(0, colon), desc) + ":" + toInt.mapDesc(desc);
        } else {
            mapped = toInt.mapFieldName(owner, member, null);
        }
        return semi < 0 ? mapped : "L" + toInt.mapType(owner) + ";" + mapped;
    }

    ClassInfo intInfo(String name) {
        ClassInfo info = intInfoCache.get(name);
        if (info == null) {
            try (InputStream in = Mappings.class.getClassLoader().getResourceAsStream(name + ".class")) {
                info = in == null ? ClassInfo.MISSING : new ClassInfo(new ClassReader(in));
            } catch (IOException e) {
                info = ClassInfo.MISSING;
            }
            intInfoCache.put(name, info);
        }
        return info == ClassInfo.MISSING ? null : info;
    }

    ClassInfo notchInfo(String name) {
        String intName = notchToInt.get(name);
        if (intName == null) {
            ClassInfo mod = modClasses.get(name);
            if (mod != null) return mod;
            ClassInfo stub = intInfo(name);
            return stub == null ? null : stub.map(intToNotch);
        }
        ClassInfo info = intInfo(intName);
        return info == null ? null : info.map(intToNotch);
    }

    private ClassInfo mcpInfo(String name) {
        ClassInfo info = intInfo(mcpToInt.getOrDefault(name, name));
        return info == null ? null : info.map(intToMcp);
    }

    String commonSuperClass(String a, String b) {
        ClassInfo infoA = notchInfo(a);
        ClassInfo infoB = notchInfo(b);
        if (infoA == null || infoB == null || infoA.isInterface() || infoB.isInterface()) return "java/lang/Object";
        Set<String> ancestors = new HashSet<>();
        for (String c = a; c != null; c = superOf(c)) ancestors.add(c);
        for (String c = b; c != null; c = superOf(c)) if (ancestors.contains(c)) return c;
        return "java/lang/Object";
    }

    private String superOf(String name) {
        ClassInfo info = notchInfo(name);
        return info == null ? null : info.superName;
    }

    static final class ClassInfo {
        static final ClassInfo MISSING = new ClassInfo(0, null, new String[0]);

        final int access;
        final String superName;
        final String[] interfaces;

        ClassInfo(int access, String superName, String[] interfaces) {
            this.access = access;
            this.superName = superName;
            this.interfaces = interfaces;
        }

        ClassInfo(ClassReader reader) {
            this(reader.getAccess(), reader.getSuperName(), reader.getInterfaces());
        }

        ClassInfo map(Map<String, String> names) {
            String[] mapped = new String[interfaces.length];
            for (int i = 0; i < mapped.length; i++) mapped[i] = names.getOrDefault(interfaces[i], interfaces[i]);
            return new ClassInfo(access, superName == null ? null : names.getOrDefault(superName, superName), mapped);
        }

        boolean isInterface() {
            return (access & Opcodes.ACC_INTERFACE) != 0;
        }
    }

    private synchronized List<String> mcpToSrg(String mcp) {
        if (mcpToSrg == null) {
            mcpToSrg = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Mappings.class.getResourceAsStream("/forgecompat_mcp.gz")), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int tab = line.indexOf('\t');
                    mcpToSrg.computeIfAbsent(line.substring(tab + 1), k -> new ArrayList<>()).add(line.substring(0, tab));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load MCP names", e);
            }
        }
        return mcpToSrg.getOrDefault(mcp, List.of());
    }

    private final class HierarchyRemapper extends Remapper {
        private final Map<String, String> classes;
        private final Map<String, String> methods;
        private final Map<String, String> fields;
        private final Function<String, ClassInfo> info;
        private final boolean toIntermediary;
        private final Map<String, String> cache = new ConcurrentHashMap<>();

        HierarchyRemapper(Map<String, String> classes, Map<String, String> methods, Map<String, String> fields, Function<String, ClassInfo> info, boolean toIntermediary) {
            this.classes = classes;
            this.methods = methods;
            this.fields = fields;
            this.info = info;
            this.toIntermediary = toIntermediary;
        }

        @Override
        public String map(String type) {
            String mapped = classes.get(type);
            if (mapped != null || !toIntermediary) return mapped;
            // frame computation in Forge transformers has to resolve notch names, see CompatClassWriter
            if (type.equals("org/objectweb/asm/ClassWriter")) return "org/polyfrost/forgecompat/CompatClassWriter";
            // Mixin 0.7 shades ASM there, so mixin config plugins of Forge mods name its classes
            if (type.startsWith("org/spongepowered/asm/lib/")) return "org/objectweb/asm/" + type.substring("org/spongepowered/asm/lib/".length());
            if (type.startsWith("gg/essential/lib/mixinextras/")) return "com/llamalad7/mixinextras/" + type.substring("gg/essential/lib/mixinextras/".length());
            return mcpToInt.get(type);
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            if (toIntermediary && srgToInt.containsKey(name)) {
                String runtimeDesc = mapMethodDesc(desc);
                String srg = srgMethods.get(map(mixinTargets.getOrDefault(owner, owner)) + "/" + name + runtimeDesc);
                return srg != null ? srg : srgMethods.getOrDefault(name + runtimeDesc, srgToInt.get(name));
            }
            if (toIntermediary && shadowMembers.contains(owner + "/" + name)) {
                String runtimeDesc = mapMethodDesc(desc);
                String target = map(mixinTargets.get(owner));
                for (String srg : mcpToSrg(name)) {
                    String mapped = srgMethods.get(target + "/" + srg + runtimeDesc);
                    if (mapped != null) return mapped;
                }
                for (String srg : mcpToSrg(name)) {
                    String mapped = srgMethods.get(srg + runtimeDesc);
                    if (mapped != null) return mapped;
                }
            }
            String mapped = find(methods, owner, "/" + name + desc);
            return mapped == null ? name : mapped;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String srg = toIntermediary ? srgToInt.getOrDefault(name, mcpFields.get(owner + "/" + name)) : null;
            if (srg != null) return srg;
            if (toIntermediary && shadowMembers.contains(owner + "/" + name)) {
                String target = map(mixinTargets.get(owner));
                String runtimeDesc = mapDesc(desc);
                for (String candidate : mcpToSrg(name)) {
                    String mapped = srgToInt.get(candidate);
                    if (mapped != null && intFields.containsKey(target + "/" + mapped + runtimeDesc)) return mapped;
                }
            }
            String mapped = find(fields, owner, "/" + name + desc);
            return mapped == null ? name : mapped;
        }

        @Override
        public Object mapValue(Object value) {
            // reflection by srg name, e.g. getDeclaredField("field_110446_Y")
            if (toIntermediary && value instanceof String) {
                String srg = srgToInt.get(value);
                if (srg != null) return srg;
            }
            return super.mapValue(value);
        }

        private String find(Map<String, String> table, String owner, String member) {
            if (owner.startsWith("[") || owner.startsWith("java/")) return null;
            String key = owner + member;
            String cached = cache.get(key);
            if (cached != null) return cached.isEmpty() ? null : cached;
            String mapped = table.get(key);
            ClassInfo ci = mapped == null ? info.apply(owner) : null;
            if (ci != null) {
                if (ci.superName != null) mapped = find(table, ci.superName, member);
                for (int i = 0; mapped == null && i < ci.interfaces.length; i++) mapped = find(table, ci.interfaces[i], member);
            }
            cache.put(key, mapped == null ? "" : mapped);
            return mapped;
        }
    }
}
