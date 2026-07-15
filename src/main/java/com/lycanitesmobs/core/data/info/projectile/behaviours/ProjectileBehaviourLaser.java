package com.lycanitesmobs.core.data.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;

public class ProjectileBehaviourLaser extends ProjectileBehaviour {
    /**
     * The aiming speed of the laser.
     **/
    protected double speed = 1;

    /**
     * The width of the laser.
     **/
    protected float width = 1;

    /**
     * The total range of the laser.
     **/
    protected double range = 16;

    @Override
    public void loadFromJSON(JsonObject json) {
        if (json.has("speed"))
            this.speed = json.get("speed").getAsDouble();
        if (json.has("width"))
            this.width = json.get("width").getAsFloat();
        if (json.has("range"))
            this.range = json.get("range").getAsDouble();
    }

    @Override
    public void onProjectileUpdate(BaseProjectileEntity projectile) {
        if (!(projectile instanceof CustomProjectileEntity)) {
            return;
        }
        CustomProjectileEntity customProjectile = (CustomProjectileEntity) projectile;
        projectile.stopMovement();
        customProjectile.syncThrower();
        customProjectile.setLaserWidth(this.width);

        // Follow Thrower/Parent Projectile:
        if (projectile.getOwner() != null) {
            Entity entityToFollow = projectile.getOwner();
            if (customProjectile.getParent() != null) {
                entityToFollow = customProjectile.getParent();
            }
            double xPos = entityToFollow.position().x();
            double yPos = entityToFollow.position().y();
            if (!(entityToFollow instanceof BaseProjectileEntity)) {
                yPos += entityToFollow.getDimensions(entityToFollow.getPose()).height() * 0.5D;
            }
            double zPos = entityToFollow.position().z();
			/*if(entityToFollow instanceof BaseCreatureEntity) {
				BaseCreatureEntity creatureToFollow = (BaseCreatureEntity)entityToFollow;
				xPos = creatureToFollow.getFacingPosition(creatureToFollow, 0, creatureToFollow.rotationYaw + 90F).getX();
				zPos = creatureToFollow.getFacingPosition(creatureToFollow, 0, creatureToFollow.rotationYaw).getZ();
			}*/
            projectile.setPos(xPos, yPos, zPos);
            projectile.setDeltaMovement(entityToFollow.getDeltaMovement());
        }

        // Update Laser End:
        this.updateEnd(projectile);
    }

    /**
     * Updates the end of the laser.
     *
     * @param projectile The projectile firing its laser!
     * @return The laser end used for stopping the laser at.
     */
    public void updateEnd(BaseProjectileEntity projectile) {
        CustomProjectileEntity customProjectile = (CustomProjectileEntity) projectile;
        // Laser Aiming:
        double targetX = projectile.position().x();
        double targetY = projectile.position().y();
        double targetZ = projectile.position().z();

        // Entity Laser Aiming:
        boolean lockedLaser = false;
        if (customProjectile.getParent() != null) {
            double[] target = projectile.getFacingPosition(projectile, this.range, customProjectile.getLaserAngle());
            targetX = target[0] + ((Mth.cos((projectile.getUpdateTick() + customProjectile.getLaserAngle()) * 0.25F) * 1.0F) - 0.5F);
            targetY = target[1] + (((Mth.cos((projectile.getUpdateTick() + customProjectile.getLaserAngle()) * 0.25F) * 1.0F) - 0.5F) * 10);
            targetZ = target[2] + ((Mth.cos((projectile.getUpdateTick() + customProjectile.getLaserAngle()) * 0.25F) * 1.0F) - 0.5F);
        } else if (projectile.getOwner() != null) {
            if (projectile.getOwner() instanceof BaseCreatureEntity && ((BaseCreatureEntity) projectile.getOwner()).getTarget() != null) {
                customProjectile.setTarget(((BaseCreatureEntity) projectile.getOwner()).getTarget());
            }
            Entity attackTarget = customProjectile.getTarget();
            if (attackTarget != null) {
                targetX = attackTarget.position().x();
                targetY = attackTarget.position().y() + (attackTarget.getDimensions(attackTarget.getPose()).height() / 2);
                targetZ = attackTarget.position().z();
                lockedLaser = true;
            } else {
                Vec3 lookDirection = projectile.getOwner().getLookAngle();
                targetX = projectile.getOwner().position().x() + (lookDirection.x * this.range);
                targetY = projectile.getOwner().position().y() + projectile.getOwner().getEyeHeight() + (lookDirection.y * this.range);
                targetZ = projectile.getOwner().position().z() + (lookDirection.z * this.range);
            }
        }

        // Raytracing:
        HashSet<Entity> excludedEntities = new HashSet<>();
        excludedEntities.add(projectile);
        if (projectile.getOwner() != null) {
            excludedEntities.add(projectile.getOwner());
            if (projectile.getOwner().getControllingPassenger() != null) {
                excludedEntities.add(projectile.getOwner().getControllingPassenger());
            }
        }
        HitResult rayTraceResult = LMHelperClass.raytrace(projectile.level(), projectile.position().x(), projectile.position().y(), projectile.position().z(), targetX, targetY, targetZ, this.width, projectile, excludedEntities);

        // Update Laser End Position:
        if (rayTraceResult != null && !lockedLaser) {
            targetX = rayTraceResult.getLocation().x;
            targetY = rayTraceResult.getLocation().y;
            targetZ = rayTraceResult.getLocation().z;
            if (rayTraceResult instanceof EntityHitResult) {
                Entity entityHit = ((EntityHitResult) rayTraceResult).getEntity();
                if (entityHit != null) {
                    targetY += entityHit.getDimensions(entityHit.getPose()).height() / 2;
                }
            }
        }

        customProjectile.setLaserEnd(new Vec3(targetX, targetY, targetZ));

        // Laser Damage:
        if (projectile.getUpdateTick() % 10 == 0 && projectile.isAlive() && rayTraceResult instanceof EntityHitResult) {
            EntityHitResult entityRayTraceResult = (EntityHitResult) rayTraceResult;
            if (customProjectile.getLaserEnd().distanceTo(entityRayTraceResult.getEntity().position()) <= (this.width * 10)) {
                boolean doDamage = true;
                if (entityRayTraceResult.getEntity() instanceof LivingEntity) {
                    doDamage = projectile.canDamage((LivingEntity) entityRayTraceResult.getEntity());
                }
                if (doDamage) {
                    this.updateDamage(projectile, entityRayTraceResult.getEntity());
                }
            }
        }

        if (projectile.getProjectileLife() % 2 == 0) {
            projectile.playSound(projectile.getBeamSound(), 1.0F, 1.0F / (projectile.level().getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }


    public boolean updateDamage(BaseProjectileEntity projectile, Entity target) {
        boolean attackSuccess;
        float damage = projectile.getDamage(target);
        float damageInit = damage;

        // Prevent Knockback:
        double targetKnockbackResistance = 0;
        if (projectile.getKnockbackChance() < 1) {
            if (projectile.getKnockbackChance() <= 0 || projectile.level().getRandom().nextDouble() <= projectile.getKnockbackChance()) {
                if (target instanceof LivingEntity) {
                    targetKnockbackResistance = ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
                    ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
                }
            }
        }

        // Deal Damage:
        if (projectile.getOwner() instanceof BaseCreatureEntity) {
            BaseCreatureEntity creatureThrower = (BaseCreatureEntity) projectile.getOwner();
            attackSuccess = creatureThrower.doRangedDamage(target, projectile, damage, projectile.isBlockedByEntity(target));
        } else {
            // 1.15.2 parity: pierce portion bypasses armor/resistance/protection.
            double pierceDamage = 1;
            if (damage <= pierceDamage)
                attackSuccess = target.hurtOrSimulate(ObjectManager.getDamageSource(projectile.level(), "pierce", projectile, projectile.getOwner()), damage);
            else {
                int hurtResistantTimeBefore = target.invulnerableTime;
                target.hurt(ObjectManager.getDamageSource(projectile.level(), "pierce", projectile, projectile.getOwner()), (float) pierceDamage);
                target.invulnerableTime = hurtResistantTimeBefore;
                damage -= pierceDamage;
                attackSuccess = target.hurtOrSimulate(projectile.level().damageSources().thrown(projectile, projectile.getOwner()), damage);
            }
        }

        if (target instanceof LivingEntity) {
            projectile.onDamage((LivingEntity) target, damageInit, attackSuccess);
        }

        // Restore Knockback:
        if (projectile.getKnockbackChance() < 1) {
            if (projectile.getKnockbackChance() <= 0 || projectile.level().getRandom().nextDouble() <= projectile.getKnockbackChance()) {
                if (target instanceof LivingEntity)
                    ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);
            }
        }

        return attackSuccess;
    }

    @Override
    public void onProjectileImpact(BaseProjectileEntity projectile, Level world, BlockPos pos) {
    }
}
