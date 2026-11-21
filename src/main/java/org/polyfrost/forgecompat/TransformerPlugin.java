package org.polyfrost.forgecompat;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;

import java.util.List;
import java.util.Set;

/**
 * Forge transformers run in ForgeCompat's Knot transformer proxy. ForgePatches run in preApply of TransformTargetsMixin,
 * so Mixin's writer computes their frames.
 */
public class TransformerPlugin implements IMixinConfigPlugin {
    private static final boolean OVERWRITES_PRESENT = EarlyInit.hasMixinConfig("liquidbounce.forge.mixins.json");
    private static final boolean RAVEN_PRESENT = EarlyInit.hasEntry("keystrokesmod/tweaker/ASMEventHandler.class", "keystrokesmod/tw/ev.class");

    @Override
    public void preApply(String targetClassName, ClassNode node, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.endsWith(".TransformTargetsMixin") && ForgeCompat.active()) ForgePatches.apply(node);
    }

    // Mixin's Locals pairs the target's frames with ClassInfo's, which were read before the Forge transformers ran
    static void refreshFrames(String name, byte[] bytes) {
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
