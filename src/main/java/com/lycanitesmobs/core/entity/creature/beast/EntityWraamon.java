package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class EntityWraamon extends TameableCreatureEntity implements Enemy {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityWraamon(EntityType<? extends EntityWraamon> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimPriorityGoalIndex(), new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Leap:
        if (!this.level().isClientSide() && this.hasAttackTarget() && this.onGround() && !this.level().isClientSide() && this.getRandom().nextInt(10) == 0)
            this.leap(6.0F, 0.6D, this.getTarget());
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        // Disable Knockback:
        double targetKnockbackResistance = 0;
        if (target instanceof LivingEntity) {
            targetKnockbackResistance = ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue();
            ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        }

        // Melee Attack:
        if (!super.attackMelee(target, damageScale))
            return false;

        // Restore Knockback:
        if (target instanceof LivingEntity)
            ((LivingEntity) target).getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(targetKnockbackResistance);

        return true;
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    // ========== Movement ==========
    @Override
    public boolean canClimb() {
        return false;
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
        if (source.is(DamageTypes.IN_WALL)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public float getFallResistance() {
        return 10;
    }
}
