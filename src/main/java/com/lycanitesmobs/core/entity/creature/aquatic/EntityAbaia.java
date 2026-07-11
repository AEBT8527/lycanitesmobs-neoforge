package com.lycanitesmobs.core.entity.creature.aquatic;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;


import java.util.List;

public class EntityAbaia extends TameableCreatureEntity implements Enemy {

    protected short aoeAttackTick = 0;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityAbaia(EntityType<? extends EntityAbaia> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsOnLand = false;
        this.spawnsInWater = true;
        this.hasAttackSound = true;

        this.babySpawnChance = 0.05D;
        this.canGrow = true;
        this.setupMob();
    }


    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Static Aura Attack:
        if (!this.level().isClientSide() && this.hasAttackTarget() && ++this.aoeAttackTick == (this.isPetType("familiar") ? 100 : 40)) {
            this.aoeAttackTick = 0;
            List aoeTargets = this.getNearbyEntities(LivingEntity.class, e -> true, 4);
            for (Object entityObj : aoeTargets) {
                LivingEntity target = (LivingEntity) entityObj;
                if (target != this && this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().hasLineOfSight(target) && (target.isInWater() || this.isRareVariant())) {
                    //target.hurt(ElementDamageSource.causeElementDamage(this, ElementManager.getInstance().getElement("lightning")), this.getAttackDamage(1));
                }
            }
        }

        // Particles:
        if (this.level().isClientSide() && this.hasAttackTarget()) {
            this.level().addParticle(ParticleTypes.CRIT, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);

            List<LivingEntity> aoeTargets = this.getNearbyEntities(LivingEntity.class, (e) -> true, 4);
            for (Object entityObj : aoeTargets) {
                LivingEntity target = (LivingEntity) entityObj;
                if (this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().hasLineOfSight(target)) {
                    this.level().addParticle(ParticleTypes.CRIT, target.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) target.getDimensions(Pose.STANDING).width(), target.position().y() + this.getRandom().nextDouble() * (double) target.getDimensions(Pose.STANDING).height(), target.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) target.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // Pathing Weight:
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        int waterWeight = 10;

        Block block = this.level().getBlockState(new BlockPos(x, y, z)).getBlock();
        if (block == Blocks.WATER)
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);
        if (this.level().isRaining() && this.level().canSeeSkyFromBelowWater(new BlockPos(x, y, z)))
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);

        if (this.getTarget() != null)
            return super.getBlockPathWeight(x, y, z);
        if (this.waterContact())
            return -999999.0F;

        return super.getBlockPathWeight(x, y, z);
    }

    // Swimming:
    @Override
    public boolean isStrongSwimmer() {
        return true;
    }

    // Walking:
    @Override
    public boolean canWalk() {
        return false;
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
    // ========== Damage ==========

    /**
     * Returns whether or not the given damage type is applicable, if not no damage will be taken.
     **/
    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        if (source.is(DamageTypes.LIGHTNING_BOLT)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }

    @Override
    public boolean canBreatheAir() {
        return false;
    }
}
