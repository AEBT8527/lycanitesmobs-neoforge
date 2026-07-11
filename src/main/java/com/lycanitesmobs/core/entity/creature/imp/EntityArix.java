package com.lycanitesmobs.core.entity.creature.imp;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.creature.elemental.EntityXaphan;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3d;

public class EntityArix extends TameableCreatureEntity implements Enemy {

    protected boolean wantsToLand;
    protected boolean isLanded;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityArix(EntityType<? extends EntityArix> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.hasAttackSound = false;
        this.flySoundSpeed = 20;
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(5.0F).setCheckSight(false));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Land/Fly:
        if (!this.level().isClientSide()) {
            if (this.isLanded) {
                this.wantsToLand = false;
                if (!this.isSitting() && this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                    this.leap(1.0D, 1.0D);
                    this.isLanded = false;
                }
            } else {
                if (this.wantsToLand) {
                    if (this.isSafeToLand()) {
                        this.isLanded = true;
                    }
                } else {
                    if (this.updateTick % (5 * 20) == 0 && this.getRandom().nextBoolean()) {
                        this.wantsToLand = true;
                    }
                }
            }
        }

        // Particles:
        if (this.level().isClientSide() && !this.hasPerchTarget())
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Get Wander Position ==========
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

    // ========== Get Flight Offset ==========
    public double getFlightOffset() {
        if (!this.wantsToLand) {
            super.getFlightOffset();
        }
        return 0;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttackType(EntityType targetType) {
        return super.canAttackType(targetType);
    }

    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("icefireball", target, range, 0, new Vector3d(0, 0, 0), 0.8f, 2f, 6F);
        super.attackRanged(target, range);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        return !this.isLanded;
    }

    @Override
    public boolean isStrongSwimmer() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }


    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.getBagSize();
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        if (source.type().equals(ObjectManager.getDamageSource(this.level(), "ooze").type())) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public float getFallResistance() {
        return 100;
    }
}
