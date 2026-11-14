package org.polyfrost.forgecompat.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.forgecompat.ForgeCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void forgecompat$stopping(CallbackInfo ci) {
        ForgeCompat.fireAll(new FMLServerStoppingEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    private void forgecompat$stopped(CallbackInfo ci) {
        ForgeCompat.fireAll(new FMLServerStoppedEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void forgecompat$tickStart(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void forgecompat$tickEnd(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
    }
}
