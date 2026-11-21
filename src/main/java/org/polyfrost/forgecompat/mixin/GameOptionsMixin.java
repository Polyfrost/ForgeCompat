package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Ljava/lang/String;split(Ljava/lang/String;)[Ljava/lang/String;"))
    private String[] forgecompat$splitKeyOnLastColon(String line, String regex, Operation<String[]> original) {
        int colon = line.lastIndexOf(':');
        if (!line.startsWith("key_") || colon < 0) return original.call(line, regex);
        return new String[]{line.substring(0, colon), line.substring(colon + 1)};
    }
}
