package net.minecraftforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.render.Window;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class GuiIngameForge extends GameGui {
    public static boolean renderHelmet = true, renderPortal = true, renderHotbar = true, renderCrosshairs = true,
            renderBossHealth = true, renderHealth = true, renderArmor = true, renderFood = true, renderHealthMount = true,
            renderAir = true, renderExperiance = true, renderJumpBar = true, renderObjective = true;
    public static int left_height = 39;
    public static int right_height = 39;
    private RenderGameOverlayEvent eventParent;

    public GuiIngameForge(Minecraft minecraft) {
        super(minecraft);
    }

    @Override
    public void render(float tickDelta) {
        renderGameOverlay(tickDelta);
    }

    public void renderGameOverlay(float tickDelta) {
        super.render(tickDelta);
        Window window = new Window(Minecraft.getInstance());
        renderTitle(window.getWidth(), window.getHeight(), tickDelta);
    }

    protected void renderTitle(int width, int height, float tickDelta) {
    }

    protected void renderExperience(int width, int height) {
    }

    protected void renderHealth(int width, int height) {
    }

    protected void renderFood(int width, int height) {
    }
}
