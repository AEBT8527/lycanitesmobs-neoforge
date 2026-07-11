package com.lycanitesmobs.client.renderer.entity.projectile;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** 26.x state bridge for projectile renderers (same live-entity approach as CreatureRenderState). */
@OnlyIn(Dist.CLIENT)
public class ProjectileRenderState extends EntityRenderState {
    public BaseProjectileEntity entity;
    public float lycPartialTicks;
}
