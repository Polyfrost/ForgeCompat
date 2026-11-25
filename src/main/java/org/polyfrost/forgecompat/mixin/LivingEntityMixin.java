package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.polyfrost.forgecompat.Hooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void forgecompat$livingUpdate(CallbackInfo ci) {
        if (Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        if (MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingUpdateEvent((LivingEntity) (Object) this))) ci.cancel();
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void forgecompat$jump(CallbackInfo ci) {
        if (Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        MinecraftForge.EVENT_BUS.post(new LivingEvent.LivingJumpEvent((LivingEntity) (Object) this));
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void forgecompat$death(DamageSource source, CallbackInfo ci) {
        if (Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        if (MinecraftForge.EVENT_BUS.post(new LivingDeathEvent((LivingEntity) (Object) this, source))) ci.cancel();
    }

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    private void forgecompat$hurt(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef amountRef) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (Hooks.isSceneWorld(entity.world) || entity.isInvulnerable(source)) return;
        amountRef.set(ForgeHooks.onLivingHurt(entity, source, amount));
        if (amountRef.get() <= 0) ci.cancel();
    }

    @Inject(method = "takeDamage", at = @At("HEAD"), cancellable = true)
    private void forgecompat$attacked(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (Hooks.isSceneWorld(((LivingEntity) (Object) this).world)) return;
        if (MinecraftForge.EVENT_BUS.post(new LivingAttackEvent((LivingEntity) (Object) this, source, amount))) cir.setReturnValue(false);
    }
}
