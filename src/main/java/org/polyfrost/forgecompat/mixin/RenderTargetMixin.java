package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.render.pipeline.RenderTarget;
import net.minecraft.client.render.platform.GLX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {
    @Unique
    private static final int GL_DEPTH24_STENCIL8 = 0x88F0;
    @Unique
    private static final int GL_STENCIL_ATTACHMENT = 0x8D20;

    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    public int depthBufferId;

    @Unique
    private boolean stencilEnabled;

    @Shadow
    public abstract void resize(int width, int height);

    public boolean enableStencil() {
        if (!GLX.useFbo()) return false;
        if (!stencilEnabled) {
            stencilEnabled = true;
            resize(width, height);
        }
        return true;
    }

    public boolean isStencilEnabled() {
        return stencilEnabled;
    }

    @ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GLX;renderbufferStorage(IIII)V"), index = 1)
    private int forgecompat$depthStencilFormat(int format) {
        return stencilEnabled ? GL_DEPTH24_STENCIL8 : format;
    }

    @Inject(method = "createBuffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GLX;framebufferRenderbuffer(IIII)V", shift = At.Shift.AFTER))
    private void forgecompat$attachStencil(CallbackInfo ci) {
        if (stencilEnabled) GLX.framebufferRenderbuffer(GLX.GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GLX.GL_RENDERBUFFER, depthBufferId);
    }
}
