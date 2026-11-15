package org.polyfrost.forgecompat.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.objectweb.asm.Opcodes;
import org.polyfrost.forgecompat.ForgeCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "shutdown", at = @At("HEAD"))
    private void forgecompat$stopping(CallbackInfo ci) {
        ForgeCompat.fireServerEvent(new FMLServerStoppingEvent((MinecraftServer) (Object) this), LoaderState.SERVER_STOPPING);
    }

    @Unique
    private boolean forgecompat$expectedStop;

    @Inject(method = "run", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;onServerCrashed(Lnet/minecraft/util/crash/CrashReport;)V"),
            @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;stopped:Z", opcode = Opcodes.PUTFIELD)
    })
    private void forgecompat$expectStopped(CallbackInfo ci) {
        if (forgecompat$expectedStop) return;
        forgecompat$expectedStop = true;
        FMLCommonHandler.instance().expectServerStopped();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;exit()V"))
    private void forgecompat$stopped(CallbackInfo ci) {
        ForgeCompat.fireServerEvent(new FMLServerStoppedEvent((MinecraftServer) (Object) this), LoaderState.AVAILABLE);
        FMLCommonHandler.instance().handleServerStopped();
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
