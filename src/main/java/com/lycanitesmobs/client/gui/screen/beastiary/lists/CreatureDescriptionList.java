package com.lycanitesmobs.client.gui.screen.beastiary.lists;

import com.lycanitesmobs.client.gui.screen.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureKnowledge;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CreatureDescriptionList extends BaseList {
    protected CreatureKnowledge creatureKnowledge;

    /**
     * Constructor
     *
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param top    The y position that the list starts at.
     * @param bottom The y position that the list stops at.
     * @param x      The x position of the list.
     */
    public CreatureDescriptionList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
        super(parentGui, width, height, top, bottom, x, 500);
    }


    public void setCreatureKnowledge(CreatureKnowledge creatureKnowledge) {
        this.creatureKnowledge = creatureKnowledge;
    }

    @Override
    protected int contentHeight() {
        return this.drawHelper.getWordWrappedHeight(this.getContent(), (this.width / 2)) + 0;
    }

    @Override
    public void createEntries() {
        this.addEntry(new Entry(this));
    }

    /**
     * List Entry
     */
    public static class Entry extends BaseListEntry {
        private CreatureDescriptionList parentList;

        public Entry(CreatureDescriptionList parentList) {
            this.parentList = parentList;
        }

        @Override
        public void render(GuiGraphicsExtractor matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
            if (index == 0) {
                this.parentList.drawHelper.drawStringWrapped(matrixStack, this.parentList.getContent(), left + 6, top, this.parentList.getWidth() - 20, 0xFFFFFF, true);
            }
        }

        @Override
        protected void onClicked() {
        }


    }

    public String getContent() {
        if (this.creatureKnowledge == null) {
            return "";
        }
        CreatureInfo creatureInfo = this.creatureKnowledge.getCreatureInfo();
        if (creatureInfo == null) {
            return "";
        }
        MutableComponent text = Component.literal("");
        CreatureType creatureType = creatureInfo.getCreatureType();

        if (creatureType != null) {
            // Taming:
            if (creatureInfo.isTameable() && creatureType.getTreatItem() != null) {
                text.append("\u00A7l")
                        .append(Component.translatable("gui.beastiary.tameable"))
                        .append(": " + "\u00A7r")
                        .append(Component.translatable(creatureType.getTreatItem().getDescriptionId()))
                        .append("\n");

                // Mounting:
                if (creatureInfo.isMountable()) {
                    text.append("\n\u00A7l")
                            .append(Component.translatable("gui.beastiary.mountable"))
                            .append("\u00A7r\n");
                }
            }

            // Summoning:
            if (creatureInfo.isSummonable()) {
                text.append("\u00A7l")
                        .append(Component.translatable("gui.beastiary.summonable"))
                        .append("\u00A7r\n");
            }

            // Perching:
            if ((creatureInfo.isTameable() || creatureInfo.isSummonable()) && creatureInfo.isPerchable()) {
                text.append("\u00A7l")
                        .append(Component.translatable("gui.beastiary.perchable"))
                        .append("\u00A7r\n");
            }
        }

        if (this.drawHelper.getStringWidth(text.getString()) > 0) {
            text.append("\n");
        }

        // Diet:
        text.append("\u00A7l")
                .append(Component.translatable("gui.beastiary.diet"))
                .append(": " + "\u00A7r")
                .append("\n").append(creatureInfo.getDietNames());

        // Summary:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.summary"))
                .append(": " + "\u00A7r")
                .append("\n")
                .append(creatureInfo.getDescription());

        // Stats:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("creature.stat.base"))
                .append(": " + "\u00A7r");
        if (this.creatureKnowledge.getRank() >= 2) {
            text.append("\n")
                    .append(Component.translatable("creature.stat.health"))
                    .append(": " + creatureInfo.getHealth());
            text.append("\n")
                    .append(Component.translatable("creature.stat.defense"))
                    .append(": " + creatureInfo.getDefense());

            text.append("\n")
                    .append(Component.translatable("creature.stat.speed"))
                    .append(": " + creatureInfo.getSpeed());
            text.append("\n")
                    .append(Component.translatable("creature.stat.damage"))
                    .append(": " + creatureInfo.getDamage());

            text.append("\n")
                    .append(Component.translatable("creature.stat.pierce"))
                    .append(": " + creatureInfo.getPierce());
            MutableComponent effectText = Component.literal(creatureInfo.getEffectDuration() + "s " + creatureInfo.getEffectAmplifier() + "X");
            if (creatureInfo.getEffectDuration() <= 0 || creatureInfo.getEffectAmplifier() < 0) {
                effectText = Component.translatable("common.none");
            }
            text.append("\n")
                    .append(Component.translatable("creature.stat.effect"))
                    .append(": ")
                    .append(effectText);
        } else {
            text.append("\n")
                    .append(Component.translatable("gui.beastiary.unlockedat"))
                    .append(" ")
                    .append(Component.translatable("creature.stat.knowledge"))
                    .append(" " + 2);
        }

        // Combat:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.combat"))
                .append(": " + "\u00A7r");
        if (this.creatureKnowledge.getRank() >= 2) {
            text.append("\n").append(creatureInfo.getCombatDescription());
        } else {
            text.append("\n")
                    .append(Component.translatable("gui.beastiary.unlockedat"))
                    .append(" ")
                    .append(Component.translatable("creature.stat.knowledge"))
                    .append(" " + 2);
        }

        // Habitat:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.habitat"))
                .append(": " + "\u00A7r");
        if (this.creatureKnowledge.getRank() >= 2) {
            text.append("\n")
                    .append(creatureInfo.getHabitatDescription());
        } else {
            text.append("\n")
                    .append(Component.translatable("gui.beastiary.unlockedat"))
                    .append(" ")
                    .append(Component.translatable("creature.stat.knowledge"))
                    .append(" " + 2);
        }

        // Biomes:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.biomes"))
                .append(": " + "\u00A7r");
        if (this.creatureKnowledge.getRank() >= 2) {
            text.append("\n")
                    .append(creatureInfo.getBiomeNames());
        } else {
            text.append("\n")
                    .append(Component.translatable("gui.beastiary.unlockedat"))
                    .append(" ")
                    .append(Component.translatable("creature.stat.knowledge"))
                    .append(" " + 2);
        }

        // Drops:
        text.append("\n\n\u00A7l")
                .append(Component.translatable("gui.beastiary.drops"))
                .append(": " + "\u00A7r");
        if (this.creatureKnowledge.getRank() >= 2) {
            text.append("\n")
                    .append(creatureInfo.getDropNames());
        } else {
            text.append("\n")
                    .append(Component.translatable("gui.beastiary.unlockedat"))
                    .append(" ")
                    .append(Component.translatable("creature.stat.knowledge"))
                    .append(" " + 2);
        }

        return text.getString();
    }
}
