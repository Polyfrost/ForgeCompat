package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void forgecompat$renderPre(ClientPlayerEntity player, double x, double y, double z, float yaw, float tickDelta, CallbackInfo ci) {
        if (!MinecraftForge.EVENT_BUS.hasListeners(RenderPlayerEvent.Pre.class) || org.polyfrost.forgecompat.Hooks.isSceneWorld(player.world)) return;
        if (MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Pre(player, (PlayerRenderer) (Object) this, tickDelta, x, y, z))) ci.cancel();
    }

    @Inject(method = "render(Lnet/minecraft/client/entity/living/player/ClientPlayerEntity;DDDFF)V", at = @At("TAIL"))
    private void forgecompat$renderPost(ClientPlayerEntity player, double x, double y, double z, float yaw, float tickDelta, CallbackInfo ci) {
        if (!MinecraftForge.EVENT_BUS.hasListeners(RenderPlayerEvent.Post.class) || org.polyfrost.forgecompat.Hooks.isSceneWorld(player.world)) return;
        MinecraftForge.EVENT_BUS.post(new RenderPlayerEvent.Post(player, (PlayerRenderer) (Object) this, tickDelta, x, y, z));
    }
}
