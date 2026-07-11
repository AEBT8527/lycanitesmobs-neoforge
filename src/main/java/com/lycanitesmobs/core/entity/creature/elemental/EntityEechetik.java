package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class EntityEechetik extends TameableCreatureEntity implements Enemy {

    protected int myceliumRadius = 2;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityEechetik(EntityType<? extends EntityEechetik> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.ARTHROPOD;
        this.hasAttackSound = true;

        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(true));
    }

    @Override
    public void loadCreatureFlags() {
        this.myceliumRadius = this.creatureInfo.getFlag("myceliumRadius", this.myceliumRadius);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Plague Aura Attack:
        if (!this.level().isClientSide() && this.updateTick % 40 == 0 && this.hasAttackTarget()) {
            EffectBase plague = ObjectManager.getEffect("plague");
            if (plague != null) {
                MobEffectInstance potionEffect = new MobEffectInstance(ObjectManager.holder(plague), this.getEffectDuration(2), 1);
                List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 2);
                for (Object entityObj : aoeTargets) {
                    LivingEntity target = (LivingEntity) entityObj;
                    if (target != this && this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().hasLineOfSight(target) && target.canBeAffected(potionEffect)) {
                        target.addEffect(potionEffect);
                    }
                }
            }
        }

        // Grow Mycelium:
        if (!this.level().isClientSide() && this.updateTick % 100 == 0 && this.myceliumRadius > 0 && !this.isTamed() && LMHelperClass.getGameRuleBool(this.level(), GameRules.MOB_GRIEFING, true)) {
            int range = this.myceliumRadius;
            for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); w++) {
                for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); d++) {
                    for (int h = -((int) Math.ceil(this.getDimensions(Pose.STANDING).height()) + range); h <= Math.ceil(this.getDimensions(Pose.STANDING).height()); h++) {
                        BlockPos blockPos = this.blockPosition().offset(w, h, d);
                        BlockState blockState = this.level().getBlockState(blockPos);
                        BlockState upperBlockState = this.level().getBlockState(blockPos.above());
                        if (upperBlockState.getBlock() == Blocks.AIR && blockState.getBlock() == Blocks.DIRT) {
                            this.level().setBlockAndUpdate(blockPos, Blocks.MYCELIUM.defaultBlockState());
                        }
                    }
                }
            }
        }

        // Particles:
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.PORTAL, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width() * 2, this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width() * 2, 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.MYCELIUM, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width() * 2, this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width() * 2, 0.0D, 0.0D, 0.0D);
            }
        }
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;
        return true;
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
    //                    Taking Damage
    // ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 0F;
        else return super.getDamageModifier(damageSrc);
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
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }
}
