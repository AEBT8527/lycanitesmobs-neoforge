package com.lycanitesmobs.client.gui.screen.base;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public class BaseOverlayScreen extends BaseGui {
    public int mountMessageTimeMax = 10 * 20;
    public int mountMessageTime = 0;

    // ==================================================
    //                     Constructor
    // ==================================================
    public BaseOverlayScreen(Minecraft minecraft) {
        super(Component.translatable("gui.overlay"));
        // This screen is used as a HUD helper without going through Screen.init(...), which is
        // what normally assigns Screen.minecraft/font — set them here so getMinecraft() etc. work.
        this.minecraft = minecraft;
        this.font = minecraft.font;
    }

    public static BaseOverlayScreen newInstance() {
        return new BaseOverlayScreen(Minecraft.getInstance());
    }

    // 1.21 split the old icons.png into individual HUD sprites; the jump/stamina bar is drawn
    // via GuiGraphics.blitSprite with the sprites below instead of UV offsets into this texture.
    public static final ResourceLocation GUI_ICONS_LOCATION = ResourceLocation.parse("textures/gui/icons.png");
    public static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_background");
    public static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_progress");

}
