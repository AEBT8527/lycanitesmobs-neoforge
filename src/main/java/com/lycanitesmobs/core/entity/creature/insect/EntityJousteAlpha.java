package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityJousteAlpha extends AgeableCreatureEntity {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityJousteAlpha(EntityType<? extends EntityJousteAlpha> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.attackCooldownMax = 10;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(this.getClass()));

        super.registerGoals();

        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // Pathing Weight:
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.level().getBlockState(new BlockPos(x, y - 1, z));
        if (blockState.getBlock() != Blocks.AIR) {
            if (Material.SAND.contains(blockState.getBlock()))
                return 10F;
            if (Material.CLAY.contains(blockState.getBlock()))
                return 7F;
            if (Material.STONE.contains(blockState.getBlock()))
                return 5F;
        }
        return super.getBlockPathWeight(x, y, z);
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    @Override
    public boolean canAttackOwnSpecies() {
        return true;
    }

    // ========== Set Attack Target ==========
    @Override
    public void setTarget(LivingEntity entity) {
        if (entity == null && this.getTarget() instanceof EntityJousteAlpha && this.getHealth() < this.getMaxHealth()) {
            this.heal((this.getMaxHealth() - this.getHealth()) / 2);
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 2, false, true));
        }
        super.setTarget(entity);
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
        if (source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }


    // ==================================================
    //                     Breeding
    // ==================================================
    // ========== Create Child ==========
    @Override
    public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
        return (AgeableCreatureEntity) CreatureManager.getInstance().getCreature("jouste").createEntity(this.level());
    }
}
