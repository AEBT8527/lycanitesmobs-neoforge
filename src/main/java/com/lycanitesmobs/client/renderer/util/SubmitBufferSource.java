package com.lycanitesmobs.client.renderer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridge between the mod's legacy {@link MultiBufferSource}-driven render code and 26.x's
 * submit-based entity rendering.
 * <p>
 * Legacy model code asks for a {@link VertexConsumer} per {@link RenderType} and emits vertices
 * immediately; the new pipeline instead wants geometry submitted via
 * {@link SubmitNodeCollector#submitCustomGeometry}. This class captures emitted vertices in RAM
 * (positions are already fully transformed by the model's pose multiplications) and replays them
 * inside the deferred custom-geometry callbacks, preserving per-RenderType submission order.
 */
public class SubmitBufferSource implements MultiBufferSource {
    private final List<RenderType> order = new ArrayList<>();
    private final List<CapturingConsumer> captures = new ArrayList<>();

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        // Preserve draw order; merging same-renderType requests would reorder translucent parts.
        CapturingConsumer capture = new CapturingConsumer();
        this.order.add(renderType);
        this.captures.add(capture);
        return capture;
    }

    /** Submit everything captured so far and reset for reuse. */
    public void submitAll(PoseStack poseStack, SubmitNodeCollector collector) {
        for (int i = 0; i < this.order.size(); i++) {
            CapturingConsumer capture = this.captures.get(i);
            if (capture.vertexCount == 0) {
                continue;
            }
            collector.submitCustomGeometry(poseStack, this.order.get(i), capture::replay);
        }
        this.order.clear();
        this.captures.clear();
    }

    /**
     * Records the raw vertex stream. Model code transforms positions itself (via the
     * addVertex(Matrix4f,...) default overloads which resolve to the primitive calls below),
     * so replay just re-emits the primitives without further transformation.
     */
    static class CapturingConsumer implements VertexConsumer {
        // 3 pos + 1 color + 2 uv + 1 uv1 + 1 uv2 + 3 normal = 11 floats/ints per vertex slot
        private static final int STRIDE = 11;
        private float[] data = new float[STRIDE * 512];
        int vertexCount = 0;
        private int base = -STRIDE;

        private void grow() {
            if ((this.vertexCount + 1) * STRIDE > this.data.length) {
                float[] bigger = new float[this.data.length * 2];
                System.arraycopy(this.data, 0, bigger, 0, this.data.length);
                this.data = bigger;
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.grow();
            this.base = this.vertexCount * STRIDE;
            this.vertexCount++;
            float[] d = this.data;
            d[this.base] = x;
            d[this.base + 1] = y;
            d[this.base + 2] = z;
            d[this.base + 3] = Float.intBitsToFloat(0xFFFFFFFF); // default white
            d[this.base + 4] = 0f;
            d[this.base + 5] = 0f;
            d[this.base + 6] = Float.intBitsToFloat(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
            d[this.base + 7] = Float.intBitsToFloat(0x00F000F0); // full-bright default light
            d[this.base + 8] = 0f;
            d[this.base + 9] = 1f;
            d[this.base + 10] = 0f;
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this.setColor((a << 24) | (b << 16) | (g << 8) | r);
        }

        @Override
        public VertexConsumer setColor(int color) {
            if (this.base >= 0) this.data[this.base + 3] = Float.intBitsToFloat(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            if (this.base >= 0) {
                this.data[this.base + 4] = u;
                this.data[this.base + 5] = v;
            }
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            if (this.base >= 0) this.data[this.base + 6] = Float.intBitsToFloat((v << 16) | (u & 0xFFFF));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            if (this.base >= 0) this.data[this.base + 7] = Float.intBitsToFloat((v << 16) | (u & 0xFFFF));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            if (this.base >= 0) {
                this.data[this.base + 8] = x;
                this.data[this.base + 9] = y;
                this.data[this.base + 10] = z;
            }
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }

        void replay(PoseStack.Pose pose, VertexConsumer vc) {
            float[] d = this.data;
            for (int i = 0; i < this.vertexCount; i++) {
                int b = i * STRIDE;
                int uv1 = Float.floatToRawIntBits(d[b + 6]);
                int uv2 = Float.floatToRawIntBits(d[b + 7]);
                vc.addVertex(d[b], d[b + 1], d[b + 2])
                        .setColor(Float.floatToRawIntBits(d[b + 3]))
                        .setUv(d[b + 4], d[b + 5])
                        .setUv1(uv1 & 0xFFFF, uv1 >>> 16)
                        .setUv2(uv2 & 0xFFFF, uv2 >>> 16)
                        .setNormal(d[b + 8], d[b + 9], d[b + 10]);
            }
        }
    }
}
