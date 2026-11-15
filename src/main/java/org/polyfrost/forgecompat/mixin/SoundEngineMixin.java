package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.sound.SoundPool;
import net.minecraft.client.sound.instance.SoundInstance;
import net.minecraft.client.sound.system.SoundEngine;
import net.minecraft.client.sound.system.SoundManager;
import net.minecraft.resource.Identifier;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @WrapOperation(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/system/SoundManager;get(Lnet/minecraft/resource/Identifier;)Lnet/minecraft/client/sound/SoundPool;"))
    private SoundPool forgecompat$playSound(SoundManager manager, Identifier location, Operation<SoundPool> original, @Local(argsOnly = true) LocalRef<SoundInstance> sound, @Cancellable CallbackInfo ci) {
        SoundPool pool = original.call(manager, location);
        PlaySoundEvent event = new PlaySoundEvent((SoundEngine) (Object) this, sound.get(), pool == null ? null : pool.getCategory());
        MinecraftForge.EVENT_BUS.post(event);
        if (event.result == null) {
            ci.cancel();
            return pool;
        }
        if (event.result == sound.get() && event.result.getLocation().equals(location)) return pool;
        sound.set(event.result);
        return original.call(manager, event.result.getLocation());
    }
}
