package org.polyfrost.forgecompat.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.common.LoaderState;
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
        ForgeCompat.fireServerEvent(new FMLServerAboutToStartEvent((MinecraftServer) (Object) this), LoaderState.SERVER_ABOUT_TO_START);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void forgecompat$starting(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        ForgeCompat.fireServerEvent(new FMLServerStartingEvent((MinecraftServer) (Object) this), LoaderState.SERVER_STARTING);
        ForgeCompat.fireServerEvent(new FMLServerStartedEvent((MinecraftServer) (Object) this), LoaderState.SERVER_STARTED);
    }
}
