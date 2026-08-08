package com.lycanitesmobs.client.renderer.entity.projectile;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** 26.x state bridge for projectile renderers (same live-entity approach as CreatureRenderState). */
public class ProjectileRenderState extends EntityRenderState {
    public BaseProjectileEntity entity;
    public float lycPartialTicks;
}
