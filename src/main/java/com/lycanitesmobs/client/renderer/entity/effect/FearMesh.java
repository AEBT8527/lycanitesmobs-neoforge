package com.lycanitesmobs.client.renderer.entity.effect;

/**
 * TODO(26.x): the fear ghost-mesh effect was built directly on VertexBuffer/Tesselator with a
 * custom vertex format — all removed with the old render system. Stubbed until the effect is
 * rebuilt on the submit pipeline (FearRenderer's draw body is already disabled).
 */
public class FearMesh {
    public FearMesh(FearMeshProfile profile) {
    }

    public void tick(float yaw, float pitch, float horizontalDistance, float partialTick) {
    }

    public Object getVbo() {
        return null;
    }

    public int getVertexCount() {
        return 0;
    }

    public Object getVertexFormat() {
        return null;
    }

    public float getSmoothedYaw() {
        return 0f;
    }

    public float getSmoothedPitch() {
        return 0f;
    }

    public void dispose() {
    }
}
