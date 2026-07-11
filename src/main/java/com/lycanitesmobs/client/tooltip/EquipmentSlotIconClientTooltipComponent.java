package com.lycanitesmobs.client.tooltip;

import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.item.tooltip.EquipmentSlotIconTooltipComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class EquipmentSlotIconClientTooltipComponent implements ClientTooltipComponent {
    private final EquipmentSlotIconTooltipComponent data;
    private int tooltipWidth;

    public EquipmentSlotIconClientTooltipComponent(EquipmentSlotIconTooltipComponent data) {
        this.data = data;
    }

    public void setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
    }

    @Override
    public int getHeight(Font font) {
        return 18 + 10;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }


    @Override
    public void extractImage(Font font, int x, int y, int lycW, int lycH, GuiGraphicsExtractor graphics) {
        if (tooltipWidth <= 0) return;

        int iconWidth = 18;
        int iconHeight = 18;

        int left = x + (tooltipWidth - iconWidth) / 2;
        int top = y;

        Identifier texture = TextureManager.getTexture("GUIEquipmentForge");

        int slotU = 238;
        int slotVBase = 0;
        int slotV = slotVBase;

        String type = data.getSlotType();

        if ("base".equals(type)) slotV += iconHeight * 1;
        else if ("head".equals(type)) slotV += iconHeight * 2;
        else if ("blade".equals(type)) slotV += iconHeight * 3;
        else if ("axe".equals(type)) slotV += iconHeight * 4;
        else if ("pike".equals(type)) slotV += iconHeight * 5;
        else if ("pommel".equals(type)) slotV += iconHeight * 6;
        else if ("jewel".equals(type)) slotV += iconHeight * 7;
        else if ("aura".equals(type)) slotV += iconHeight * 8;
        graphics.blit(texture, (left), (top), (left) + (iconWidth), (top) + (iconHeight), (slotU) / 256.0F, ((slotU) + (iconWidth)) / 256.0F, (slotV) / 256.0F, ((slotV) + (iconHeight)) / 256.0F);

        String nameKey = "equipment.slot." + type;
        var nameComp = Component.translatable(nameKey);
        int nameWidth = font.width(nameComp);

        int textX = x + (tooltipWidth - nameWidth) / 2;
        int textY = top + iconHeight + 1;

        graphics.textRenderer().accept(textX, textY, nameComp.copy().withColor(0xFFD700)); // GOLD
    }

}
