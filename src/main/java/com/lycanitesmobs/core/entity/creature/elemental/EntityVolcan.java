package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.HashMap;
import java.util.List;

public class EntityVolcan extends TameableCreatureEntity implements Enemy {

    private int blockMeltingRadius = 2;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityVolcan(EntityType<? extends EntityVolcan> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new AttackMeleeGoal(this).setLongMemory(true));
    }

    @Override
    public void loadCreatureFlags() {
        this.blockMeltingRadius = this.creatureInfo.getFlag("blockMeltingRadius", this.blockMeltingRadius);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Burning Aura Attack:
        if (!this.level().isClientSide() && this.updateTick % 40 == 0) {
            List aoeTargets = this.getNearbyEntities(LivingEntity.class, null, 4);
            for (Object entityObj : aoeTargets) {
                LivingEntity target = (LivingEntity) entityObj;
                if (target != this && this.canAttackType(target.getType()) && this.canAttack(target) && this.getSensing().hasLineOfSight(target)) {
                    target.igniteForSeconds(2);
                }
            }
        }

        // Melt Blocks:
        if (!this.level().isClientSide() && this.updateTick % 40 == 0 && this.blockMeltingRadius > 0 && !this.isTamed() && LMHelperClass.getGameRuleBool(this.level(), GameRules.MOB_GRIEFING, true)) {
            int range = this.blockMeltingRadius;
            for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); w++) {
                for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); d++) {
                    for (int h = -((int) Math.ceil(this.getDimensions(Pose.STANDING).height()) + range); h <= Math.ceil(this.getDimensions(Pose.STANDING).height()); h++) {
                        BlockState meltState = this.level().getBlockState(this.blockPosition().offset(w, h, d));
                        if (meltState.is(LycanitesBlockTags.VOLCAN_MELTABLE)) {
                            BlockState blockState = Blocks.LAVA.defaultBlockState().setValue(BlockStateProperties.LEVEL, 5);
                            this.level().setBlockAndUpdate(this.blockPosition().offset(w, h, d), blockState);
                        }
						/*else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.ICE || block == Blocks.SNOW) {
							this.getEntityWorld().setBlockState(this.getPosition().add(w, h, d), Blocks.AIR.getDefaultState(), 3);
						}*/
                    }
                }
            }
        }

        // Particles:
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.FLAME, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.DRIPPING_LAVA, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }
            if (this.tickCount % 10 == 0)
                for (int i = 0; i < 2; ++i) {
                    this.level().addParticle(ParticleTypes.FLAME, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
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

        // Silverfish Extermination:
        if (target instanceof Silverfish) {
            target.remove(RemovalReason.DISCARDED);
        }

        return true;
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        return true;
    }

    // ========== Get Interact Commands ==========
    @Override
    public HashMap<Integer, String> getInteractCommands(Player player, ItemStack itemStack) {
        HashMap<Integer, String> commands = new HashMap<>();
        commands.putAll(super.getInteractCommands(player, itemStack));

        if (itemStack != null) {
            // Water:
            if (itemStack.getItem() == Items.BUCKET && this.isTamed())
                commands.put(COMMAND_PIORITIES.ITEM_USE.id, "Water");
        }

        return commands;
    }

    // ========== Perform Command ==========
    @Override
    public boolean performCommand(String command, Player player, ItemStack itemStack, InteractionHand hand) {

        // Water:
        if (command.equals("Water")) {
            this.replacePlayersItem(player, hand, itemStack, new ItemStack(Items.LAVA_BUCKET));
            return true;
        }

        return super.performCommand(command, player, itemStack, hand);
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
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
}
