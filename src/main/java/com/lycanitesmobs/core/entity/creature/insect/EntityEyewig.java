package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.level.Level;

public class EntityEyewig extends RideableCreatureEntity {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityEyewig(EntityType<? extends EntityEyewig> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.setupMob();
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false).setMaxChaseDistanceSq(4.0F));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setStaminaTime(100).setRange(8.0F).setMinChaseDistance(4.0F).setMountedAttacking(false));
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // Pushed By Water:
    @Override
    public boolean isPushedByFluid() {
        return false;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    BaseProjectileEntity projectile = null;

    @Override
    public void attackRanged(Entity target, float range) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("poisonray");
        if (projectileInfo == null) {
            return;
        }

        // Update Laser:
        if (this.projectile != null && this.projectile.isAlive()) {
            this.projectile.setProjectileLife(20);
        } else {
            this.projectile = null;
        }

        // Create New Laser:
        if (this.projectile == null) {
            // Type:
            this.projectile = projectileInfo.createProjectile(this.level(), this);

            // Launch:
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);
        }

        super.attackRanged(target, range);
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    BaseProjectileEntity abilityProjectile = null;

    public void mountAbility(Entity rider) {
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("poisonray");
        if (projectileInfo == null) {
            return;
        }

        if (this.level().isClientSide())
            return;

        if (this.getStamina() < this.getStaminaRecoveryMax() * 2)
            return;

        if (this.hasAttackTarget())
            this.setTarget(null);

        // Update Laser:
        if (this.abilityProjectile != null && this.abilityProjectile.isAlive()) {
            this.abilityProjectile.setProjectileLife(20);
        } else {
            this.abilityProjectile = null;
        }

        // Create New Laser:
        if (this.abilityProjectile == null) {
            // Type:
            if (this.getControllingPassenger() == null || !(this.getControllingPassenger() instanceof LivingEntity))
                return;

            this.abilityProjectile = projectileInfo.createProjectile(this.level(), this);

            // Launch:
            this.playSound(this.abilityProjectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, this.abilityProjectile);
        }

        this.applyStaminaCost();
    }


    // ==================================================
    //                      Targets
    // ==================================================
    @Override
    public boolean isAggressive() {
        if (this.isTamed()) {
            return super.isAggressive();
        }
        if (this.level() != null && this.level().isBrightOutside())
            return this.testLightLevel() < 2;
        else
            return super.isAggressive();
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean canClimb() {
        return true;
    }

    @Override
    public boolean isStrongSwimmer() {
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
    public float getFallResistance() {
        return 10;
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
}
