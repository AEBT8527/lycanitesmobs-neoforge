package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3d;

import java.util.HashMap;

public class EntityXaphan extends TameableCreatureEntity implements Enemy {
    private int nextSplash = 20;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityXaphan(EntityType<? extends EntityXaphan> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsInWater = true;
        this.hasAttackSound = false;
        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(10.0F));
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide() && this.updateTick % this.nextSplash == 0) {
            this.fireProjectile("acidsplash", null, 0, 0, new Vector3d(0.5D - this.getRandom().nextDouble(), 0, 0.5D - this.getRandom().nextDouble()), 0f, (float) this.nextSplash / 20, 1F);
            this.nextSplash = 20 + this.getRandom().nextInt(20);
        }

        // Particles:
        if (this.level().isClientSide())
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.BUBBLE_POP, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        for (int row = -1; row <= 1; row++) {
            int projectileCount = 10;
            for (int i = 0; i < projectileCount; i++) {
                this.fireProjectile("acidsplash", target, range, (90 / projectileCount) * i, new Vector3d(0, 3 * row, 0), 0.6f, 2f, 1F);
            }
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
    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        var entity = source.getEntity();
        if (entity instanceof EntityXaphan && this.getPlayerOwner() == ((EntityXaphan) entity).getPlayerOwner()) {
            return true;
        }
        if (source.type().equals(ObjectManager.getDamageSource(this.level(), "acid").type())) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }

    @Override
    public boolean canBurn() {
        return false;
    }


    // ==================================================
    //                     Interact
    // ==================================================
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


    // ==================================================
    //                   Brightness
    // ==================================================
    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
}
