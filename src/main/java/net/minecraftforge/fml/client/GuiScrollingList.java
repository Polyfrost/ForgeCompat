package net.minecraftforge.fml.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class GuiScrollingList {
    private final Minecraft client;
    protected final int listWidth;
    protected final int listHeight;
    protected final int screenWidth;
    protected final int screenHeight;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    protected final int slotHeight;
    private int scrollUpActionId;
    private int scrollDownActionId;
    protected int mouseX;
    protected int mouseY;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    private float scrollDistance;
    protected int selectedIndex = -1;
    private long lastClickTime = 0L;
    private boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;
    protected boolean captureMouse = true;

    @Deprecated
    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight) {
        this(client, width, height, top, bottom, left, entryHeight, width, height);
    }

    public GuiScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void func_27258_a(boolean highlight) {
        highlightSelected = highlight;
    }

    @Deprecated
    protected void func_27259_a(boolean hasFooter, int footerHeight) {
        setHeaderInfo(hasFooter, footerHeight);
    }

    protected void setHeaderInfo(boolean hasHeader, int headerHeight) {
        this.hasHeader = hasHeader;
        this.headerHeight = hasHeader ? headerHeight : 0;
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int index, boolean doubleClick);

    protected abstract boolean isSelected(int index);

    protected int getContentHeight() {
        return getSize() * slotHeight + headerHeight;
    }

    protected abstract void drawBackground();

    protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tesselator tess);

    @Deprecated
    protected void func_27260_a(int entryRight, int relativeY, Tesselator tess) {
    }

    protected void drawHeader(int entryRight, int relativeY, Tesselator tess) {
        func_27260_a(entryRight, relativeY, tess);
    }

    @Deprecated
    protected void func_27255_a(int x, int y) {
    }

    protected void clickHeader(int x, int y) {
        func_27255_a(x, y);
    }

    @Deprecated
    protected void func_27257_b(int mouseX, int mouseY) {
    }

    protected void drawScreen(int mouseX, int mouseY) {
        func_27257_b(mouseX, mouseY);
    }

    public int func_27256_c(int x, int y) {
        int left = this.left + 1;
        int right = this.left + listWidth - 7;
        int relativeY = y - top - headerHeight + (int) scrollDistance - 4;
        int entryIndex = relativeY / slotHeight;
        return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < getSize() ? entryIndex : -1;
    }

    public void registerScrollButtons(List buttons, int upActionID, int downActionID) {
        scrollUpActionId = upActionID;
        scrollDownActionId = downActionID;
    }

    private void applyScrollLimits() {
        int listHeight = getContentHeight() - (bottom - top - 4);
        if (listHeight < 0) listHeight /= 2;
        if (scrollDistance < 0.0F) scrollDistance = 0.0F;
        if (scrollDistance > listHeight) scrollDistance = listHeight;
    }

    public void actionPerformed(ButtonWidget button) {
        if (!button.active) return;
        if (button.id == scrollUpActionId) {
            scrollDistance -= slotHeight * 2 / 3;
            initialMouseClickY = -2.0F;
            applyScrollLimits();
        } else if (button.id == scrollDownActionId) {
            scrollDistance += slotHeight * 2 / 3;
            initialMouseClickY = -2.0F;
            applyScrollLimits();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        drawBackground();
        boolean isHovering = mouseX >= left && mouseX <= left + listWidth && mouseY >= top && mouseY <= bottom;
        int listLength = getSize();
        int scrollBarRight = left + listWidth;
        int scrollBarLeft = scrollBarRight - 6;
        int entryLeft = left;
        int entryRight = scrollBarLeft - 1;
        int viewHeight = bottom - top;
        int border = 4;

        if (Mouse.isButtonDown(0)) {
            if (initialMouseClickY == -1.0F) {
                if (isHovering) {
                    int mouseListY = mouseY - top - headerHeight + (int) scrollDistance - border;
                    int slotIndex = mouseListY / slotHeight;
                    if (mouseX >= entryLeft && mouseX <= entryRight && slotIndex >= 0 && mouseListY >= 0 && slotIndex < listLength) {
                        elementClicked(slotIndex, slotIndex == selectedIndex && System.currentTimeMillis() - lastClickTime < 250L);
                        selectedIndex = slotIndex;
                        lastClickTime = System.currentTimeMillis();
                    } else if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY < 0) {
                        clickHeader(mouseX - entryLeft, mouseY - top + (int) scrollDistance - border);
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
                        scrollFactor = -1.0F;
                        int scrollHeight = Math.max(getContentHeight() - viewHeight - border, 1);
                        int barHeight = Math.min(Math.max((int) ((float) (viewHeight * viewHeight) / getContentHeight()), 32), viewHeight - border * 2);
                        scrollFactor /= (float) (viewHeight - barHeight) / scrollHeight;
                    } else {
                        scrollFactor = 1.0F;
                    }
                    initialMouseClickY = mouseY;
                } else {
                    initialMouseClickY = -2.0F;
                }
            } else if (initialMouseClickY >= 0.0F) {
                scrollDistance -= (mouseY - initialMouseClickY) * scrollFactor;
                initialMouseClickY = mouseY;
            }
        } else {
            while (isHovering && Mouse.next()) {
                int scroll = Mouse.getEventDWheel();
                if (scroll != 0) scrollDistance += (scroll > 0 ? -1 : 1) * slotHeight / 2;
            }
            initialMouseClickY = -1.0F;
        }

        applyScrollLimits();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        Window res = new Window(client);
        double scaleW = client.width / res.getScaledWidth();
        double scaleH = client.height / res.getScaledHeight();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (left * scaleW), (int) (client.height - bottom * scaleH), (int) (listWidth * scaleW), (int) (viewHeight * scaleH));

        if (client.world != null) {
            drawGradientRect(left, top, right, bottom, 0xC0101010, 0xD0101010);
        } else {
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            client.getTextureManager().bind(GuiElement.BACKGROUND_LOCATION);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            float scale = 32.0F;
            buf.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
            buf.vertex(left, bottom, 0.0).texture(left / scale, (bottom + (int) scrollDistance) / scale).color(32, 32, 32, 255).nextVertex();
            buf.vertex(right, bottom, 0.0).texture(right / scale, (bottom + (int) scrollDistance) / scale).color(32, 32, 32, 255).nextVertex();
            buf.vertex(right, top, 0.0).texture(right / scale, (top + (int) scrollDistance) / scale).color(32, 32, 32, 255).nextVertex();
            buf.vertex(left, top, 0.0).texture(left / scale, (top + (int) scrollDistance) / scale).color(32, 32, 32, 255).nextVertex();
            tess.end();
        }

        int baseY = top + border - (int) scrollDistance;
        if (hasHeader) drawHeader(entryRight, baseY, tess);

        for (int slotIdx = 0; slotIdx < listLength; slotIdx++) {
            int slotTop = baseY + slotIdx * slotHeight + headerHeight;
            int slotBuffer = slotHeight - border;
            if (slotTop > bottom || slotTop + slotBuffer < top) continue;
            if (highlightSelected && isSelected(slotIdx)) {
                int min = left;
                int max = entryRight;
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture();
                buf.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
                buf.vertex(min, slotTop + slotBuffer + 2, 0.0).texture(0.0, 1.0).color(128, 128, 128, 255).nextVertex();
                buf.vertex(max, slotTop + slotBuffer + 2, 0.0).texture(1.0, 1.0).color(128, 128, 128, 255).nextVertex();
                buf.vertex(max, slotTop - 2, 0.0).texture(1.0, 0.0).color(128, 128, 128, 255).nextVertex();
                buf.vertex(min, slotTop - 2, 0.0).texture(0.0, 0.0).color(128, 128, 128, 255).nextVertex();
                buf.vertex(min + 1, slotTop + slotBuffer + 1, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).nextVertex();
                buf.vertex(max - 1, slotTop + slotBuffer + 1, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).nextVertex();
                buf.vertex(max - 1, slotTop - 1, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).nextVertex();
                buf.vertex(min + 1, slotTop - 1, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).nextVertex();
                tess.end();
                GlStateManager.enableTexture();
            }
            drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
        }

        GlStateManager.disableDepthTest();
        int extraHeight = getContentHeight() - viewHeight - border;
        if (extraHeight > 0) {
            int height = Math.min(Math.max(viewHeight * viewHeight / getContentHeight(), 32), viewHeight - border * 2);
            int barTop = Math.max((int) scrollDistance * (viewHeight - height) / extraHeight + top, top);
            GlStateManager.disableTexture();
            quad(tess, buf, scrollBarLeft, top, scrollBarRight, bottom, 0);
            quad(tess, buf, scrollBarLeft, barTop, scrollBarRight, barTop + height, 128);
            quad(tess, buf, scrollBarLeft, barTop, scrollBarRight - 1, barTop + height - 1, 192);
        }

        drawScreen(mouseX, mouseY);
        GlStateManager.enableTexture();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private static void quad(Tesselator tess, BufferBuilder buf, int x1, int y1, int x2, int y2, int gray) {
        buf.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        buf.vertex(x1, y2, 0.0).texture(0.0, 1.0).color(gray, gray, gray, 255).nextVertex();
        buf.vertex(x2, y2, 0.0).texture(1.0, 1.0).color(gray, gray, gray, 255).nextVertex();
        buf.vertex(x2, y1, 0.0).texture(1.0, 0.0).color(gray, gray, gray, 255).nextVertex();
        buf.vertex(x1, y1, 0.0).texture(0.0, 0.0).color(gray, gray, gray, 255).nextVertex();
        tess.end();
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
        float a1 = (color1 >> 24 & 0xFF) / 255.0F, r1 = (color1 >> 16 & 0xFF) / 255.0F, g1 = (color1 >> 8 & 0xFF) / 255.0F, b1 = (color1 & 0xFF) / 255.0F;
        float a2 = (color2 >> 24 & 0xFF) / 255.0F, r2 = (color2 >> 16 & 0xFF) / 255.0F, g2 = (color2 >> 8 & 0xFF) / 255.0F, b2 = (color2 & 0xFF) / 255.0F;
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(7, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(right, top, 0.0).color(r1, g1, b1, a1).nextVertex();
        buf.vertex(left, top, 0.0).color(r1, g1, b1, a1).nextVertex();
        buf.vertex(left, bottom, 0.0).color(r2, g2, b2, a2).nextVertex();
        buf.vertex(right, bottom, 0.0).color(r2, g2, b2, a2).nextVertex();
        tess.end();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }
}
