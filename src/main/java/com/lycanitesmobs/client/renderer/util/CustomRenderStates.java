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
 * NORMAL -> entityCutout (no-cull, alpha-tested); NORMAL+glow -> entityTranslucentEmissive;
 * ADD -> eyes (additive); SUB -> entityTranslucent (closest available; true subtractive
 * blending would need a custom RenderPipeline, revisit if it looks wrong in game).
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
        if (glow) {
            return RenderTypes.entityTranslucentEmissive(texture);
        }
        return RenderTypes.entityCutout(texture);
    }

    /** Old VBO path took no texture (relied on bound-texture state); now texture-explicit. */
    public static RenderType getObjVBORenderType(@Nullable Identifier texture, int blending, boolean glow) {
        return getObjRenderType(texture, blending, glow);
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
        if (texture == null) {
            texture = MissingTextureAtlasSprite.getLocation();
        }
        return RenderTypes.entityTranslucent(texture);
    }
}
