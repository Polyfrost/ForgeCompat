package org.polyfrost.forgecompat.mixin;

import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import org.polyfrost.forgecompat.Hooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
    @Shadow
    @Final
    private World world;

    @Inject(method = "load", at = @At("TAIL"))
    private void forgecompat$load(CallbackInfo ci) {
        if (!Hooks.isSceneWorld(world)) MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load((WorldChunk) (Object) this));
    }

    @Inject(method = "unload", at = @At("TAIL"))
    private void forgecompat$unload(CallbackInfo ci) {
        if (!Hooks.isSceneWorld(world)) MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload((WorldChunk) (Object) this));
    }
}
