package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.menu.GameMenuScreen;
import net.minecraft.client.gui.screen.menu.options.OptionsScreen;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.entity.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;

// ponytail: Forge transformers only see these classes (what OAM Cosmetics and HDSkins patch); add targets for other mods
@Mixin({Minecraft.class, OptionsScreen.class, GameMenuScreen.class, PlayerRenderer.class, PlayerModel.class, LivingEntityRenderer.class, EntityRenderer.class})
public abstract class TransformTargetsMixin {
}
