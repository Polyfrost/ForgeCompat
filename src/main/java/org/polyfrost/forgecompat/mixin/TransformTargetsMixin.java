package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleManager;
import net.minecraft.client.entity.living.player.ClientPlayerEntity;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.menu.GameMenuScreen;
import net.minecraft.client.gui.screen.menu.options.OptionsScreen;
import net.minecraft.client.network.PlayerInfo;
import net.minecraft.client.network.handler.ClientPlayNetworkHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.block.entity.SkullRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.entity.layer.AbstractArmorLayer;
import net.minecraft.client.render.entity.layer.CapeLayer;
import net.minecraft.client.render.entity.layer.WornSkullLayer;
import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.client.render.model.entity.PlayerModel;
import net.minecraft.client.render.platform.GLX;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.sound.system.SoundEngine;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.living.effect.StatusEffect;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({Minecraft.class, OptionsScreen.class, GameMenuScreen.class, PlayerRenderer.class, PlayerModel.class, LivingEntityRenderer.class, EntityRenderer.class, GameRenderer.class, Screen.class, GLX.class,
        LocalClientPlayerEntity.class, ItemStack.class, BowItem.class, ParticleManager.class, KeyBinding.class, ClientPlayNetworkHandler.class, EntityRenderDispatcher.class,
        ItemEntityRenderer.class, ClientPlayerInteractionManager.class, WorldRenderer.class, SoundEngine.class, PlayerInfo.class, StatusEffect.class,
        ClientWorld.class, PlayerManager.class, WorldChunk.class, PlayerEntity.class,
        ClientPlayerEntity.class, ModelPart.class, PlayerTabOverlay.class,
        GuiIngameForge.class, HumanoidModel.class, SkullRenderer.class,
        CapeLayer.class, AbstractArmorLayer.class,
        WornSkullLayer.class, ServerPlayNetworkHandler.class})
public abstract class TransformTargetsMixin {
}
