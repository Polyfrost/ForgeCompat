package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At("HEAD"), cancellable = true)
    private void forgecompat$specialsPre(LivingEntity entity, double x, double y, double z, CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(entity.world)) return;
        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Specials.Pre<>(entity, renderer(), x, y, z))) ci.cancel();
    }

    @Inject(method = "renderNameTag(Lnet/minecraft/entity/living/LivingEntity;DDD)V", at = @At("TAIL"))
    private void forgecompat$specialsPost(LivingEntity entity, double x, double y, double z, CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(entity.world)) return;
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Specials.Post<>(entity, renderer(), x, y, z));
    }

    @SuppressWarnings("unchecked")
    private LivingEntityRenderer<T> renderer() {
        return (LivingEntityRenderer<T>) (Object) this;
    }
}
