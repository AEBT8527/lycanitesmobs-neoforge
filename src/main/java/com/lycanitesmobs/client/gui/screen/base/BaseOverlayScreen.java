package com.lycanitesmobs.client.gui.screen.base;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;


public class BaseOverlayScreen extends BaseGui {
    public int mountMessageTimeMax = 10 * 20;
    public int mountMessageTime = 0;

    // ==================================================
    //                     Constructor
    // ==================================================
    public BaseOverlayScreen(Minecraft minecraft) {
        super(Component.translatable("gui.overlay"));
        // 26.x: Screen.minecraft/font are final and assigned by Screen itself.
    }

    public static BaseOverlayScreen newInstance() {
        return new BaseOverlayScreen(Minecraft.getInstance());
    }

    public static final Identifier GUI_ICONS_LOCATION = Identifier.parse("textures/gui/icons.png");
    public static final Identifier JUMP_BAR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/jump_bar_background");
    public static final Identifier JUMP_BAR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/jump_bar_progress");

}
