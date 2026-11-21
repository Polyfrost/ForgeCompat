package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.entity.living.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({GLX.class, StatusEffect.class, Screen.class})
public abstract class TransformTargetsMixin {
}
