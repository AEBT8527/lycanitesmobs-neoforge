package com.lycanitesmobs.client.renderer.misc;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Renders nothing; used for dummy/effect-only entities. Ported to 26.x state rendering. */
@OnlyIn(Dist.CLIENT)
public class NoneRenderer<T extends Entity> extends EntityRenderer<T, EntityRenderState> {

    public NoneRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
