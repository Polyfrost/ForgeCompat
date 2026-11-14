package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.gui.chat.ChatGui;
import net.minecraft.client.gui.overlay.DebugOverlay;
import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import net.minecraft.client.gui.spectator.SpectatorGui;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraft.client.render.Window;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameGui.class)
public class GameGuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void forgecompat$overlayPre(float tickDelta, CallbackInfo ci) {
        GuiIngameForge.left_height = 39;
        GuiIngameForge.right_height = 39;
        if (!pre(ElementType.ALL, true, null, tickDelta)) ci.cancel();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderPumpkinOverlay(Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$helmet(GameGui gui, Window window, Operation<Void> original, float tickDelta) {
        if (!pre(ElementType.HELMET, GuiIngameForge.renderHelmet, window, tickDelta)) return;
        original.call(gui, window);
        post(ElementType.HELMET, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderNauseaOverlay(FLnet/minecraft/client/render/Window;)V"))
    private void forgecompat$portal(GameGui gui, float delta, Window window, Operation<Void> original, float tickDelta) {
        if (!pre(ElementType.PORTAL, GuiIngameForge.renderPortal, window, tickDelta)) return;
        original.call(gui, delta, window);
        post(ElementType.PORTAL, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;hasCrosshair()Z"))
    private boolean forgecompat$crosshairs(GameGui gui, Operation<Boolean> original, float tickDelta, @Local Window window) {
        if (!pre(ElementType.CROSSHAIRS, GuiIngameForge.renderCrosshairs, window, tickDelta)) return false;
        boolean shown = original.call(gui);
        post(ElementType.CROSSHAIRS, window, tickDelta);
        return shown;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderBossBars()V"))
    private void forgecompat$bossHealth(GameGui gui, Operation<Void> original, float tickDelta, @Local Window window) {
        if (!pre(ElementType.BOSSHEALTH, GuiIngameForge.renderBossHealth, window, tickDelta)) return;
        original.call(gui);
        post(ElementType.BOSSHEALTH, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderStatusBars(Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$statusBars(GameGui gui, Window window, Operation<Void> original, float tickDelta) {
        int shown = 0;
        if (pre(ElementType.HEALTH, GuiIngameForge.renderHealth, window, tickDelta)) shown |= 1;
        if (pre(ElementType.ARMOR, GuiIngameForge.renderArmor, window, tickDelta)) shown |= 2;
        if (pre(ElementType.FOOD, GuiIngameForge.renderFood, window, tickDelta)) shown |= 4;
        if (pre(ElementType.HEALTHMOUNT, GuiIngameForge.renderHealthMount, window, tickDelta)) shown |= 8;
        if (pre(ElementType.AIR, GuiIngameForge.renderAir, window, tickDelta)) shown |= 16;
        if (shown == 0) return;
        original.call(gui, window);
        GuiIngameForge.left_height += 10;
        GuiIngameForge.right_height += 10;
        if ((shown & 1) != 0) post(ElementType.HEALTH, window, tickDelta);
        if ((shown & 2) != 0) post(ElementType.ARMOR, window, tickDelta);
        if ((shown & 4) != 0) post(ElementType.FOOD, window, tickDelta);
        if ((shown & 8) != 0) post(ElementType.HEALTHMOUNT, window, tickDelta);
        if ((shown & 16) != 0) post(ElementType.AIR, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderJumpBar(Lnet/minecraft/client/render/Window;I)V"))
    private void forgecompat$jumpBar(GameGui gui, Window window, int x, Operation<Void> original, float tickDelta) {
        if (!pre(ElementType.JUMPBAR, GuiIngameForge.renderJumpBar, window, tickDelta)) return;
        original.call(gui, window, x);
        post(ElementType.JUMPBAR, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderXpBar(Lnet/minecraft/client/render/Window;I)V"))
    private void forgecompat$experience(GameGui gui, Window window, int x, Operation<Void> original, float tickDelta) {
        if (!pre(ElementType.EXPERIENCE, GuiIngameForge.renderExperiance, window, tickDelta)) return;
        original.call(gui, window, x);
        post(ElementType.EXPERIENCE, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderScoreboardObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$objective(GameGui gui, ScoreboardObjective objective, Window window, Operation<Void> original) {
        if (GuiIngameForge.renderObjective) original.call(gui, objective, window);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/DebugOverlay;render(Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$debug(DebugOverlay overlay, Window window, Operation<Void> original, float tickDelta) {
        if (!pre(ElementType.DEBUG, true, window, tickDelta)) return;
        original.call(overlay, window);
        post(ElementType.DEBUG, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;render(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void forgecompat$playerList(PlayerTabOverlay overlay, int width, Scoreboard scoreboard, ScoreboardObjective objective, Operation<Void> original, float tickDelta, @Local Window window) {
        if (!pre(ElementType.PLAYER_LIST, true, window, tickDelta)) return;
        original.call(overlay, width, scoreboard, objective);
        post(ElementType.PLAYER_LIST, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/chat/ChatGui;render(I)V"))
    private void forgecompat$chat(ChatGui chat, int ticks, Operation<Void> original, float tickDelta, @Local Window window) {
        if (!MinecraftForge.EVENT_BUS.hasListeners(RenderGameOverlayEvent.Chat.class)) {
            original.call(chat, ticks);
        } else {
            int y = window.getHeight() - 48;
            RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(tickDelta, window, 0, y);
            if (MinecraftForge.EVENT_BUS.post(event)) return;
            GlStateManager.translatef(event.posX, event.posY - y, 0);
            original.call(chat, ticks);
            GlStateManager.translatef(-event.posX, y - event.posY, 0);
        }
        post(ElementType.CHAT, window, tickDelta);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void forgecompat$overlayPost(float tickDelta, CallbackInfo ci) {
        // render's Window local is out of scope at return, so build one only if someone listens
        Window window = null;
        if (MinecraftForge.EVENT_BUS.hasListeners(RenderGameOverlayEvent.Text.class)) forgecompat$text(tickDelta, window = new Window(minecraft));
        post(ElementType.ALL, window, tickDelta);
    }

    @Unique
    private void forgecompat$text(float tickDelta, Window window) {
        java.util.ArrayList<String> left = new java.util.ArrayList<>();
        java.util.ArrayList<String> right = new java.util.ArrayList<>();
        RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(tickDelta, window, left, right);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        net.minecraft.client.render.TextRenderer font = minecraft.textRenderer;
        boolean debug = minecraft.options.debugEnabled;
        int top = 2;
        for (String line : left) {
            if (line != null && !line.isEmpty()) {
                if (debug) net.minecraft.client.gui.GuiElement.fill(1, top - 1, 2 + font.getWidth(line) + 1, top + font.fontHeight - 1, 0x90505050);
                font.drawWithShadow(line, 2, top, 0xE0E0E0);
            }
            top += font.fontHeight;
        }
        top = 2;
        for (String line : right) {
            if (line != null && !line.isEmpty()) {
                int width = font.getWidth(line);
                int x = window.getWidth() - 2 - width;
                if (debug) net.minecraft.client.gui.GuiElement.fill(x - 1, top - 1, x + width + 1, top + font.fontHeight - 1, 0x90505050);
                font.drawWithShadow(line, x, top, 0xE0E0E0);
            }
            top += font.fontHeight;
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderHotbar(Lnet/minecraft/client/render/Window;F)V"))
    private void forgecompat$hotbar(GameGui gui, Window window, float tickDelta, Operation<Void> original) {
        if (!pre(ElementType.HOTBAR, GuiIngameForge.renderHotbar, window, tickDelta)) return;
        original.call(gui, window, tickDelta);
        post(ElementType.HOTBAR, window, tickDelta);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/render/Window;F)V"))
    private void forgecompat$spectatorHotbar(SpectatorGui gui, Window window, float tickDelta, Operation<Void> original) {
        if (!pre(ElementType.HOTBAR, GuiIngameForge.renderHotbar, window, tickDelta)) return;
        original.call(gui, window, tickDelta);
        post(ElementType.HOTBAR, window, tickDelta);
    }

    /** @param window null to create one only if a listener needs it */
    @Unique
    private static boolean pre(ElementType type, boolean enabled, Window window, float tickDelta) {
        if (!enabled) return false;
        if (!MinecraftForge.EVENT_BUS.hasListeners(RenderGameOverlayEvent.Pre.class)) return true;
        return !MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(tickDelta, window != null ? window : new Window(Minecraft.getInstance()), type));
    }

    /** @param window null to create one only if a listener needs it */
    @Unique
    private static void post(ElementType type, Window window, float tickDelta) {
        if (MinecraftForge.EVENT_BUS.hasListeners(RenderGameOverlayEvent.Post.class)) MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(tickDelta, window != null ? window : new Window(Minecraft.getInstance()), type));
    }
}
