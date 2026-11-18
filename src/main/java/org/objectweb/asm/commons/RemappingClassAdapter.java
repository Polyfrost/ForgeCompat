package org.objectweb.asm.commons;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@Deprecated
public class RemappingClassAdapter extends ClassRemapper {
    public RemappingClassAdapter(ClassVisitor classVisitor, Remapper remapper) {
        this(Opcodes.ASM5, classVisitor, remapper);
    }

    protected RemappingClassAdapter(int api, ClassVisitor classVisitor, Remapper remapper) {
        super(api, classVisitor, remapper);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        String newDesc = remapper.mapMethodDesc(descriptor);
        MethodVisitor mv = cv == null ? null : cv.visitMethod(access, remapper.mapMethodName(className, name, descriptor), newDesc,
                remapper.mapSignature(signature, false), exceptions == null ? null : remapper.mapTypes(exceptions));
        return mv == null ? null : createRemappingMethodAdapter(access, newDesc, mv);
    }

    @Override
    protected FieldVisitor createFieldRemapper(FieldVisitor fieldVisitor) {
        return createRemappingFieldAdapter(fieldVisitor);
    }

    protected FieldVisitor createRemappingFieldAdapter(FieldVisitor fv) {
        return super.createFieldRemapper(fv);
    }

    protected MethodVisitor createRemappingMethodAdapter(int access, String newDesc, MethodVisitor mv) {
        return createMethodRemapper(mv);
    }
}
