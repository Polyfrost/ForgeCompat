package net.minecraftforge.fml.common.discovery;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.polyfrost.forgecompat.ForgeCompat;

public class ASMDataTable {
    private final Map<String, Set<ASMData>> data = new ConcurrentHashMap<>();

    public static ASMDataTable get() {
        return ForgeCompat.ASM_DATA;
    }

    public void addASMData(String annotation, String className, String objectName, Map<String, Object> info) {
        data.computeIfAbsent(annotation, k -> ConcurrentHashMap.newKeySet()).add(new ASMData(annotation, className, objectName, info));
    }

    public Set<ASMData> getAll(String annotation) {
        return Collections.unmodifiableSet(data.getOrDefault(annotation, new HashSet<>()));
    }

    public static final class ASMData implements Cloneable {
        private final String annotationName;
        private final String className;
        private final String objectName;
        private final Map<String, Object> annotationInfo;

        public ASMData(String annotationName, String className, String objectName, Map<String, Object> annotationInfo) {
            this.annotationName = annotationName;
            this.className = className;
            this.objectName = objectName;
            this.annotationInfo = annotationInfo;
        }

        public String getAnnotationName() {
            return annotationName;
        }

        public String getClassName() {
            return className;
        }

        public String getObjectName() {
            return objectName;
        }

        public Map<String, Object> getAnnotationInfo() {
            return annotationInfo;
        }
    }
}
