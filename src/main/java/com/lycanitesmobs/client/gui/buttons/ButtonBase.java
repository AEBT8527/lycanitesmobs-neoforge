package com.lycanitesmobs.client.gui.buttons;

import com.lycanitesmobs.client.util.helpers.DrawHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonBase extends Button {
    public DrawHelper drawHelper;
    public int buttonId;



    // ==================================================
    //                    Constructor
    // ==================================================
    public ButtonBase(int buttonId, int x, int y, int width, int height, Component text, Button.OnPress pressable) {
        super(x, y, width, height, text, pressable,Button.DEFAULT_NARRATION);
        Minecraft minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
        this.buttonId = buttonId;
    }

    @Override
    protected void extractContents(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Mirrors vanilla Button.Plain: default sprite + centered label.
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, net.minecraft.client.gui.GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
