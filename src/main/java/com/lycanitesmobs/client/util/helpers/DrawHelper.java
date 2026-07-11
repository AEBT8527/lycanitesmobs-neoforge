package com.lycanitesmobs.client.util.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * GUI drawing utilities. 26.x removed the immediate-mode path this class used
 * (Tesselator + BufferUploader.drawWithShader + RenderSystem shader/texture state),
 * so everything now routes through {@link GuiGraphicsExtractor#blit} which submits
 * into the gui render pipeline directly.
 */
public class DrawHelper {
    protected Minecraft minecraft;
    protected Font fontRenderer;

    public DrawHelper(Minecraft minecraft, Font fontRenderer) {
        this.minecraft = minecraft;
        this.fontRenderer = fontRenderer;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public Font getFontRenderer() {
        return this.fontRenderer;
    }

    public void drawString(GuiGraphicsExtractor guiGraphics, String text, float x, float y, int color, boolean shadow) {
        guiGraphics.textRenderer().accept((int) ((int) x), (int) ((int) y), net.minecraft.network.chat.Component.literal(String.valueOf(text)).withColor(color));
    }

    public void drawString(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int color) {
        this.drawString(guiGraphics, text, x, y, color, false);
    }

    public void drawCenteredString(GuiGraphicsExtractor g, Font f, Component text, int x, int y, int color) {
        g.textRenderer().accept(net.minecraft.client.gui.TextAlignment.CENTER, x, y, text.copy().withColor(color));
    }

    public void drawStringCentered(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int color, boolean shadow) {
        int textWidth = this.getStringWidth(text);
        int textOffset = textWidth / 2;
        this.drawString(guiGraphics, text, x - textOffset, y, color, shadow);
    }

    public int draw(GuiGraphicsExtractor guiGraphics, String string, float x, float y, int color) {
        guiGraphics.textRenderer().accept((int) ((int) x), (int) ((int) y), net.minecraft.network.chat.Component.literal(String.valueOf(string)).withColor(color));
        return this.getStringWidth(string);
    }

    public int drawShadow(GuiGraphicsExtractor guiGraphics, String string, float x, float y, int color) {
        guiGraphics.textRenderer().accept((int) ((int) x), (int) ((int) y), net.minecraft.network.chat.Component.literal(String.valueOf(string)).withColor(color));
        return this.getStringWidth(string);
    }

    public void drawStringWrapped(GuiGraphicsExtractor g, String text, int x, int y, int wrapWidth, int color, boolean shadow) {
        List<FormattedCharSequence> lines = this.getFontRenderer().split(Component.literal(text), wrapWidth);
        int lineY = y;
        for (FormattedCharSequence line : lines) {
            g.textRenderer().accept((int) (x), (int) (lineY), net.minecraft.network.chat.Component.literal(String.valueOf(line)).withColor(color));
            lineY += 10;
        }
    }

    public int getStringWidth(String text) {
        return this.getFontRenderer().width(text);
    }

    public int getWordWrappedHeight(String text, int wrapWidth) {
        return this.getFontRenderer().wordWrapHeight(net.minecraft.network.chat.Component.literal(text), wrapWidth);
    }

    /**
     * Draws a texture stretched to width/height where u/v are the normalized (0..1) max UVs,
     * matching the old Tesselator quad this class used to emit.
     */
    public void drawTexture(GuiGraphicsExtractor g, Identifier texture, float x, float y, float z, float u, float v, float width, float height) {
        g.blit(texture, (int) x, (int) y, (int) (x + width), (int) (y + height), 0.0F, u, 0.0F, v);
    }

    /** Tiled draw: UVs run in texel space against a square texture of the given resolution. */
    public void drawTextureTiled(GuiGraphicsExtractor g, Identifier texture, float x, float y, float z, float u, float v, float width, float height, float resolution) {
        float scale = 0.00390625F * resolution;
        g.blit(texture, (int) x, (int) y, (int) (x + width), (int) (y + height),
                u * scale, (u + width) * scale, v * scale, (v + height) * scale);
    }

    public void drawBar(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, float z, float width, float height, int segments, int segmentLimit) {
        boolean reverse = segmentLimit < 0;
        if (reverse) {
            segmentLimit = -segmentLimit;
        }
        for (int i = 0; i < segments; i++) {
            int currentSegment = i;
            if (reverse) {
                currentSegment = segmentLimit - i - 1;
            }
            this.drawTexture(guiGraphics, texture, x + (width * currentSegment), y, z, 1, 1, width, height);
        }
    }

    /**
     * Draws a texel-space section of a 256x256-style texture (vanilla "modal rect" semantics).
     * The old no-texture overloads relied on globally-bound texture state which no longer
     * exists; callers must now pass the texture explicitly.
     */
    public void drawTexturedModalRect(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height) {
        this.drawTexturedModalRect(g, texture, x, y, u, v, width, height, 1);
    }

    public void drawTexturedModalRect(GuiGraphicsExtractor g, Identifier texture, int x, int y, int u, int v, int width, int height, int resolution) {
        float scale = 0.00390625F * resolution;
        g.blit(texture, x, y, x + width, y + height,
                u * scale, (u + width) * scale, v * scale, (v + height) * scale);
    }
}
