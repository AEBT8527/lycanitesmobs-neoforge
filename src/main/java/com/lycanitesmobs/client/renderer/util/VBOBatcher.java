package com.lycanitesmobs.client.renderer.util;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * 26.x: the custom VBO batching pipeline (VertexBuffer + GlStateManager + custom shader) was
 * removed with the old render system. OBJ parts are now emitted straight into vanilla entity
 * VertexConsumers (see ObjModel.renderPart / Mesh.emit), so this batcher is an inert stub that
 * keeps the old call sites compiling; queued commands are simply ignored.
 */
public class VBOBatcher {
    private static final VBOBatcher INSTANCE = new VBOBatcher();

    public static VBOBatcher getInstance() {
        return INSTANCE;
    }

    public void queue(RenderType renderType, VBODrawCommand command) {
    }

    public void queueDeferred(RenderType renderType, VBODrawCommand command) {
    }

    public void endBatches() {
    }

    public void endDeferredBatches() {
    }

    /** Data-only remnant of the old per-draw command; kept so legacy call sites compile. */
    public static class VBODrawCommand {
        public VBODrawCommand(Object vbo, int indexCount, Object format, Matrix4f matrix, Identifier texture) {
        }

        public VBODrawCommand setColor(Vector4f color) { return this; }
        public VBODrawCommand setTextureOffset(org.joml.Vector2f offset) { return this; }
        public VBODrawCommand setOverlayOffset(float u, float v) { return this; }
        public VBODrawCommand setLightOffset(int light) { return this; }
        public VBODrawCommand setRecolorBand(float r, float g, float b, float range) { return this; }
        public VBODrawCommand setVariantTint(float r, float g, float b, float amount) { return this; }
        public VBODrawCommand setAlphaCutoff(float cutoff) { return this; }
    }
}
