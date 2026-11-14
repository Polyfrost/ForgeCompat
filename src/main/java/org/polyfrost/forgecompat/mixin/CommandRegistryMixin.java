package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.command.Command;
import net.minecraft.server.command.handler.CommandRegistry;
import net.minecraft.server.command.source.CommandSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandRegistry.class)
public class CommandRegistryMixin {
    @Inject(method = "run(Lnet/minecraft/server/command/source/CommandSource;Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/Command;canUse(Lnet/minecraft/server/command/source/CommandSource;)Z"), cancellable = true)
    private void forgecompat$commandEvent(CommandSource source, String message, CallbackInfoReturnable<Integer> cir, @Local Command command, @Local(ordinal = 0) LocalRef<String[]> args) {
        if (!command.canUse(source)) return;
        CommandEvent event = new CommandEvent(command, source, args.get());
        if (MinecraftForge.EVENT_BUS.post(event)) {
            if (event.exception instanceof RuntimeException) throw (RuntimeException) event.exception;
            if (event.exception != null) throw new RuntimeException(event.exception);
            cir.setReturnValue(1);
            return;
        }
        args.set(event.parameters);
    }
}
