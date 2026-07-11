package com.lycanitesmobs.client.gui.screen.base;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public abstract class BaseGui extends Screen {
    public BaseGui(Component screenName) {
        super(screenName);
    }

    public static void renderLivingEntityRotated(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            int scale,
            float yawDegrees,
            float pitchDegrees,
            LivingEntity entity
    ) {
        // TODO(26.x): GUI entity previews used the removed 3D dispatcher path
        // (EntityRenderDispatcher.render + RenderSystem.runAsFancy + Lighting.setupForEntityInInventory).
        // 26 renders GUI entities via submitted render states; reimplement with the new
        // GuiGraphics entity submission API. Previews are disabled until then.
    }

    public static void renderLivingEntity(GuiGraphicsExtractor guiGraphics, int x, int y, float scale, float lookX, float lookY, LivingEntity entity) {
        // TODO(26.x): GUI entity previews used the removed 3D dispatcher path
        // (EntityRenderDispatcher.render + RenderSystem.runAsFancy + Lighting.setupForEntityInInventory).
        // 26 renders GUI entities via submitted render states; reimplement with the new
        // GuiGraphics entity submission API. Previews are disabled until then.
    }
}
