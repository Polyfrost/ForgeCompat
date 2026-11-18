package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.client.IRenderHandler;
import org.polyfrost.forgecompat.ForgeDimension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ClientWorld world;

    @Inject(method = "renderSky(FI)V", at = @At("HEAD"), cancellable = true)
    private void forgecompat$skyRenderer(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        IRenderHandler renderer = ((ForgeDimension) world.dimension).getSkyRenderer();
        if (renderer == null) return;
        renderer.render(tickDelta, world, minecraft);
        ci.cancel();
    }

    @Inject(method = "renderClouds(FI)V", at = @At("HEAD"), cancellable = true)
    private void forgecompat$cloudRenderer(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        IRenderHandler renderer = ((ForgeDimension) world.dimension).getCloudRenderer();
        if (renderer == null) return;
        renderer.render(tickDelta, world, minecraft);
        ci.cancel();
    }
}
