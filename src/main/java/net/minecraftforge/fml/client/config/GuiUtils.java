package net.minecraftforge.fml.client.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import org.lwjgl.opengl.GL11;

public class GuiUtils {
    public static void drawTexturedModalRect(int x, int y, int u, int v, int width, int height, float zLevel) {
        float uScale = 1f / 0x100;
        float vScale = 1f / 0x100;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder wr = tessellator.getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX);
        wr.vertex(x, y + height, zLevel).texture(u * uScale, (v + height) * vScale).nextVertex();
        wr.vertex(x + width, y + height, zLevel).texture((u + width) * uScale, (v + height) * vScale).nextVertex();
        wr.vertex(x + width, y, zLevel).texture((u + width) * uScale, v * vScale).nextVertex();
        wr.vertex(x, y, zLevel).texture(u * uScale, v * vScale).nextVertex();
        tessellator.end();
    }

    public static void drawContinuousTexturedBox(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;
        drawTexturedModalRect(x, y, u, v, leftBorder, topBorder, zLevel);
        drawTexturedModalRect(x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
        drawTexturedModalRect(x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
        drawTexturedModalRect(x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);
        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
            int w = i == xPasses ? remainderWidth : fillerWidth;
            drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, w, topBorder, zLevel);
            drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, w, bottomBorder, zLevel);
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
                drawTexturedModalRect(x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, w, j == yPasses ? remainderHeight : fillerHeight, zLevel);
        }
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
            int h = j == yPasses ? remainderHeight : fillerHeight;
            drawTexturedModalRect(x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, h, zLevel);
            drawTexturedModalRect(x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, h, zLevel);
        }
    }

    public static void drawHoveringText(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, TextRenderer font) {
        if (textLines.isEmpty()) return;
        GlStateManager.disableRescaleNormal();
        Lighting.turnOff();
        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        int tooltipTextWidth = 0;
        for (String textLine : textLines) tooltipTextWidth = Math.max(tooltipTextWidth, font.getWidth(textLine));
        boolean needsWrap = false;
        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) {
                tooltipTextWidth = mouseX > screenWidth / 2 ? mouseX - 12 - 8 : screenWidth - 16 - mouseX;
                needsWrap = true;
            }
        }
        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }
        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<>();
            for (int i = 0; i < textLines.size(); i++) {
                List<String> wrappedLine = font.split(textLines.get(i), tooltipTextWidth);
                if (i == 0) titleLinesCount = wrappedLine.size();
                for (String line : wrappedLine) {
                    wrappedTooltipWidth = Math.max(wrappedTooltipWidth, font.getWidth(line));
                    wrappedTextLines.add(line);
                }
            }
            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;
            tooltipX = mouseX > screenWidth / 2 ? mouseX - 16 - tooltipTextWidth : mouseX + 12;
        }
        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;
        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;
            if (textLines.size() > titleLinesCount) tooltipHeight += 2;
        }
        if (tooltipY + tooltipHeight + 6 > screenHeight) tooltipY = screenHeight - tooltipHeight - 6;
        int zLevel = 300;
        int bg = 0xF0100010;
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, bg, bg);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, bg, bg);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, bg, bg);
        drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, bg, bg);
        drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, bg, bg);
        int borderStart = 0x505000FF;
        int borderEnd = (borderStart & 0xFEFEFE) >> 1 | borderStart & 0xFF000000;
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderStart, borderEnd);
        drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderStart, borderEnd);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderStart, borderStart);
        drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderEnd, borderEnd);
        for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
            font.drawWithShadow(textLines.get(lineNumber), (float) tooltipX, (float) tooltipY, -1);
            if (lineNumber + 1 == titleLinesCount) tooltipY += 2;
            tooltipY += 10;
        }
        GlStateManager.enableLighting();
        GlStateManager.enableDepthTest();
        Lighting.turnOn();
        GlStateManager.enableRescaleNormal();
    }

    public static void drawGradientRect(int zLevel, int left, int top, int right, int bottom, int startColor, int endColor) {
        float sa = (startColor >> 24 & 255) / 255.0F, sr = (startColor >> 16 & 255) / 255.0F, sg = (startColor >> 8 & 255) / 255.0F, sb = (startColor & 255) / 255.0F;
        float ea = (endColor >> 24 & 255) / 255.0F, er = (endColor >> 16 & 255) / 255.0F, eg = (endColor >> 8 & 255) / 255.0F, eb = (endColor & 255) / 255.0F;
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder wr = tessellator.getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
        wr.vertex(right, top, zLevel).color(sr, sg, sb, sa).nextVertex();
        wr.vertex(left, top, zLevel).color(sr, sg, sb, sa).nextVertex();
        wr.vertex(left, bottom, zLevel).color(er, eg, eb, ea).nextVertex();
        wr.vertex(right, bottom, zLevel).color(er, eg, eb, ea).nextVertex();
        tessellator.end();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableTexture();
    }
}
