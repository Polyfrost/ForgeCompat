package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.world.chunk.ClientChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import org.polyfrost.forgecompat.Hooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
    @Shadow
    private World world;

    @Inject(method = "loadChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setLoaded(Z)V"))
    private void forgecompat$load(int x, int z, CallbackInfoReturnable<WorldChunk> cir, @Local WorldChunk chunk) {
        if (!Hooks.isSceneWorld(world)) MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
    }
}
