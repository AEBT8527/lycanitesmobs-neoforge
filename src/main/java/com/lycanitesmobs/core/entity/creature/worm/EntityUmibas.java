package com.lycanitesmobs.core.entity.creature.worm;

import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityUmibas extends TameableCreatureEntity implements IGroupHeavy {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityUmibas(EntityType<? extends EntityUmibas> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.ARTHROPOD;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.hasAttackSound = false;

        this.babySpawnChance = 0.25D;
        this.growthTime = -120000;
        this.setupMob();
        this.hitAreaWidthScale = 1.5F;

        // Stats:
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimPriorityGoalIndex(), new StealthGoal(this).setStealthTime(60).setStealthMove(true).setStealthAttack(true));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(1.0D).setRange(16.0F).setMinChaseDistance(8.0F));
    }

    @Override
    public boolean rollWanderChance() {
        return this.getRandom().nextDouble() <= 0.001D;
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
        if (this.isInWater())
            return 2.0F;
        return 1.0F;
    }

    // Pushed By Water:
    @Override
    public boolean isPushedByFluid() {
        return false;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        // Type:
        ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("magma");
        if (projectileInfo == null) {
            return;
        }
        BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), this);
        projectile.setProjectileScale(2f);

        // Y Offset:
        projectile.setPos(
                projectile.position().x(),
                projectile.position().y() - this.getDimensions(Pose.STANDING).height() / 4,
                projectile.position().z()
        );

        // Accuracy:
        float accuracy = 1.0F * (this.getRandom().nextFloat() - 0.5F);

        // Set Velocities:
        double d0 = target.position().x() - this.position().x() + accuracy;
        double d1 = target.position().y() + (double) target.getEyeHeight() - 1.100000023841858D - projectile.position().y() + accuracy;
        double d2 = target.position().z() - this.position().z() + accuracy;
        float f1 = Mth.sqrt(LMHelperClass.convertToFloat(d0 * d0 + d2 * d2)) * 0.2F;
        float velocity = 1.2F;
        projectile.shoot(d0, d1 + (double) f1, d2, velocity, 6.0F);

        // Launch:
        this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);

        // Random Projectiles:
        for (int i = 0; i < 10; i++) {
            projectile = projectileInfo.createProjectile(this.level(), this);
            projectile.setProjectileScale(2f);
            projectile.shoot((this.getRandom().nextFloat()) - 0.5F, this.getRandom().nextFloat(), (this.getRandom().nextFloat()) - 0.5F, 0.5F, 3.0F);
            this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);
        }

        super.attackRanged(target, range);
    }


    // ==================================================
    //                      Stealth
    // ==================================================
    @Override
    public boolean canStealth() {
        if (this.isTamed() && this.isSitting())
            return false;
        BlockState blockState = this.level().getBlockState(this.blockPosition().offset(0, -1, 0));
        return WormBurrowTerrain.isBurrowable(blockState);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    public boolean canBeTempted() {
        return this.isBaby();
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
    public boolean canBurn() {
        return false;
    }

    @Override
    public boolean waterDamage() {
        return true;
    }

    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }

    @Override
    public boolean canBreatheAir() {
        return true;
    }


    // ==================================================
    //                    Taking Damage
    // ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 0F;
        else return super.getDamageModifier(damageSrc);
    }
}
