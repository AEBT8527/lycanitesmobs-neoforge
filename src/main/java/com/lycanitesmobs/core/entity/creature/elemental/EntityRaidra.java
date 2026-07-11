package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityRaidra extends TameableCreatureEntity implements Enemy {

    protected short aoeAttackTick = 0;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityRaidra(EntityType<? extends EntityRaidra> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));
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
            List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 4);
            for (Object entityObj : aoeTargets) {
                LivingEntity target = (LivingEntity) entityObj;
                if (target != this && this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().hasLineOfSight(target)) {
                    //target.hurt(ElementDamageSource.causeElementDamage(this, ElementManager.getInstance().getElement("lightning")), this.getAttackDamage(1));
                }
            }
        }

        // Particles:
        if (this.level().isClientSide() && this.hasAttackTarget()) {
            //this.getEntityWorld().addParticle(ParticleTypes.CLOUD, this.getPositionVec().getX() + (this.rand.nextDouble() - 0.5D) * (double) this.getSize(Pose.STANDING).width(), this.getPositionVec().getY() + this.rand.nextDouble() * (double) this.getSize(Pose.STANDING).height(), this.getPositionVec().getZ() + (this.rand.nextDouble() - 0.5D) * (double) this.getSize(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);

            List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 4);
            for (Object entityObj : aoeTargets) {
                LivingEntity target = (LivingEntity) entityObj;
                if (this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().hasLineOfSight(target)) {
                    this.level().addParticle(ParticleTypes.CRIT, target.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) target.getDimensions(Pose.STANDING).width(), target.position().y() + this.getRandom().nextDouble() * (double) target.getDimensions(Pose.STANDING).height(), target.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) target.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
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
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }


    // ==================================================
    //                     Immunities
    // ==================================================

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
}
