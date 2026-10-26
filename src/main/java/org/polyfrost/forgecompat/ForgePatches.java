package org.polyfrost.forgecompat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

final class ForgePatches {
    private static final String HOOKS_CLIENT = "net/minecraftforge/client/ForgeHooksClient";
    private static final String MOUSE_PRE = "net/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre";
    private static final String EVENT_BUS = "net/minecraftforge/fml/common/eventhandler/EventBus";

    static void apply(ClassNode node) {
        Mappings mappings = ForgeCompat.mappings();
        String screen = mappings.mcpToRuntime("net/minecraft/client/gui/GuiScreen");
        if (node.name.equals(mappings.mcpToRuntime("net/minecraft/client/renderer/EntityRenderer"))) {
            String drawScreen = mappings.srgToRuntime("func_73863_a");
            for (AbstractInsnNode insn : method(node, mappings.srgToRuntime("func_181560_a"), "(FJ)V").instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(screen) && call.name.equals(drawScreen) && call.desc.equals("(IIF)V")) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = HOOKS_CLIENT;
                    call.name = "drawScreen";
                    call.desc = "(L" + screen + ";IIF)V";
                }
            }
        } else if (node.name.equals(mappings.mcpToRuntime("net/minecraft/client/renderer/OpenGlHelper"))) {
            String lightmapUnit = mappings.srgToRuntime("field_77476_b");
            node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "lastBrightnessX", "F", null, null));
            node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "lastBrightnessY", "F", null, null));
            LabelNode skip = new LabelNode();
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ILOAD, 0));
            hook.add(new FieldInsnNode(Opcodes.GETSTATIC, node.name, lightmapUnit, "I"));
            hook.add(new JumpInsnNode(Opcodes.IF_ICMPNE, skip));
            hook.add(new VarInsnNode(Opcodes.FLOAD, 1));
            hook.add(new FieldInsnNode(Opcodes.PUTSTATIC, node.name, "lastBrightnessX", "F"));
            hook.add(new VarInsnNode(Opcodes.FLOAD, 2));
            hook.add(new FieldInsnNode(Opcodes.PUTSTATIC, node.name, "lastBrightnessY", "F"));
            hook.add(skip);
            method(node, mappings.srgToRuntime("func_77475_a"), "(IFF)V").instructions.insert(hook);
        } else if (node.name.equals(screen)) {
            String handleMouseInput = mappings.srgToRuntime("func_146274_d");
            MethodNode handleInput = method(node, mappings.srgToRuntime("func_146269_k"), "()V");
            for (AbstractInsnNode insn : handleInput.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode call) || !call.owner.equals(screen) || !call.name.equals(handleMouseInput)) continue;
                LabelNode skip = new LabelNode();
                LabelNode end = new LabelNode();
                InsnList before = new InsnList();
                before.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS", "L" + EVENT_BUS + ";"));
                before.add(new TypeInsnNode(Opcodes.NEW, MOUSE_PRE));
                before.add(new InsnNode(Opcodes.DUP));
                before.add(new VarInsnNode(Opcodes.ALOAD, 0));
                before.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, MOUSE_PRE, "<init>", "(L" + screen + ";)V", false));
                before.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT_BUS, "post", "(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", false));
                before.add(new JumpInsnNode(Opcodes.IFNE, skip));
                handleInput.instructions.insertBefore(call, before);
                InsnList after = new InsnList();
                after.add(new JumpInsnNode(Opcodes.GOTO, end));
                after.add(skip);
                after.add(new InsnNode(Opcodes.POP));
                after.add(end);
                handleInput.instructions.insert(call, after);
            }
        }
    }

    private static MethodNode method(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) return method;
        }
        throw new IllegalStateException("Missing " + node.name + "." + name + desc);
    }
}
