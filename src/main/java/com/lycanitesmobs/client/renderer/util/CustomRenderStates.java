package com.lycanitesmobs.client.renderer.util;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

import javax.annotation.Nullable;

/**
 * Render type lookup for the OBJ pipeline.
 * <p>
 * 26.x removed the whole RenderStateShard/ShaderInstance/CompositeState system this class was
 * built on (custom POS_TEX_NORMAL shader + hand-rolled blend shards). Models are now drawn
 * through vanilla entity render types (RenderPipeline-backed), mapping the mod's blend/glow
 * flags onto the closest vanilla pipelines — the same mapping the Iris compatibility path
 * already used:
 * Base models: NORMAL and NORMAL+glow -> entityCutout (no-cull, alpha-tested, depth-writing);
 * ADD -> eyes (additive); SUB -> entityTranslucent (closest available; true subtractive
 * blending would need a custom RenderPipeline, revisit if it looks wrong in game).
 * Layers go through {@link #getObjLayerRenderType} instead so they land in the translucent pass,
 * which runs after the base and therefore draws on top of it.
 */
public class CustomRenderStates {
    public static final Vector4f WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);

    public enum BLEND {
        NORMAL(0), ADD(1), SUB(2);
        public final int id;

        BLEND(int value) {
            this.id = value;
        }

        public int getValue() {
            return id;
        }
    }

    public static RenderType getObjRenderType(@Nullable Identifier texture, int blending, boolean glow) {
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        if (blending == BLEND.ADD.getValue()) {
            return RenderTypes.eyes(texture);
        }
        if (blending == BLEND.SUB.getValue()) {
            return RenderTypes.entityTranslucent(texture);
        }
        // A glowing BASE deliberately does NOT use entityTranslucentEmissive. That type is drawn in
        // the translucent pass with depth writes off, so the base and its own detail layers ended up
        // in passes whose relative order is not submission order - the base could be painted over
        // its layers, which made every glow model (nymph, cinder, sylph, volcan, wisp, cherufe,
        // khalk, salamander) render as a flat untextured silhouette. The models already report
        // FULL_BRIGHT for glowing parts, and entityCutout samples the lightmap, so the emissive look
        // survives while the base stays opaque and depth-writing, exactly as vanilla does it.
        return RenderTypes.entityCutout(texture);
    }

    /** Old VBO path took no texture (relied on bound-texture state); now texture-explicit. */
    public static RenderType getObjVBORenderType(@Nullable Identifier texture, int blending, boolean glow) {
        return getObjRenderType(texture, blending, glow);
    }

    /**
     * Render type for a model LAYER, which has to end up in the same pass as the base it sits on.
     *
     * A glowing base maps to entity_translucent_emissive, which vanilla draws in the translucent
     * pass with depth writes disabled, while a plain layer maps to entity_cutout - an earlier pass
     * that writes depth. The base was therefore drawn after, and straight over, its own detail
     * layer: every glow model (nymph, cinder, sylph, volcan, wisp, cherufe, khalk, salamander)
     * rendered as a flat untextured silhouette with its head/hair/wing detail hidden underneath.
     *
     * Keeping such a layer translucent puts it back in the same pass as the base, where submission
     * order decides - which is exactly what the single no-cull translucent family used on 1.21.1 did.
     */
    public static RenderType getObjLayerRenderType(@Nullable Identifier texture, int blending, boolean glow) {
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        if (blending == BLEND.ADD.getValue()) {
            return RenderTypes.eyes(texture);
        }
        // Layers always sit on top of the base, so they belong in the translucent pass: that runs
        // after the opaque/cutout pass the base is drawn in, tests depth with LEQUAL and does not
        // write depth. Ordering is then decided by the passes themselves rather than by whichever
        // draw call happened to be grouped first, which is what made the old mapping flaky.
        return glow ? RenderTypes.entityTranslucentEmissive(texture) : RenderTypes.entityTranslucent(texture);
    }

    /** Translucent, colour-tintable pass used by ghost/fear style effects. */
    public static RenderType getFearRenderType(@Nullable Identifier texture) {
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType getObjColorOnlyRenderType(@Nullable Identifier texture, int blending, boolean glow) {
        return getObjRenderType(texture, blending, glow);
    }

    public static RenderType getObjOutlineRenderType(@Nullable Identifier texture) {
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        return RenderTypes.outline(texture);
    }

    public static RenderType getSpriteRenderType(@Nullable Identifier texture) {
        return getSpriteRenderType(texture, false);
    }

    public static RenderType getSpriteRenderType(@Nullable Identifier texture, boolean emissive) {
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        return emissive ? RenderTypes.entityTranslucentEmissive(texture) : RenderTypes.entityTranslucent(texture);
    }
}
