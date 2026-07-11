package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public class EntityGrell extends RideableCreatureEntity {

    public EntityGrell(EntityType<? extends EntityGrell> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEAD;
        this.hasAttackSound = false;

        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
        this.hitAreaWidthScale = 1.5F;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false).setMaxChaseDistanceSq(3.0F));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.25D).setRange(40.0F).setMinChaseDistance(10.0F).setLongMemory(false));

        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(Ghast.class));
    }

    public boolean isFlying() {
        return true;
    }

    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.getBagSize();
    }

    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("acidglob", target, range, 0, new Vector3d(0, 0, 0), 0.6f, 2f, 1F);
        super.attackRanged(target, range);
    }

    @Override
    public boolean canBurn() {
        return false;
    }

        public double getPassengersRidingOffset() {
        return (double) this.getDimensions(Pose.STANDING).height() * 1.1D;
    }

    @Override
    public void mountAbility(Entity rider) {
        if (this.level().isClientSide())
            return;

        if (this.hasPickupEntity()) {
            this.dropPickupEntity();
            return;
        }

        if (this.getStamina() < this.getStaminaCost())
            return;

        if (rider instanceof Player) {
            Player player = (Player) rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("acidglob");
            if (projectileInfo != null) {
                BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), player);
                projectile.setProjectileScale(2F);
                projectile.shootFromRotation(player, player.xRot, player.yRot, -1, 1, 10);
                DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);
                this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                this.triggerAttackCooldown();
            }
        }

        this.applyStaminaCost();
    }

    public float getStaminaCost() {
        return 2;
    }

    public int getStaminaRecoveryWarmup() {
        return 2 * 20;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }
}
