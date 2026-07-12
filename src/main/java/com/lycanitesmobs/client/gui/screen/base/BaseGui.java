package com.lycanitesmobs.client.gui.screen.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class BaseGui extends Screen {
    public BaseGui(Component screenName) {
        super(screenName);
    }

    /**
     * Renders a creature preview at (x, y) that follows the mouse.
     * <p>
     * lookX/lookY are (screenPos - mouse), matching the mod's legacy convention; they are
     * turned into small look angles the same way vanilla's inventory preview does.
     */
    public static void renderLivingEntity(GuiGraphicsExtractor guiGraphics, int x, int y, float scale, float lookX, float lookY, LivingEntity entity) {
        float yawDegrees = (float) Math.atan(lookX / 40.0F) * 20.0F;
        float pitchDegrees = (float) Math.atan(lookY / 40.0F) * 20.0F;
        renderLivingEntityRotated(guiGraphics, x, y, (int) scale, yawDegrees, pitchDegrees, entity);
    }

    /**
     * Renders a creature preview at (x, y) with explicit yaw/pitch.
     * <p>
     * 26.x renders GUI entities through {@link GuiGraphicsExtractor#entity} from an extracted
     * render state. Lycanites' {@code CreatureRenderer} produces a valid {@code CreatureRenderState}
     * whose submit path reads the live entity, so we set the entity's facing here and let the
     * renderer draw it. Feet land at (x, y); the box is sized from {@code scale}.
     */
    public static void renderLivingEntityRotated(GuiGraphicsExtractor guiGraphics, int x, int y, int scale, float yawDegrees, float pitchDegrees, LivingEntity entity) {
        if (entity == null || scale <= 0) {
            return;
        }

        // Face the viewer and follow the look angles. CreatureRenderer derives its render yaw
        // from the entity's body rotation, so drive it directly here.
        entity.yBodyRotO = entity.yBodyRot = yawDegrees;
        entity.yHeadRotO = entity.yHeadRot = yawDegrees;
        entity.setYRot(yawDegrees);
        entity.setYHeadRot(yawDegrees);
        entity.xRotO = entity.xRot = pitchDegrees;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(entity);
        EntityRenderState renderState;
        try {
            renderState = renderer.createRenderState(entity, 1.0F);
        } catch (Exception e) {
            return;
        }
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;

        // Box centred horizontally on x, feet at y, extending upward.
        int x0 = x - scale;
        int y0 = y - scale * 2;
        int x1 = x + scale;
        int y1 = y;
        float height = renderState.boundingBoxHeight <= 0 ? 1.0F : renderState.boundingBoxHeight;
        Vector3f translation = new Vector3f(0.0F, height / 2.0F, 0.0F);
        // Identity rotation: CreatureRenderer applies its own model orientation/flip.
        Quaternionf rotation = new Quaternionf();

        guiGraphics.entity(renderState, scale, translation, rotation, null, x0, y0, x1, y1);
    }
}
