package com.lycanitesmobs.client.renderer.entity.creature;

import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 26.x render-state bridge for Lycanites creatures.
 * <p>
 * The whole legacy pipeline (models, layers, animations) reads live entity fields during
 * rendering, so migrating it wholesale to extracted state would mean rewriting 150+ model
 * classes. Instead the state carries the entity reference; extraction happens on the render
 * thread right before submit, so reads remain safe in practice (matching what the mod did
 * on 1.20/1.21 anyway).
 */
@OnlyIn(Dist.CLIENT)
public class CreatureRenderState extends LivingEntityRenderState {
    public BaseCreatureEntity entity;
    public float lycPartialTicks;
    public float lycYaw;
    public int lycPackedLight;
}
