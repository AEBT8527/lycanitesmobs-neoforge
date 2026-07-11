package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.data.info.ObjectLists;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3d;

public class EntityVapula extends TameableCreatureEntity implements Enemy {

    private int blockBreakRadius = 0;

    private float fireDamageAbsorbed = 0;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityVapula(EntityType<? extends EntityVapula> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
        this.attackPhaseMax = 8;
        this.setAttackCooldownMax(60);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false).setMaxChaseDistanceSq(3.0F));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setRange(18.0F).setMinChaseDistance(10.0F).setCheckSight(false));
    }

    @Override
    public void loadCreatureFlags() {
        this.blockBreakRadius = this.creatureInfo.getFlag("blockBreakRadius", this.blockBreakRadius);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            if (this.isRareVariant() && !this.isPetType("familiar")) {
                // Random Charging:
                if (this.hasAttackTarget() && this.distanceTo(this.getTarget()) > 1 && this.getRandom().nextInt(20) == 0) {
                    if (this.position().y() - 1 > this.getTarget().position().y())
                        this.leap(6.0F, -1.0D, this.getTarget());
                    else if (this.position().y() + 1 < this.getTarget().position().y())
                        this.leap(6.0F, 1.0D, this.getTarget());
                    else
                        this.leap(6.0F, 0D, this.getTarget());
                    if (LMHelperClass.getGameRuleBool(this.level(), GameRules.MOB_GRIEFING, true) && this.blockBreakRadius > -1 && !this.isTamed()) {
                        this.destroyArea((int) this.position().x(), (int) this.position().y(), (int) this.position().z(), 10, true, this.blockBreakRadius);
                    }
                }
            }
        }

        // Particles:
        if (this.level().isClientSide() && !CreatureManager.getInstance().getConfig().disableBlockParticles()) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIAMOND_BLOCK.defaultBlockState()),
                        this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(),
                        this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(),
                        this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
        // Silverfish Extermination:
        if (this.hasAttackTarget() && this.getTarget() instanceof Silverfish)
            return 4.0F;
        return super.getAISpeedModifier();
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        // Silverfish Extermination:
        if (target instanceof Silverfish) {
            target.remove(RemovalReason.DISCARDED);
        }
        return true;
    }

    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("crystalshard", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        this.nextAttackPhase();
        super.attackRanged(target, range);
    }

    @Override
    public int getRangedCooldown() {
        if (this.getAttackPhase() < 7)
            return super.getRangedCooldown() / 24;
        return super.getRangedCooldown();
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
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
    @Override
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.getEntity() != null) {
            ItemStack heldItem = ItemStack.EMPTY;
            if (damageSrc.getEntity() instanceof LivingEntity) {
                LivingEntity entityLiving = (LivingEntity) damageSrc.getEntity();
                if (!entityLiving.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                    heldItem = entityLiving.getItemInHand(InteractionHand.MAIN_HAND);
                }
            }
            if (ObjectLists.isPickaxe(heldItem)) {
                return 3.0F;
            }
        }
        return super.getDamageModifier(damageSrc);
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
