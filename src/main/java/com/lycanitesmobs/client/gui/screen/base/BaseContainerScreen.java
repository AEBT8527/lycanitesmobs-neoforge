package com.lycanitesmobs.client.gui.screen.base;

import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.client.util.helpers.DrawHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements Button.OnPress {
    public DrawHelper drawHelper;

    public BaseContainerScreen(T container, Inventory playerInventory, Component name) {
        // imageWidth/imageHeight are final in 26.x and set via the super constructor;
        // every Lycanites container screen used the standard 176x166.
        super(container, playerInventory, name, 176, 166);
    }

    /**
     * Secondary init method called by main init method.
     */
    @Override
    protected void init() {
        super.init();

        this.drawHelper = new DrawHelper(minecraft, minecraft.font);

        this.initWidgets();
    }

    /**
     * Initialises all buttons and other widgets that this Screen uses.
     */
    protected abstract void initWidgets();

    /**
     * Draws and updates the GUI.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderWidgets(guiGraphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderForeground(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Draws the background image.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    public abstract void renderBackground(GuiGraphicsExtractor matrixStack, int mouseX, int mouseY, float partialTicks);


    /**
     * Updates widgets like buttons and other controls for this screen. Super renders the button list, called after this.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected void renderWidgets(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < this.renderables.size(); ++i) {
            this.renderables.get(i).extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Draws foreground elements.
     *
     * @param mouseX       The x position of the mouse cursor.
     * @param mouseY       The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected abstract void renderForeground(GuiGraphicsExtractor matrixStack, int mouseX, int mouseY, float partialTicks);

    @Override
    public void onPress(Button guiButton) {
        if (!(guiButton instanceof ButtonBase)) {
            return;
        }
        this.actionPerformed(((ButtonBase) guiButton).buttonId);
    }

    /**
     * Called when a Button Base is pressed providing the press button's id.
     *
     * @param buttonId The id of the button pressed.
     */
    public abstract void actionPerformed(int buttonId);
}
