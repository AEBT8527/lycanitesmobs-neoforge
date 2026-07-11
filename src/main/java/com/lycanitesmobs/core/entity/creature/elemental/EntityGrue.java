package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.StealthGoal;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class EntityGrue extends TameableCreatureEntity implements Enemy {
    public static final byte ATTACK_NONE = 0, ATTACK_SWIPE = 1, ATTACK_BITE = 2;
    private static final EntityDataAccessor<Byte> ATTACK_ANIM = SynchedEntityData.defineId(EntityGrue.class, EntityDataSerializers.BYTE);

    private int teleportTime = 60;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityGrue(EntityType<? extends EntityGrue> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.spawnsInWater = true;
        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimPriorityGoalIndex(), new StealthGoal(this).setStealthTime(20).setStealthAttack(true).setStealthMove(true));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(true));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Random Target Teleporting:
        if (!this.level().isClientSide() && this.hasAttackTarget()) {
            if (this.teleportTime-- <= 0) {
                this.teleportTime = 60 + this.getRandom().nextInt(40);
                BlockPos teleportPosition = this.getFacingPosition(this.getTarget(), -this.getTarget().getDimensions(Pose.STANDING).width() - 1D, 0);
                if (this.canTeleportTo(teleportPosition)) {
                    this.playJumpSound();
                    this.setPos(teleportPosition.getX(), teleportPosition.getY(), teleportPosition.getZ());
                }
            }
        }

        // Particles:
        if (this.level().isClientSide())
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.WITCH, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }
    }

    /**
     * Checks if this entity can teleport to the provided block position.
     *
     * @param pos The position to teleport to.
     * @return True if it's safe to teleport.
     */
    public boolean canTeleportTo(BlockPos pos) {
        for (int y = 0; y <= 1; y++) {
            BlockState blockState = this.level().getBlockState(pos.offset(0, y, 0));
            if (blockState.canOcclude())
                return false;
        }
        return true;
    }


    // ==================================================
    //                     Stealth
    // ==================================================
    @Override
    public boolean canStealth() {
        if (this.level().isClientSide()) return false;
        return this.testLightLevel() <= 0;
    }

    @Override
    public void startStealth() {
        if (this.level().isClientSide()) {
            ParticleOptions particle = ParticleTypes.WITCH;
            double d0 = this.getRandom().nextGaussian() * 0.02D;
            double d1 = this.getRandom().nextGaussian() * 0.02D;
            double d2 = this.getRandom().nextGaussian() * 0.02D;
            for (int i = 0; i < 100; i++)
                this.level().addParticle(particle, this.position().x() + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).width() * 2.0F) - (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + 0.5D + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).height()), this.position().z() + (double) (this.getRandom().nextFloat() * this.getDimensions(Pose.STANDING).width() * 2.0F) - (double) this.getDimensions(Pose.STANDING).width(), d0, d1, d2);
        }
        super.startStealth();
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        boolean bite = this.getRandom().nextFloat() < 0.5F;
        this.startAttackAnim(bite);
        // Leech:
        if (this.isRareVariant() && target instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) target;
            List<MobEffect> goodEffects = new ArrayList<>();
            for (MobEffectInstance effectInstance : targetLiving.getActiveEffects()) {
                if (ObjectLists.inEffectList("buffs", effectInstance.getEffect().value()))
                    goodEffects.add(effectInstance.getEffect().value());
            }
            if (goodEffects.size() > 0) {
                if (goodEffects.size() > 1)
                    targetLiving.removeEffect(ObjectManager.holder(goodEffects.get(this.getRandom().nextInt(goodEffects.size()))));
                else
                    targetLiving.removeEffect(ObjectManager.holder(goodEffects.get(0)));
                float leeching = Math.max(1, this.getAttackDamage(damageScale) / 2);
                this.heal(leeching);
            }
        }

        return true;
    }


    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_ANIM, ATTACK_NONE);
    }

    public void startAttackAnim(boolean bite) {
        this.entityData.set(ATTACK_ANIM, bite ? ATTACK_BITE : ATTACK_SWIPE);
    }

    public byte getAttackAnimType() {
        return this.entityData.get(ATTACK_ANIM);
    }

    public float getMeleeAttackAnim(float pt) {
        int max = Math.max(1, this.getAttackCooldownMax());
        float cur = this.attackCooldown;
        float t = (max - (cur - pt)) / (float) max;
        return Mth.clamp(t, 0F, 1F);
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
    //                     Immunities
    // ==================================================
    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        if (source.is(DamageTypes.IN_WALL)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }

    /**
     * Returns true if this mob should be damaged by the sun.
     **/
    public boolean daylightBurns() {
        return false;
    }

    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }


    // ==================================================
    //                       Visuals
    // ==================================================
    @Override
    public Identifier getTexture(String suffix) {
        if (!this.hasCustomName() || !"Shadow Clown".equals(this.getCustomName().getString()))
            return super.getTexture(suffix);

        String textureName = this.getTextureName() + "_shadowclown";
        if (!"".equals(suffix)) {
            textureName += "_" + suffix;
        }
        return AssetHelper.entityTexture(textureName);
    }
}
