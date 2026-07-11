package com.lycanitesmobs.client.obj.geometry;

import com.lycanitesmobs.core.util.math.Vector3o;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

public class Mesh {

    public int[] indices;
    public Vertex[] vertices;
    public Vector3o[] normals;

    public static List<String> logs = new ArrayList<>();

    /**
     * 26.x: the custom-format VBO path (VertexBuffer + POS_TEX_NORMAL shader) is gone.
     * Emit the mesh triangles straight into a vanilla entity VertexConsumer instead.
     * UV offset supports scrolling texture animations; colour is the layer tint.
     */
    public void emit(VertexConsumer vc, PoseStack.Pose pose, int light, int overlay,
                     float red, float green, float blue, float alpha, float uOffset, float vOffset) {
        this.computeVertexNormalsIfNeeded();
        if (this.indices == null || this.vertices == null) {
            return;
        }
        // QUADS-mode render types: emit each triangle as a degenerate quad (3rd vertex doubled).
        final int[] lycTriToQuad = {0, 1, 2, 2};
        for (int tri = 0; tri + 2 < this.indices.length; tri += 3) {
            for (int iv : lycTriToQuad) {
            int idx = this.indices[tri + iv];
            Vertex vertex = this.vertices[idx];
            Vector3o normal = this.normals != null && idx < this.normals.length ? this.normals[idx] : null;
            float nx = normal != null ? normal.x() : 0f;
            float ny = normal != null ? normal.y() : 1f;
            float nz = normal != null ? normal.z() : 0f;
            vc.addVertex(pose.pose(), vertex.getPos().x(), vertex.getPos().y(), vertex.getPos().z())
                    .setColor(red, green, blue, alpha)
                    .setUv(vertex.getTexCoords().x + uOffset, (1.0F - vertex.getTexCoords().y) + vOffset)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(pose, nx, ny, nz);
            }
        }
    }

    public void delete() {
    }

    private static Vector3o calcNormal(Vector3o v1, Vector3o v2, Vector3o v3) {
        Vector3o u = new Vector3o(v2).sub(v1);
        Vector3o v = new Vector3o(v3).sub(v1);
        Vector3o out = new Vector3o(
                u.y() * v.z() - u.z() * v.y(),
                u.z() * v.x() - u.x() * v.z(),
                u.x() * v.y() - u.y() * v.x()
        );
        out.normalize();
        return out;
    }

    public void computeVertexNormalsIfNeeded() {
        if (this.normals != null && this.vertices != null && this.normals.length == this.vertices.length) {
            return;
        }
        if (this.vertices == null || this.indices == null) {
            this.normals = null;
            return;
        }

        Vector3o[] vertexNormals = new Vector3o[this.vertices.length];
        for (int i = 0; i < vertexNormals.length; i++) {
            vertexNormals[i] = new Vector3o(0, 0, 0);
        }

        for (int i = 0; i < this.indices.length; i += 3) {
            int i0 = this.indices[i];
            int i1 = this.indices[i + 1];
            int i2 = this.indices[i + 2];

            Vector3o v1 = this.vertices[i0].getPos();
            Vector3o v2 = this.vertices[i1].getPos();
            Vector3o v3 = this.vertices[i2].getPos();

            Vector3o faceNormal = calcNormal(v1, v2, v3);

            Vector3o n0 = vertexNormals[i0];
            Vector3o n1 = vertexNormals[i1];
            Vector3o n2 = vertexNormals[i2];

            n0.set(n0.x() + faceNormal.x(), n0.y() + faceNormal.y(), n0.z() + faceNormal.z());
            n1.set(n1.x() + faceNormal.x(), n1.y() + faceNormal.y(), n1.z() + faceNormal.z());
            n2.set(n2.x() + faceNormal.x(), n2.y() + faceNormal.y(), n2.z() + faceNormal.z());
        }

        for (int i = 0; i < vertexNormals.length; i++) {
            vertexNormals[i].normalize();
        }

        this.normals = vertexNormals;
    }
}
