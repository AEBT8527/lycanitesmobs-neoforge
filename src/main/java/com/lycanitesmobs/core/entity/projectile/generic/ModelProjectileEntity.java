package com.lycanitesmobs.core.entity.projectile.generic;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ModelProjectileEntity extends BaseProjectileEntity {
	public ModelProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
		super(entityType, world);
	}

	public ModelProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLiving) {
		super(entityType, world, entityLiving);
	}

	public ModelProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z) {
		super(entityType, world, x, y, z);
	}

	@Override
	public String getTextureName() {
	return this.entityName.toLowerCase();
	}

	@Override
	public ResourceLocation getTexture() {
		return AssetHelper.texture("textures/projectile/" + this.getTextureName() + ".png");
	}
}
