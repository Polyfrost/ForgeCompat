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
    private final Map<String, String> mcpFields = new HashMap<>();
    // "owner/name desc" -> name
    private final Map<String, String> notchMethods = new HashMap<>();
    private final Map<String, String> notchFields = new HashMap<>();
    private final Map<String, String> intMethods = new HashMap<>();
    private final Map<String, String> intFields = new HashMap<>();
    /** classes of the Forge jar being remapped, before remapping */
    final Map<String, ClassInfo> modClasses = new ConcurrentHashMap<>();
    private final Map<String, ClassInfo> intInfoCache = new ConcurrentHashMap<>();

    final Remapper toInt;
    final Remapper toNotch;

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
        for (String[] p : members) {
            String owner = notchToIntermediary.getOrDefault(p[1], p[1]).replace('/', '.');
            String intOwner = notchToInt.getOrDefault(p[1], p[1]);
            if (p[0].equals("M")) {
                String name = identity ? p[4] : resolver.mapMethodName("intermediary", owner, p[4], intermediaryDescs.mapMethodDesc(p[3]));
                notchMethods.put(p[1] + "/" + p[2] + p[3], name);
                intMethods.put(intOwner + "/" + name + runtimeDescs.mapMethodDesc(p[3]), p[2]);
                if (p[5].startsWith("func_")) srgToInt.put(p[5], name);
            } else {
                String name = identity ? p[4] : resolver.mapFieldName("intermediary", owner, p[4], intermediaryDescs.mapDesc(p[3]));
                notchFields.put(p[1] + "/" + p[2] + p[3], name);
                intFields.put(intOwner + "/" + name + runtimeDescs.mapDesc(p[3]), p[2]);
                if (p[5].startsWith("field_")) srgToInt.put(p[5], name);
                else if (!p[5].equals("-")) mcpFields.put(notchToMcp.getOrDefault(p[1], p[1]) + "/" + p[5], name);
            }
        }
        toInt = new HierarchyRemapper(notchToInt, notchMethods, notchFields, this::notchInfo, true);
        toNotch = new HierarchyRemapper(intToNotch, intMethods, intFields, this::intInfo, false);
    }

    String mcpToRuntime(String mcpName) {
        return mcpToInt.get(mcpName);
    }

    String srgToRuntime(String srg) {
        return srgToInt.get(srg);
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
            return mod != null ? mod : intInfo(name);
        }
        ClassInfo info = intInfo(intName);
        if (info == null) return null;
        String[] interfaces = new String[info.interfaces.length];
        for (int i = 0; i < interfaces.length; i++) interfaces[i] = intToNotch.getOrDefault(info.interfaces[i], info.interfaces[i]);
        return new ClassInfo(info.access, info.superName == null ? null : intToNotch.getOrDefault(info.superName, info.superName), interfaces);
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

        boolean isInterface() {
            return (access & Opcodes.ACC_INTERFACE) != 0;
        }
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
            return mcpToInt.get(type);
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String srg = toIntermediary ? srgToInt.get(name) : null;
            if (srg != null) return srg;
            String mapped = find(methods, owner, "/" + name + desc);
            return mapped == null ? name : mapped;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String srg = toIntermediary ? srgToInt.getOrDefault(name, mcpFields.get(owner + "/" + name)) : null;
            if (srg != null) return srg;
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
