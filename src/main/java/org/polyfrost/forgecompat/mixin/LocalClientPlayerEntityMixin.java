package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import org.objectweb.asm.Opcodes;
import org.polyfrost.forgecompat.RavenHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalClientPlayerEntity.class)
public class LocalClientPlayerEntityMixin {
    @ModifyExpressionValue(method = "mobTick", at = @At(value = "CONSTANT", args = "floatValue=0.2F"))
    private float forgecompat$ravenNoSlow(float slowdown) {
        return RavenHooks.active() ? 1.0F : slowdown;
    }

    @Inject(method = "mobTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/living/player/Input;movementForward:F", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
    private void forgecompat$ravenSlowdown(CallbackInfo ci) {
        if (RavenHooks.active()) RavenHooks.slowdown();
    }
}
