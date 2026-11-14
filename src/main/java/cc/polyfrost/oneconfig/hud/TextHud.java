package cc.polyfrost.oneconfig.hud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;

public abstract class TextHud {
    public boolean enabled;
    public float x;
    public float y;
    public float scale = 1f;
    protected transient List<String> lines = new ArrayList<>();

    public TextHud(boolean enabled, int x, int y) {
        this.enabled = enabled;
        this.x = x;
        this.y = y;
    }

    public TextHud(boolean enabled) {
        this(enabled, 0, 0);
    }

    protected abstract void getLines(List<String> lines, boolean example);

    protected void getLinesFrequent(List<String> lines, boolean example) {
    }

    protected boolean shouldShow() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public final void forgecompat$tick(boolean example) {
        lines.clear();
        if (enabled || example) getLines(lines, example);
    }

    public final void forgecompat$render(boolean example) {
        if (!enabled && !example) return;
        getLinesFrequent(lines, example);
        if (lines.isEmpty()) return;
        TextRenderer font = Minecraft.getInstance().textRenderer;
        GlStateManager.pushMatrix();
        GlStateManager.translatef(x, y, 0f);
        GlStateManager.scalef(scale, scale, 1f);
        int lineY = 0;
        for (String line : lines) {
            font.draw(line, 0f, lineY, 0xFFFFFFFF, false);
            lineY += 12;
        }
        GlStateManager.popMatrix();
    }

    public final float forgecompat$width() {
        TextRenderer font = Minecraft.getInstance().textRenderer;
        int width = 0;
        for (String line : lines) width = Math.max(width, font.getWidth(line));
        return width * scale;
    }

    public final float forgecompat$height() {
        return lines.isEmpty() ? 0f : (lines.size() * 12 - 4) * scale;
    }
}
