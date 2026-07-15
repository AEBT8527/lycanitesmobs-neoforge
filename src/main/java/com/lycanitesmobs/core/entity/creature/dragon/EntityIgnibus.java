package com.lycanitesmobs.core.entity.creature.dragon;

import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.projectile.generic.RapidFireProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3d;

import net.minecraft.util.Mth;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import java.util.ArrayList;
import java.util.List;

public class EntityIgnibus extends RideableCreatureEntity implements IGroupHeavy {
    protected boolean wantsToLand;
    protected boolean isLanded;

    public EntityIgnibus(EntityType<? extends EntityIgnibus> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.isLavaCreature = true;
        this.flySoundSpeed = 20;
        this.hasAttackSound = false;

        this.setAttackCooldownMax(20);
        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
        this.hitAreaWidthScale = 1.5F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setStaminaTime(100).setRange(20.0F).setMinChaseDistance(10.0F));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Land/Fly:
        if (!this.level().isClientSide()) {
            if (this.isLanded) {
                this.wantsToLand = false;
                if (this.hasPickupEntity() || this.getControllingPassenger() != null || this.isLeashed() || this.isInWater() || (!this.isTamed() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean())) {
                    this.leap(1.0D, 1.0D);
                    this.isLanded = false;
                }
                if (this.isTamed() && !this.isSitting()) {
                    this.isLanded = false;
                }
            } else {
                if (this.wantsToLand) {
                    if (this.isSafeToLand()) {
                        this.isLanded = true;
                    }
                } else {
                    if (!this.hasPickupEntity() && !this.hasAttackTarget() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                        this.wantsToLand = true;
                    }
                }
            }
            if (this.hasPickupEntity() || this.getControllingPassenger() != null || this.hasAttackTarget() || this.isInWater()) {
                this.wantsToLand = false;
            } else if (this.isTamed() && this.isSitting() && !this.isLeashed()) {
                this.wantsToLand = true;
            }
        }

        // Particles:
        if (this.level().isClientSide())
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.SMOKE, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.FLAME, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        rider.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (5 * 20) + 5, 1));
        super.riderEffects(rider);
    }

    @Override
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        if (this.wantsToLand || !this.isLanded) {
            BlockPos groundPos;
            for (groundPos = wanderPosition.below(); groundPos.getY() > 0 && this.level().getBlockState(groundPos).getBlock() == Blocks.AIR; groundPos = groundPos.below()) {
            }
            if (this.level().getBlockState(groundPos).isSolid()) {
                return groundPos.above();
            }
        }
        return super.getWanderPosition(wanderPosition);
    }


    @Override
    public double getFlightOffset() {
        if (!this.wantsToLand) {
            super.getFlightOffset();
        }
        return 0;
    }

    @Override
    public boolean isFlying() {
        return !this.isLanded;
    }

    @Override
    public boolean isStrongSwimmer() {
        return false;
    }

    @Override
        public void attackRanged(Entity target, float range) {
        // 1.15.2 parity: seven rapid-fire scorchfireballs (was a 3x3 primeember grid).
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("scorchfireball");
        if (projectileInfo == null) {
            return;
        }
        List<RapidFireProjectileEntity> projectiles = new ArrayList<>();

        RapidFireProjectileEntity projectileEntry = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectiles.add(projectileEntry);

        RapidFireProjectileEntity projectileEntry2 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectileEntry2.addOffset(1.0D, 0, 0);
        projectileEntry2.setProjectileScale(0.25f);
        projectiles.add(projectileEntry2);

        RapidFireProjectileEntity projectileEntry3 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectileEntry3.addOffset(-1.0D, 0, 0);
        projectileEntry3.setProjectileScale(0.25f);
        projectiles.add(projectileEntry3);

        RapidFireProjectileEntity projectileEntry4 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectileEntry4.addOffset(0, 0, 1.0D);
        projectileEntry4.setProjectileScale(0.25f);
        projectiles.add(projectileEntry4);

        RapidFireProjectileEntity projectileEntry5 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectileEntry5.addOffset(0, 0, -1.0D);
        projectileEntry5.setProjectileScale(0.25f);
        projectiles.add(projectileEntry5);

        RapidFireProjectileEntity projectileEntry6 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectileEntry6.addOffset(0, 1.0D, 0);
        projectileEntry6.setProjectileScale(0.25f);
        projectiles.add(projectileEntry6);

        RapidFireProjectileEntity projectileEntry7 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
        projectileEntry7.addOffset(0, -1.0D, 0);
        projectileEntry7.setProjectileScale(0.25f);
        projectiles.add(projectileEntry7);

        for (RapidFireProjectileEntity projectile : projectiles) {
            projectile.setProjectileScale(1f);

            // Y Offset:
            projectile.setPos(
                    projectile.position().x(),
                    projectile.position().y() - this.getDimensions(Pose.STANDING).height() / 4,
                    projectile.position().z()
            );

            // Accuracy:
            float accuracy = 4.0F * (this.getRandom().nextFloat() - 0.5F);

            // Set Velocities:
            double d0 = target.position().x() - this.position().x() + accuracy;
            double d1 = target.position().y() + (double) target.getEyeHeight() - 1.1F - projectile.position().y() + accuracy;
            double d2 = target.position().z() - this.position().z() + accuracy;
            float f1 = Mth.sqrt(LMHelperClass.convertToFloat(d0 * d0 + d2 * d2)) * 0.2F;
            float velocity = 1.2F;
            projectile.shoot(d0, d1 + f1, d2, velocity, 6.0F);
            projectile.setProjectileScale(4F);

            // Launch:
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);
        }

        super.attackRanged(target, range);
    }

    @Override
    public boolean canBurn() {
        return false;
    }

    @Override
    public boolean canBreatheUnderlava() {
        return true;
    }

    @Override
    public float getFallResistance() {
        return 100;
    }

    @Override
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 0F;
        else return super.getDamageModifier(damageSrc);
    }

    @Override
    public void mountAbility(Entity rider) {
        if (this.level().isClientSide())
            return;

        if (this.abilityToggled)
            return;

        if (this.hasPickupEntity()) {
            this.dropPickupEntity();
            return;
        }

        if (this.getStamina() < this.getStaminaCost())
            return;

        if (rider instanceof Player) {
            Player player = (Player) rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("scorchfireball"); // 1.15.2 parity
            if (projectileInfo == null) {
                return;
            }

            // Type:
            List<RapidFireProjectileEntity> projectiles = new ArrayList<>();

            RapidFireProjectileEntity projectileEntry = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), player, 15, 3);
            projectiles.add(projectileEntry);

            RapidFireProjectileEntity projectileEntry2 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
            projectileEntry2.addOffset(1.0D, 0, 0);
            projectileEntry2.setProjectileScale(0.25f);
            projectiles.add(projectileEntry2);

            RapidFireProjectileEntity projectileEntry3 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
            projectileEntry3.addOffset(-1.0D, 0, 0);
            projectileEntry3.setProjectileScale(0.25f);
            projectiles.add(projectileEntry3);

            RapidFireProjectileEntity projectileEntry4 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
            projectileEntry4.addOffset(0, 0, 1.0D);
            projectileEntry4.setProjectileScale(0.25f);
            projectiles.add(projectileEntry4);

            RapidFireProjectileEntity projectileEntry5 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
            projectileEntry5.addOffset(0, 0, -1.0D);
            projectileEntry5.setProjectileScale(0.25f);
            projectiles.add(projectileEntry5);

            RapidFireProjectileEntity projectileEntry6 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
            projectileEntry6.addOffset(0, 1.0D, 0);
            projectileEntry6.setProjectileScale(0.25f);
            projectiles.add(projectileEntry6);

            RapidFireProjectileEntity projectileEntry7 = new RapidFireProjectileEntity(ProjectileManager.getInstance().getOldProjectileType(RapidFireProjectileEntity.class), projectileInfo, this.level(), this, 15, 3);
            projectileEntry7.addOffset(0, -10D, 0);
            projectileEntry7.setProjectileScale(0.25f);
            projectiles.add(projectileEntry7);

            for (RapidFireProjectileEntity projectile : projectiles) {
                projectile.setProjectileScale(1f);

                // Y Offset:
                projectile.setPos(
                        projectile.position().x(),
                        projectile.position().y() - this.getDimensions(Pose.STANDING).height() / 4,
                        projectile.position().z()
                );

                // Launch:
                this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);
            }
            this.triggerAttackCooldown();
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 2;
    }

    public int getStaminaRecoveryWarmup() {
        return 2 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }

    @Override
    public float getBrightness() {
        if (isAttackOnCooldown())
            return 1.0F;
        else
            return super.getBrightness();
    }
}
