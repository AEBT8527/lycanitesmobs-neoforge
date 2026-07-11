package com.lycanitesmobs.client.tooltip;

import com.lycanitesmobs.core.item.tooltip.EquipmentElementTitleComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class EquipmentElementTitleClientComponent implements ClientTooltipComponent {
    private final EquipmentElementTitleComponent data;
    private int tooltipWidth;

    public EquipmentElementTitleClientComponent(EquipmentElementTitleComponent data) {
        this.data = data;
    }

    public void setTooltipWidth(int tooltipWidth) {
        this.tooltipWidth = tooltipWidth;
    }

    @Override
    public int getHeight(Font font) {
        return 10;
    }

    @Override
    public int getWidth(Font font) {
        return 0;
    }

    @Override
    public void extractImage(Font font, int x, int y, int lycW, int lycH, GuiGraphicsExtractor graphics) {
        if (tooltipWidth <= 0) return;
        int yOffset = -25;

        Component title = Component.translatable("equipment.elements")
                .withStyle(style -> style.withUnderlined(true));

        int textWidth = font.width(title);

        int baseX = x;
        int areaWidth = tooltipWidth;

        if (data.isRightSide()) {
            int colWidth = tooltipWidth / 3;
            baseX = x + colWidth * 2;
            areaWidth = colWidth;
        }

        int textX = baseX + (areaWidth - textWidth) / 2;
        int textY = y + yOffset;

        graphics.textRenderer().accept(textX, textY, title.copy().withColor(0x88CCFF));
    }

}
