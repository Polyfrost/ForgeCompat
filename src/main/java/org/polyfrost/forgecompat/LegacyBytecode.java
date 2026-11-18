package org.polyfrost.forgecompat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.Collection;

final class LegacyBytecode {
    static void apply(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof MethodInsnNode call && fixToArrayCast(method, call)) method.maxStack++;
        }
    }

    private static boolean fixToArrayCast(MethodNode method, MethodInsnNode call) {
        if (!call.name.equals("toArray") || !call.desc.equals("()[Ljava/lang/Object;") || call.getOpcode() == Opcodes.INVOKESTATIC
                || call.getOpcode() == Opcodes.INVOKESPECIAL || !isCollection(call.owner)) return false;
        if (!(nextInsn(call) instanceof TypeInsnNode cast) || cast.getOpcode() != Opcodes.CHECKCAST
                || !cast.desc.startsWith("[") || cast.desc.equals("[Ljava/lang/Object;")) return false;
        method.instructions.insertBefore(call, new LdcInsnNode(Type.getType(cast.desc)));
        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, "org/polyfrost/forgecompat/Hooks", "toArray",
                "(Ljava/util/Collection;Ljava/lang/Class;)[Ljava/lang/Object;", false));
        return true;
    }

    private static boolean isCollection(String owner) {
        if (!owner.startsWith("java/util/") && !owner.startsWith("com/google/common/collect/")) return false;
        try {
            return Collection.class.isAssignableFrom(Class.forName(owner.replace('/', '.'), false, LegacyBytecode.class.getClassLoader()));
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private static AbstractInsnNode nextInsn(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() < 0) next = next.getNext();
        return next;
    }

    private LegacyBytecode() {
    }
}
