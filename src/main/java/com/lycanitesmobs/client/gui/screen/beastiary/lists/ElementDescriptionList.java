package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import net.minecraft.core.registries.BuiltInRegistries;
import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class ElementDescriptionList extends BaseList {
    protected ElementInfo elementInfo;

    /**
     * Constructor
     *
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param top    The y position that the list starts at.
     * @param bottom The y position that the list stops at.
     * @param x      The x position of the list.
     */
    public ElementDescriptionList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
        super(parentGui, width, height, top, bottom, x, 500);
    }

    public void setElementInfo(ElementInfo elementInfo) {
        this.elementInfo = elementInfo;
    }

    @Override
    protected int getMaxPosition() {
        return this.drawHelper.getWordWrappedHeight(this.getContent(), (this.width / 2)) + this.headerHeight;
    }

    @Override
    public void createEntries() {
        this.addEntry(new Entry(this));
    }

    /**
     * List Entry
     */
    public static class Entry extends BaseListEntry {
        private ElementDescriptionList parentList;

        public Entry(ElementDescriptionList parentList) {
            this.parentList = parentList;
        }

        @Override
        public void render(GuiGraphics matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
            if (index == 0) {
                this.parentList.drawHelper.drawStringWrapped(matrixStack, this.parentList.getContent(), left + 6, top, this.parentList.getWidth() - 20, 0xFFFFFF, true);
            }
        }

        @Override
        protected void onClicked() {
        }


    }

    public String getContent() {
        if (this.elementInfo == null) {
            return "";
        }

        MutableComponent text = Component.literal("\u00A7l")
                .append(elementInfo.getTitle())
                .append(": " + "\u00A7r\n")
                .append(elementInfo.getDescription());

        // Buffs:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.elements.buffs"))
                .append(": " + "\u00A7r");

        for (String buff : this.elementInfo.getBuffs()) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(buff));
            if (effect == null) {
                continue;
            }

            ResourceLocation effectResource = ResourceLocation.parse(buff);

            text.append("\n")
                    .append(effect.getDisplayName())
                    .append(": ")
                    .append(Component.translatable("effect." + effectResource.getPath() + ".description"));
        }

        // Debuffs:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.elements.debuffs"))
                .append(": " + "\u00A7r");

        for (String debuff : this.elementInfo.getDebuffs()) {
            if ("burning".equals(debuff)) {
                text.append("\n")
                        .append(Component.translatable("effect.burning"))
                        .append(": ")
                        .append(Component.translatable("effect.burning.description"));
                continue;
            }

            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(debuff));
            if (effect == null) {
                continue;
            }

            ResourceLocation effectResource = ResourceLocation.parse(debuff);

            text.append("\n")
                    .append(effect.getDisplayName())
                    .append(": ")
                    .append(Component.translatable("effect." + effectResource.getPath() + ".description"));
        }

        return text.getString();
    }

}
