package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class EntitySylph extends TameableCreatureEntity implements Enemy {

    private float fireDamageAbsorbed = 0;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntitySylph(EntityType<? extends EntitySylph> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = false;
        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setRange(16.0F).setMinChaseDistance(8.0F));

        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(EntityType.PLAYER));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        int projectileCount = 10;
        for (int i = 0; i < projectileCount; i++) {
            this.fireProjectile("aetherwave", target, range, (360 / projectileCount) * i, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        }
        super.attackRanged(target, range);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean isStrongSwimmer() {
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
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            this.fireDamageAbsorbed += amount;
            return false;
        }
        return super.hurtServer(lycLevel, source, amount);
    }

    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }

    @Override
    public boolean canBurn() {
        return false;
    }
}
