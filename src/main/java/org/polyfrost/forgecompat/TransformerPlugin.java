package org.polyfrost.forgecompat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fabric has no class transformer hook, so Forge transformers run in preApply of the TransformTargetsMixins.
 */
public class TransformerPlugin implements IMixinConfigPlugin {
    private static final boolean OVERWRITES_PRESENT = EarlyInit.hasMixinConfig("liquidbounce.forge.mixins.json");
    private static final boolean RAVEN_PRESENT = EarlyInit.hasEntry("keystrokesmod/tweaker/ASMEventHandler.class", "keystrokesmod/tw/ev.class");

    @Override
    public void preApply(String targetClassName, ClassNode node, String mixinClassName, IMixinInfo mixinInfo) {
        if (!mixinClassName.endsWith("TransformTargetsMixin") || !ForgeCompat.active()) return;
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        byte[] bytes = ForgeCompat.transform(node.name, writer.toByteArray());
        if (bytes == null) {
            ForgePatches.apply(node);
            return;
        }

        Map<String, MethodNode> originals = new HashMap<>();
        for (MethodNode method : node.methods) originals.put(method.name + method.desc, method);
        node.methods.clear();
        node.fields.clear();
        node.innerClasses.clear();
        node.visibleAnnotations = null;
        node.invisibleAnnotations = null;
        node.visibleTypeAnnotations = null;
        node.invisibleTypeAnnotations = null;
        node.attrs = null;
        node.nestMembers = null;
        node.permittedSubclasses = null;
        node.recordComponents = null;
        new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);

        for (int i = 0; i < node.methods.size(); i++) {
            MethodNode method = node.methods.get(i);
            MethodNode original = originals.get(method.name + method.desc);
            if (original == null) continue;
            try {
                for (Field field : MethodNode.class.getFields()) {
                    if (!field.getName().equals("instructions")) field.set(original, field.get(method));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            // a Target holds on to this InsnList
            original.instructions.clear();
            original.instructions.add(method.instructions);
            node.methods.set(i, original);
        }
        refreshFrames(node.name, bytes);
        ForgePatches.apply(node);
    }

    // Mixin's Locals pairs the target's frames with ClassInfo's, which were read before the Forge transformers ran
    private static void refreshFrames(String name, byte[] bytes) {
        ClassNode transformed = new ClassNode();
        boolean expand = MixinEnvironment.getCurrentEnvironment().getOption(MixinEnvironment.Option.CLASSREADER_EXPAND_FRAMES);
        new ClassReader(bytes).accept(transformed, expand ? ClassReader.EXPAND_FRAMES : 0);
        ClassInfo info = ClassInfo.forName(name);
        for (MethodNode method : transformed.methods) {
            ClassInfo.Method methodInfo = info.findMethod(method, method.access | ClassInfo.INCLUDE_INITIALISERS);
            if (methodInfo == null || methodInfo.getFrames() == null) continue;
            methodInfo.getFrames().clear();
            methodInfo.getFrames().addAll(info.new Method(method).getFrames());
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(".TextRendererMixin")) return RAVEN_PRESENT;
        return OVERWRITES_PRESENT || !mixinClassName.contains(".mixin.overwrite.");
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
