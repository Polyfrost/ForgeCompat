package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {
    public boolean shouldRender(StatusEffectInstance effect) {
        return true;
    }

    public boolean shouldRenderInvText(StatusEffectInstance effect) {
        return true;
    }

    public boolean shouldRenderHUD(StatusEffectInstance effect) {
        return true;
    }

    public void renderInventoryEffect(int x, int y, StatusEffectInstance effect, Minecraft mc) {
    }

    public void renderHUDEffect(int x, int y, StatusEffectInstance effect, Minecraft mc, float alpha) {
    }
}
