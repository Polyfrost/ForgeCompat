package org.polyfrost.forgecompat.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.polyfrost.forgecompat.ForgeCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    @Inject(method = "init", at = @At("HEAD"))
    private void forgecompat$aboutToStart(CallbackInfoReturnable<Boolean> cir) {
        ForgeCompat.fireAll(new FMLServerAboutToStartEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void forgecompat$starting(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        ForgeCompat.fireAll(new FMLServerStartingEvent((MinecraftServer) (Object) this));
        ForgeCompat.fireAll(new FMLServerStartedEvent((MinecraftServer) (Object) this));
    }
}
