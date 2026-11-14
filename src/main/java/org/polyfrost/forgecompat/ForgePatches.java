package org.polyfrost.forgecompat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

final class ForgePatches {
    private static final String MOUSE_PRE = "net/minecraftforge/client/event/GuiScreenEvent$MouseInputEvent$Pre";
    private static final String EVENT_BUS = "net/minecraftforge/fml/common/eventhandler/EventBus";
    private static final String FONT_RESOURCE_STREAM = "getResourceInputStream";

    static void apply(ClassNode node) {
        Mappings mappings = ForgeCompat.mappings();
        String screen = mappings.mcpToRuntime("net/minecraft/client/gui/GuiScreen");
        if (node.name.equals(mappings.mcpToRuntime("net/minecraft/client/renderer/OpenGlHelper"))) {
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
        } else if (node.name.equals(mappings.mcpToRuntime("net/minecraft/potion/Potion"))) {
            String identifier = mappings.mcpToRuntime("net/minecraft/util/ResourceLocation");
            MethodNode init = new MethodNode(Opcodes.ACC_PROTECTED, "<init>", "(L" + identifier + ";ZI)V", null, null);
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            init.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/polyfrost/forgecompat/Hooks", "nextPotionId", "()I", false));
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            init.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
            init.instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
            init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, node.name, "<init>", "(IL" + identifier + ";ZI)V", false));
            init.instructions.add(new InsnNode(Opcodes.RETURN));
            init.maxStack = 5;
            init.maxLocals = 4;
            node.methods.add(init);
            for (AbstractInsnNode insn : method(node, "<clinit>", "()V").instructions.toArray()) {
                if (insn instanceof IntInsnNode size && size.operand == 32 && insn.getNext() instanceof TypeInsnNode array && array.getOpcode() == Opcodes.ANEWARRAY && array.desc.equals(node.name)) {
                    size.setOpcode(Opcodes.SIPUSH);
                    size.operand = 256;
                    break;
                }
            }
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

    static byte[] applyFontRenderer(byte[] bytes) {
        Mappings mappings = ForgeCompat.mappings();
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        String minecraft = mappings.mcpToRuntime("net/minecraft/client/Minecraft");
        String identifier = mappings.mcpToRuntime("net/minecraft/util/ResourceLocation");
        String resourceManager = mappings.mcpToRuntime("net/minecraft/client/resources/IResourceManager");
        String resource = mappings.mcpToRuntime("net/minecraft/client/resources/IResource");
        String getInstance = mappings.srgToRuntime("func_71410_x");
        String getResourceManager = mappings.srgToRuntime("func_110442_L");
        String getResource = mappings.srgToRuntime("func_110536_a");
        String getInputStream = mappings.srgToRuntime("func_110527_b");
        String streamDesc = "(L" + identifier + ";)Ljava/io/InputStream;";

        MethodNode stream = new MethodNode(Opcodes.ACC_PROTECTED, FONT_RESOURCE_STREAM, streamDesc, null, new String[]{"java/io/IOException"});
        stream.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, minecraft, getInstance, "()L" + minecraft + ";", false));
        stream.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, minecraft, getResourceManager, "()L" + resourceManager + ";", false));
        stream.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        stream.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, resourceManager, getResource, "(L" + identifier + ";)L" + resource + ";", true));
        stream.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, resource, getInputStream, "()Ljava/io/InputStream;", true));
        stream.instructions.add(new InsnNode(Opcodes.ARETURN));
        stream.maxStack = 2;
        stream.maxLocals = 2;
        node.methods.add(stream);

        String glState = mappings.mcpToRuntime("net/minecraft/client/renderer/GlStateManager");
        MethodNode alpha = new MethodNode(Opcodes.ACC_PUBLIC, "enableAlpha", "()V", null, null);
        alpha.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, glState, mappings.srgToRuntime("func_179141_d"), "()V", false));
        alpha.instructions.add(new InsnNode(Opcodes.RETURN));
        alpha.maxLocals = 1;
        node.methods.add(alpha);

        for (String methodName : new String[]{mappings.srgToRuntime("func_111272_d"), mappings.srgToRuntime("func_98306_d")}) {
            MethodNode method = method(node, methodName, "()V");
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode call)) continue;
                if (call.owner.equals(minecraft) && call.name.equals(getInstance) && call.getNext() instanceof MethodInsnNode next && next.name.equals(getResourceManager)) {
                    method.instructions.set(call, new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.remove(next);
                } else if (call.owner.equals(resourceManager) && call.name.equals(getResource) && call.getNext() instanceof MethodInsnNode next && next.name.equals(getInputStream)) {
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, FONT_RESOURCE_STREAM, streamDesc, false));
                    method.instructions.remove(next);
                }
            }
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode method(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) return method;
        }
        throw new IllegalStateException("Missing " + node.name + "." + name + desc);
    }
}
