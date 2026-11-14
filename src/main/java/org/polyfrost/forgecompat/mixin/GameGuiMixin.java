package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
        if (MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(tickDelta, new Window(minecraft), ElementType.ALL))) ci.cancel();
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderPumpkinOverlay(Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$helmet(GameGui gui, Window window, Operation<Void> original, float tickDelta) {
        element(ElementType.HELMET, GuiIngameForge.renderHelmet, window, tickDelta, () -> original.call(gui, window));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderNauseaOverlay(FLnet/minecraft/client/render/Window;)V"))
    private void forgecompat$portal(GameGui gui, float delta, Window window, Operation<Void> original, float tickDelta) {
        element(ElementType.PORTAL, GuiIngameForge.renderPortal, window, tickDelta, () -> original.call(gui, delta, window));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;hasCrosshair()Z"))
    private boolean forgecompat$crosshairs(GameGui gui, Operation<Boolean> original, float tickDelta) {
        Window window = new Window(minecraft);
        if (!GuiIngameForge.renderCrosshairs || MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(tickDelta, window, ElementType.CROSSHAIRS))) return false;
        boolean shown = original.call(gui);
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(tickDelta, window, ElementType.CROSSHAIRS));
        return shown;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderBossBars()V"))
    private void forgecompat$bossHealth(GameGui gui, Operation<Void> original, float tickDelta) {
        element(ElementType.BOSSHEALTH, GuiIngameForge.renderBossHealth, new Window(minecraft), tickDelta, () -> original.call(gui));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderStatusBars(Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$statusBars(GameGui gui, Window window, Operation<Void> original, float tickDelta) {
        ElementType[] types = {ElementType.HEALTH, ElementType.ARMOR, ElementType.FOOD, ElementType.HEALTHMOUNT, ElementType.AIR};
        boolean[] flags = {GuiIngameForge.renderHealth, GuiIngameForge.renderArmor, GuiIngameForge.renderFood, GuiIngameForge.renderHealthMount, GuiIngameForge.renderAir};
        boolean[] shown = new boolean[types.length];
        boolean any = false;
        for (int i = 0; i < types.length; i++) {
            shown[i] = flags[i] && !MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(tickDelta, window, types[i]));
            any |= shown[i];
        }
        if (!any) return;
        original.call(gui, window);
        GuiIngameForge.left_height += 10;
        GuiIngameForge.right_height += 10;
        for (int i = 0; i < types.length; i++) {
            if (shown[i]) MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(tickDelta, window, types[i]));
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderJumpBar(Lnet/minecraft/client/render/Window;I)V"))
    private void forgecompat$jumpBar(GameGui gui, Window window, int x, Operation<Void> original, float tickDelta) {
        element(ElementType.JUMPBAR, GuiIngameForge.renderJumpBar, window, tickDelta, () -> original.call(gui, window, x));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderXpBar(Lnet/minecraft/client/render/Window;I)V"))
    private void forgecompat$experience(GameGui gui, Window window, int x, Operation<Void> original, float tickDelta) {
        element(ElementType.EXPERIENCE, GuiIngameForge.renderExperiance, window, tickDelta, () -> original.call(gui, window, x));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GameGui;renderScoreboardObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$objective(GameGui gui, ScoreboardObjective objective, Window window, Operation<Void> original) {
        if (GuiIngameForge.renderObjective) original.call(gui, objective, window);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/DebugOverlay;render(Lnet/minecraft/client/render/Window;)V"))
    private void forgecompat$debug(DebugOverlay overlay, Window window, Operation<Void> original, float tickDelta) {
        element(ElementType.DEBUG, true, window, tickDelta, () -> original.call(overlay, window));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/overlay/PlayerTabOverlay;render(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void forgecompat$playerList(PlayerTabOverlay overlay, int width, Scoreboard scoreboard, ScoreboardObjective objective, Operation<Void> original, float tickDelta) {
        element(ElementType.PLAYER_LIST, true, new Window(minecraft), tickDelta, () -> original.call(overlay, width, scoreboard, objective));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/chat/ChatGui;render(I)V"))
    private void forgecompat$chat(ChatGui chat, int ticks, Operation<Void> original, float tickDelta) {
        Window window = new Window(minecraft);
        int y = window.getHeight() - 48;
        RenderGameOverlayEvent.Chat event = new RenderGameOverlayEvent.Chat(tickDelta, window, 0, y);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        GlStateManager.translatef(event.posX, event.posY - y, 0);
        original.call(chat, ticks);
        GlStateManager.translatef(-event.posX, y - event.posY, 0);
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(tickDelta, window, ElementType.CHAT));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void forgecompat$overlayPost(float tickDelta, CallbackInfo ci) {
        Window window = new Window(minecraft);
        forgecompat$text(tickDelta, window);
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(tickDelta, window, ElementType.ALL));
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
        hotbar(window, tickDelta, () -> original.call(gui, window, tickDelta));
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/render/Window;F)V"))
    private void forgecompat$spectatorHotbar(SpectatorGui gui, Window window, float tickDelta, Operation<Void> original) {
        hotbar(window, tickDelta, () -> original.call(gui, window, tickDelta));
    }

    @Unique
    private static void hotbar(Window window, float tickDelta, Runnable render) {
        element(ElementType.HOTBAR, GuiIngameForge.renderHotbar, window, tickDelta, render);
    }

    @Unique
    private static void element(ElementType type, boolean enabled, Window window, float tickDelta, Runnable render) {
        if (!enabled || MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(tickDelta, window, type))) return;
        render.run();
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(tickDelta, window, type));
    }
}
