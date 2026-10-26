package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.GameMenuScreen;
import net.minecraft.client.gui.screen.menu.options.OptionsScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.entity.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;

// ponytail: Forge transformers and ForgePatches only see these classes (what OAM Cosmetics, HDSkins and Essential patch); add targets for other mods
@Mixin({Minecraft.class, OptionsScreen.class, GameMenuScreen.class, PlayerRenderer.class, PlayerModel.class, LivingEntityRenderer.class, EntityRenderer.class, GameRenderer.class, Screen.class, GLX.class})
public abstract class TransformTargetsMixin {
}
