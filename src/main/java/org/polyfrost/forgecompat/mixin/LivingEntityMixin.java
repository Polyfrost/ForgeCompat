package org.polyfrost.forgecompat.mixin;

import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void forgecompat$livingUpdate(CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        if (MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingUpdateEvent((LivingEntity) (Object) this))) ci.cancel();
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void forgecompat$jump(CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent((LivingEntity) (Object) this));
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void forgecompat$death(net.minecraft.entity.damage.DamageSource source, CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        if (MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingDeathEvent((LivingEntity) (Object) this, source))) ci.cancel();
    }
}
