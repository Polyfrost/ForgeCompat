package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @ModifyReturnValue(method = "getFovModifier", at = @At("RETURN"))
    private float forgecompat$fovUpdate(float fov) {
        if (!MinecraftForge.EVENT_BUS.hasListeners(FOVUpdateEvent.class)) return fov;
        FOVUpdateEvent event = new FOVUpdateEvent((ClientPlayerEntity) (Object) this, fov);
        MinecraftForge.EVENT_BUS.post(event);
        return event.newfov;
    }
}
