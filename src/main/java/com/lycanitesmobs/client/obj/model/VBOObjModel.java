package com.lycanitesmobs.client.obj.model;

import com.lycanitesmobs.client.model.creature.base.ModelObjState;
import com.lycanitesmobs.client.obj.geometry.ObjPart;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * 26.x: the VBO fast path (custom VertexBuffer batching + POS_TEX_NORMAL shader + Iris entity
 * format fallback) is gone with the old render system; everything renders through the vanilla
 * VertexConsumer path in {@link ObjModel#renderPart}. The static config fields remain because
 * renderers still set them, but they no longer switch render paths.
 */
public class VBOObjModel extends ObjModel {

    public static RenderType renderType;
    public static Identifier tex;
    public static boolean renderNormal;
    public static boolean renderOutline;
    public static boolean useEntityFormat;
    public static RenderType irisRenderType;
    public static int irisBlending;
    public static boolean irisGlow;
    public static boolean deferFlush;

    public float baseR = 1.0F, baseG = 1.0F, baseB = 1.0F, baseRange = 0.1F;
    public float variantR = 1.0F, variantG = 1.0F, variantB = 1.0F, variantAmount = 0.0F;

    public VBOObjModel(Identifier resourceLocation) {
        super(resourceLocation);
    }

    public VBOObjModel(Identifier resourceLocation, ResourceManager resourceManager) {
        super(resourceLocation, resourceManager);
    }

    public void applyVariantFromState(ModelObjState state) {
        if (state == null) return;
        this.baseR = state.baseR;
        this.baseG = state.baseG;
        this.baseB = state.baseB;
        this.baseRange = state.baseRange;
        this.variantR = state.variantR;
        this.variantG = state.variantG;
        this.variantB = state.variantB;
        this.variantAmount = state.variantAmount;
    }

    @Override
    public void renderPart(VertexConsumer vertexBuilder, Matrix3f matrix3f, Matrix4f matrix4f,
                           int brightness, int fade, ObjPart objPart, Vector4f color, Vector2f textureOffset) {
        super.renderPart(vertexBuilder, matrix3f, matrix4f, brightness, fade, objPart, color, textureOffset);
    }

    public static float[] hexToRGB(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255.0F;
        float g = ((hex >> 8) & 0xFF) / 255.0F;
        float b = (hex & 0xFF) / 255.0F;
        return new float[]{r, g, b};
    }
}
