package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ProjectileBehaviourFireProjectiles extends ProjectileBehaviour {
    /**
     * The name of the projectile to fire.
     **/
    protected String projectileName;

    /**
     * How many ticks per projectile fired on update, if less than 0 no projectiles are fired on update. 20 ticks = 1 second.
     **/
    protected int tickRate = 10;

    /**
     * If above 0, all spawned projectiles are remembered and additional ones are only spawned if the total spawned amount is too low.
     **/
    protected int persistentCount = 0;

    /**
     * How many projectiles fired on impact.
     **/
    protected int impactCount = 5;

    /**
     * The velocity of the fired projectile.
     **/
    protected float velocity = 1.2F;

    @Override
    public void loadFromJSON(JsonObject json) {
        this.projectileName = json.get("projectileName").getAsString();

        if (json.has("tickRate"))
            this.tickRate = json.get("tickRate").getAsInt();

        if (json.has("impactCount"))
            this.impactCount = json.get("impactCount").getAsInt();

        if (json.has("persistentCount"))
            this.persistentCount = json.get("persistentCount").getAsInt();

        if (json.has("velocity"))
            this.velocity = json.get("velocity").getAsFloat();
    }

    @Override
    public void onProjectileUpdate(BaseProjectileEntity projectile) {
        if (this.tickRate < 0 || projectile.getUpdateTick() % this.tickRate != 0 || projectile.getCommandSenderWorld().isClientSide) {
            return;
        }

        if (this.persistentCount <= 0 || ((CustomProjectileEntity) projectile).getSpawnedProjectileCount() < this.persistentCount) {
            this.createProjectile(projectile);
        }
    }

    @Override
    public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
        if (projectile.getCommandSenderWorld().isClientSide) {
            return;
        }

        for (int i = 0; i < this.impactCount; i++) {
            this.createProjectile(projectile);
        }

        CustomProjectileEntity customProjectile = (CustomProjectileEntity) projectile;
        for (int i = 0; i < customProjectile.getSpawnedProjectileCount(); i++) {
            customProjectile.getSpawnedProjectile(i).remove(Entity.RemovalReason.DISCARDED);
        }
    }


    protected LivingEntity getShooter() {
        return null;
    }

    /**
     * Fires a new projectile from the given projectile.
     *
     * @param projectile The projectile to fire a new projectile from.
     * @return The new projectile that was fired.
     */
    public BaseProjectileEntity createProjectile(BaseProjectileEntity projectile) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(this.projectileName);
        if (projectileInfo == null) {
            return null;
        }
        BaseProjectileEntity childProjectile;

        if (projectile.getOwner() != null) {
            LivingEntity shooter = projectile.getShooter() instanceof LivingEntity ? (LivingEntity) projectile.getShooter() : null;
            childProjectile = projectileInfo.createProjectile(projectile.getCommandSenderWorld(), shooter);
            childProjectile.setPos(
                    projectile.position().x(),
                    projectile.position().y(),
                    projectile.position().z()
            );
        } else {
            childProjectile = projectileInfo.createProjectile(
                    projectile.getCommandSenderWorld(),
                    projectile.position().x(),
                    projectile.position().y(),
                    projectile.position().z()
            );
        }

        if (childProjectile instanceof CustomProjectileEntity) {
            ((CustomProjectileEntity) childProjectile).setParent(projectile);
        }
        if (this.persistentCount > 0 && childProjectile instanceof CustomProjectileEntity) {
            ((CustomProjectileEntity) childProjectile).setLaserAngle((360F / this.persistentCount) * ((CustomProjectileEntity) projectile).getSpawnedProjectileCount());
            ((CustomProjectileEntity) projectile).addSpawnedProjectile(childProjectile);
        }

        double motionT = projectile.getDeltaMovement().x() + projectile.getDeltaMovement().y() + projectile.getDeltaMovement().z();
        if (projectile.getDeltaMovement().x() < 0)
            motionT -= projectile.getDeltaMovement().x() * 2;
        if (projectile.getDeltaMovement().y() < 0)
            motionT -= projectile.getDeltaMovement().y() * 2;
        if (projectile.getDeltaMovement().z() < 0)
            motionT -= projectile.getDeltaMovement().z() * 2;
        double motionX = 0;
        double motionY = 0;
        double motionZ = 0;
        if (motionT > 0.0001D) {
            motionX = projectile.getDeltaMovement().x() / motionT;
            motionY = projectile.getDeltaMovement().y() / motionT;
            motionZ = projectile.getDeltaMovement().z() / motionT;
        }
        childProjectile.shoot(
                motionX + (projectile.getCommandSenderWorld().getRandom().nextGaussian() - 0.5D),
                motionY + (projectile.getCommandSenderWorld().getRandom().nextGaussian() - 0.5D),
                motionZ + (projectile.getCommandSenderWorld().getRandom().nextGaussian() - 0.5D),
                this.velocity,
                0
        );

        projectile.playSound(childProjectile.getLaunchSound(), 1.0F, 1.0F / (projectile.getCommandSenderWorld().getRandom().nextFloat() * 0.4F + 0.8F));
        DeferredLevelActionManager.spawnEntity(projectile.getCommandSenderWorld(), projectile.blockPosition(), null, childProjectile);

        return childProjectile;
    }
}
