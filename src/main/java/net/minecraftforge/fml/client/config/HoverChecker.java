package net.minecraftforge.fml.client.config;

import net.minecraft.client.gui.widget.ButtonWidget;

public class HoverChecker {
    private int top, bottom, left, right, threshold;
    private ButtonWidget button;
    private long hoverStart = -1;

    public HoverChecker(int top, int bottom, int left, int right, int threshold) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.threshold = threshold;
    }

    public HoverChecker(ButtonWidget button, int threshold) {
        this.button = button;
        this.threshold = threshold;
    }

    public void updateBounds(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public boolean checkHover(int mouseX, int mouseY) {
        return checkHover(mouseX, mouseY, true);
    }

    public boolean checkHover(int mouseX, int mouseY, boolean canHover) {
        if (button != null) {
            top = button.y;
            bottom = button.y + org.polyfrost.forgecompat.Hooks.buttonHeight(button);
            left = button.x;
            right = button.x + button.getWidth();
            canHover = canHover && button.visible;
        }
        if (canHover && hoverStart == -1 && mouseY >= top && mouseY <= bottom && mouseX >= left && mouseX <= right) hoverStart = System.currentTimeMillis();
        else if (!canHover || mouseY < top || mouseY > bottom || mouseX < left || mouseX > right) resetHoverTimer();
        return canHover && hoverStart != -1 && System.currentTimeMillis() - hoverStart >= threshold;
    }

    public void resetHoverTimer() {
        hoverStart = -1;
    }
}
