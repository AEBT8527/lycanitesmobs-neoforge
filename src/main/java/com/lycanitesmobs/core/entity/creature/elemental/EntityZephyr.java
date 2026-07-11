package com.lycanitesmobs.core.entity.creature.elemental;

import com.lycanitesmobs.core.entity.IFusable;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class EntityZephyr extends TameableCreatureEntity implements Enemy, IFusable {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityZephyr(EntityType<? extends EntityZephyr> entityType, Level world) {
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
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Particles:
        //if(this.getEntityWorld().isRemote)
        //this.getEntityWorld().addParticle(ParticleTypes.SNOWBALL, this.getPositionVec().getX() + (this.rand.nextDouble() - 0.5D) * (double)this.getSize(Pose.STANDING).width(), this.getPositionVec().getY() + this.rand.nextDouble() * (double)this.getSize(Pose.STANDING).height(), this.getPositionVec().getZ() + (this.rand.nextDouble() - 0.5D) * (double)this.getSize(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        int projectileCount = 10;
        for (int i = 0; i < projectileCount; i++) {
            this.fireProjectile("whirlwind", target, range, (360 / projectileCount) * i, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
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
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CACTUS)) return true;
        if (source.is(DamageTypes.LIGHTNING_BOLT) && !this.isTamed()) {
            return true;
        }
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
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


    // ==================================================
    //                      Fusion
    // ==================================================
    protected IFusable fusionTarget;

    @Override
    public IFusable getFusionTarget() {
        return this.fusionTarget;
    }

    @Override
    public void setFusionTarget(IFusable fusionTarget) {
        this.fusionTarget = fusionTarget;
    }

    @Override
    public EntityType<? extends LivingEntity> getFusionType(IFusable fusable) {
        if (fusable instanceof EntityCinder) {
            return CreatureManager.getInstance().getEntityType("raidra");
        }
        if (fusable instanceof EntityJengu) {
            return CreatureManager.getInstance().getEntityType("reiver");
        }
        if (fusable instanceof EntityGeonach) {
            return CreatureManager.getInstance().getEntityType("banshee");
        }
        if (fusable instanceof EntityAegis) {
            return CreatureManager.getInstance().getEntityType("sylph");
        }
        if (fusable instanceof EntityArgus) {
            return CreatureManager.getInstance().getEntityType("wraith");
        }
        return null;
    }
}
