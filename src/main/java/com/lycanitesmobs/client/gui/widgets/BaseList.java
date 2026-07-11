package com.lycanitesmobs.client.gui.widgets;

import com.lycanitesmobs.client.util.helpers.DrawHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public abstract class BaseList<S> extends AbstractSelectionList<BaseListEntry> {
    public DrawHelper drawHelper;
    public S screen;

    public BaseList(S screen, int width, int height, int top, int bottom, int left, int slotHeight) {
        super(Minecraft.getInstance(), width, bottom - top, top, slotHeight);
        Minecraft minecraft = Minecraft.getInstance();
        this.drawHelper = new DrawHelper(minecraft, minecraft.font);
        this.setX(left);
        this.screen = screen;
        this.createEntries();
    }

    public BaseList(S screen, int width, int height, int top, int bottom, int left) {
        this(screen, width, height, top, bottom, left, 28);
    }

    @Override
    public int getRowWidth() {
        return this.getWidth();
    }

    protected int getScrollbarWidth() {
        return 6;
    }

    @Override
    protected int scrollBarX() {
        return this.getScrollbarPosition();
    }

    protected int getScrollbarPosition() {
        return this.getRight() - this.getScrollbarWidth();
    }

    /**
     * Creates all List Entries for this List Widget.
     */
    public void createEntries() {
    }

    /**
     * Returns the index of the selected entry.
     *
     * @return The selected entry index, defaults to 0 if none are selected.
     */
    public int getSelectedIndex() {
        if (this.getSelected() != null)
            return this.getSelected().index;
        return 0;
    }

    /**
     * 1.21 GUI rework: the old hand-rolled Tesselator/BufferBuilder quads are replaced with
     * GuiGraphicsExtractor fills, and render() is final on AbstractWidget so we override renderWidget.
     */
    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());
        try {
            // Darkened list background.
            guiGraphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0x40000000);

            int listLeft = this.getRowLeft();
            int listTop = this.getY() + 4 - (int) this.scrollAmount();
            if (this.isFocused()) {
                this.renderHeader(guiGraphics, listLeft, listTop);
            }

            try {
                this.extractListItems(guiGraphics, mouseX, mouseY, partialTicks);
            } catch (Exception e) {
                LMHelperClass.logError("BaseList renderList error: " + e.getMessage());
            }

            int maxScroll = this.maxScrollAmount();
            if (maxScroll > 0) {
                int trackHeight = this.getBottom() - this.getY();
                int handleHeight = (int) ((float) trackHeight * trackHeight / (float) this.contentHeight());
                handleHeight = LMHelperClass.convertToInteger(LMHelperClass.clamp(handleHeight, 32, trackHeight - 8));
                int handleY = (int) this.scrollAmount() * (trackHeight - handleHeight) / maxScroll + this.getY();
                if (handleY < this.getY()) {
                    handleY = this.getY();
                }

                int scrollbarLeft = this.getScrollbarPosition();
                int scrollbarRight = scrollbarLeft + this.getScrollbarWidth();

                guiGraphics.fill(scrollbarLeft, this.getY(), scrollbarRight, this.getBottom(), 0xFF000000);
                guiGraphics.fill(scrollbarLeft, handleY, scrollbarRight, handleY + handleHeight, 0xFF808080);
                guiGraphics.fill(scrollbarLeft, handleY, scrollbarRight - 1, handleY + handleHeight - 1, 0xFFC0C0C0);
            }

            this.renderDecorations(guiGraphics, mouseX, mouseY);
        } finally {
            guiGraphics.disableScissor();
        }
    }

    /** Legacy hook; vanilla renderHeader was removed in 26.x. */
    protected void renderHeader(GuiGraphicsExtractor guiGraphics, int left, int top) {
    }

    /** Legacy hook; vanilla renderDecorations was removed in 26.x. */
    protected void renderDecorations(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
