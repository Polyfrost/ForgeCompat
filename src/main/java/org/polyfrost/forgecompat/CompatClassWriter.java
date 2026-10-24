package org.polyfrost.forgecompat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class CompatClassWriter extends ClassWriter {
    public CompatClassWriter(int flags) {
        super(flags);
    }

    public CompatClassWriter(ClassReader reader, int flags) {
        super(reader, flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        return ForgeCompat.mappings().commonSuperClass(type1, type2);
    }
}
