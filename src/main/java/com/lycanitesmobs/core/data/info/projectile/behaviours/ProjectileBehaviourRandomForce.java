package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;

public class ProjectileBehaviourRandomForce extends ProjectileBehaviour {
	/** How much force to apply. **/
	protected double force = 0.5D;

	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("force"))
			this.force = json.get("force").getAsDouble();
	}

	@Override
	public void onProjectileUpdate(BaseProjectileEntity projectile) {
		if(projectile.level().isClientSide()) {
			return;
		}

		if(projectile.getUpdateTick() % 5 == 0) {
			projectile.push(
					(0.5D - projectile.level().getRandom().nextDouble()) * this.force,
					(0.5D - projectile.level().getRandom().nextDouble()) * this.force,
					(0.5D - projectile.level().getRandom().nextDouble()) * this.force
			);
		}
	}
}
