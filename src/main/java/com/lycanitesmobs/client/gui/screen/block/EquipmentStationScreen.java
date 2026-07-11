package com.lycanitesmobs.client.gui.screen.block;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.base.BaseContainerScreen;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.container.base.BaseContainer;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.slot.EquipmentStationRepairSlot;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.network.message.MessageTileEntityButton;
import com.lycanitesmobs.core.block.blockentity.EquipmentStationTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


import java.util.List;

public class EquipmentStationScreen extends BaseContainerScreen<EquipmentStationContainer> {
    protected EquipmentStationTileEntity equipmentStation;

    public EquipmentStationScreen(EquipmentStationContainer container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        this.equipmentStation = container.getEquipmentStation();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void initWidgets() {

    }

    @Override
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;

        Identifier texture = TextureManager.getTexture("GUIEquipmentForge");
        guiGraphics.blit(texture, (backX), (backY), (backX) + (this.imageWidth), (backY) + (this.imageHeight), (0) / 256.0F, ((0) + (this.imageWidth)) / 256.0F, (0) / 256.0F, ((0) + (this.imageHeight)) / 256.0F);

        this.drawSlots(guiGraphics, backX, backY);
    }

    /**
     * Draws each Equipment Slot.
     *
     * @param backX
     * @param backY
     */
    protected void drawSlots(GuiGraphicsExtractor guiGraphics, int backX, int backY) {
        Identifier texture = TextureManager.getTexture("GUIEquipmentForge");
        BaseContainer container = this.getMenu();
        List<Slot> forgeSlots = container.getInventorySlotView();
        int slotWidth = 18;
        int slotHeight = 18;
        int slotU = 238;
        int slotVBase = 0;

        for (Slot forgeSlot : forgeSlots) {
            int slotX = backX + forgeSlot.x - 1;
            int slotY = backY + forgeSlot.y - 1;
            int slotV = slotVBase;

            if (forgeSlot instanceof EquipmentStationRepairSlot) {
                slotV += slotHeight * 9;
            }

            guiGraphics.blit(texture, (slotX), (slotY), (slotX) + (slotWidth), (slotY) + (slotHeight), (slotU) / 256.0F, ((slotU) + (slotWidth)) / 256.0F, (slotV) / 256.0F, ((slotV) + (slotHeight)) / 256.0F);
        }
    }


    @Override
    protected void renderForeground(GuiGraphicsExtractor matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.drawHelper.drawString(matrixStack, this.playerInventoryTitle.getString(), this.leftPos + 8, this.topPos + this.imageHeight - 96 + 2, 4210752);
        int backX = (this.width - this.imageWidth) / 2;
        int backY = (this.height - this.imageHeight) / 2;
        this.drawBars(matrixStack, backX, backY);
    }

    protected void drawBars(GuiGraphicsExtractor matrixStack, int backX, int backY) {
        int barWidth = 100;
        int barHeight = 11;
        int barX = (this.width / 2) - (barWidth / 2);
        int barY = backY + 48;
        int manaBarY = barY + 12;
        int barCenter = barX + (barWidth / 2);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, manaBarY, 0, 1, 1, barWidth, barHeight);
        Identifier forgeTexture = TextureManager.getTexture("GUIEquipmentForge");
        matrixStack.blit(forgeTexture, (barX - 14), (barY), (barX - 14) + (13), (barY) + (10), (225) / 256.0F, ((225) + (13)) / 256.0F, (158) / 256.0F, ((158) + (10)) / 256.0F);
        matrixStack.blit(forgeTexture, (barX - 14), (manaBarY), (barX - 14) + (13), (manaBarY) + (10), (225) / 256.0F, ((225) + (13)) / 256.0F, (170) / 256.0F, ((170) + (10)) / 256.0F);

        ItemStack partStack = this.equipmentStation.getItem(1);
        if (!(partStack.getItem() instanceof ItemEquipment)) {
            return;
        }
        ItemEquipment equipmentItem = (ItemEquipment) partStack.getItem();

        // Sharpness:
        int sharpness = equipmentItem.getSharpness(partStack);
        int sharpnessMax = ItemEquipment.SHARPNESS_MAX;
        float sharpnessNormal = (float) sharpness / sharpnessMax;
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, barY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarHealth"), barX, barY, 0, sharpnessNormal, 1, barWidth * sharpnessNormal, barHeight);
        String sharpnessText = sharpness + "/" + sharpnessMax;
        this.drawHelper.drawString(matrixStack, sharpnessText, barCenter - (this.drawHelper.getStringWidth(sharpnessText) / 2), barY + 2, 0xFFFFFF, true);

        // Mana:
        int mana = equipmentItem.getMana(partStack);
        int manaMax = ItemEquipment.SHARPNESS_MAX;
        float manaNormal = (float) mana / manaMax;
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarEmpty"), barX, manaBarY, 0, 1, 1, barWidth, barHeight);
        this.drawHelper.drawTexture(matrixStack, TextureManager.getTexture("GUIPetBarRespawn"), barX, manaBarY, 0, manaNormal, 1, barWidth * manaNormal, barHeight);
        String manaText = mana + "/" + manaMax;
        this.drawHelper.drawString(matrixStack, manaText, barCenter - (this.drawHelper.getStringWidth(manaText) / 2), manaBarY + 2, 0xFFFFFF, true);
    }

    @Override
    public void actionPerformed(int buttonid) {
        MessageTileEntityButton message = new MessageTileEntityButton(buttonid, this.equipmentStation.getBlockPos());
        LycanitesMobs.PACKET_MANAGER.sendToServer(message);
    }
}
