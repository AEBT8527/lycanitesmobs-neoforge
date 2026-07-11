package com.lycanitesmobs.client.manager;

import com.lycanitesmobs.client.util.helpers.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;

public class OverlayManager {
    public static OverlayManager instance = new OverlayManager();
    public DrawHelper drawHelper;
    public Minecraft minecraft;

    public OverlayManager() {
        this.minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
    }

    public static OverlayManager getInstance() {
        return instance;
    }

    /**
     * Plain holder for debug-overlay text lines. Forge's {@code RenderGuiOverlayEvent} (which this
     * used to extend) was removed in NeoForge; nothing posts this, so it now just carries the data.
     */
    public static class Text {
        private final GuiGraphicsExtractor guiGraphics;
        private final ArrayList<String> left;
        private final ArrayList<String> right;

        public Text(GuiGraphicsExtractor guiGraphics, ArrayList<String> left, ArrayList<String> right) {
            this.guiGraphics = guiGraphics;
            this.left = left;
            this.right = right;
        }

        public GuiGraphicsExtractor getGuiGraphics() {
            return this.guiGraphics;
        }

        public ArrayList<String> getLeft() {
            return this.left;
        }

        public ArrayList<String> getRight() {
            return this.right;
        }
    }
}
