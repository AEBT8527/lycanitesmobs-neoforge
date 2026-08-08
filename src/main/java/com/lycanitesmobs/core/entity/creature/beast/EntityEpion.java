package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;

public class EntityEpion extends RideableCreatureEntity implements Enemy {

    protected boolean griefing = true;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityEpion(EntityType<? extends EntityEpion> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEAD;
        this.hasAttackSound = false;
        this.flySoundSpeed = 20;

        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(0.75D).setRange(14.0F).setMinChaseDistance(6.0F));
    }

    @Override
    public void loadCreatureFlags() {
        this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
    }

    @Override
    public float getStrafeSpeed() {
        return 1F;
    }

    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Particles:
        if (this.level().isClientSide())
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.WITCH, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }

        if (!this.level().isClientSide() && this.shouldExplodeInDaylight() && this.isAlive()) {
            int explosionRadius = this.getSubspeciesIndex() > 0 ? 3 : 2;
            explosionRadius = Math.max(2, Math.round((float) explosionRadius * (float) this.getSizeScale()));
            this.level().explode(this, this.position().x(), this.position().y(), this.position().z(), explosionRadius, this.getDaylightExplosionInteraction());
            this.discard();
        }
    }

    @Override
    public boolean hasLineOfSight(Entity target) {
        if (this.isRareVariant()) {
            return true;
        }
        return super.hasLineOfSight(target);
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("bloodleech", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
        super.attackRanged(target, range);
    }


    // ==================================================
    //                     Abilities
    // ==================================================
    @Override
    public boolean isFlying() {
        if (this.level().isClientSide()) return true;
        if (this.shouldExplodeInDaylight())
            return false;
        return true;
    }

    protected boolean shouldExplodeInDaylight() {
        if (this.isTamed() || this.isMinion() || this.isRareVariant())
            return false;
        if (!this.daylightBurns() || !this.level().isBrightOutside())
            return false;

        BlockPos daylightPos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
        float brightness = this.getDaylightExplosionBrightness(daylightPos);
        return brightness > 0.5F && this.level().canSeeSkyFromBelowWater(daylightPos);
    }

    protected float getDaylightExplosionBrightness(BlockPos daylightPos) {
        float rawBrightness = (float) this.level().getMaxLocalRawBrightness(daylightPos) / 15.0F;
        float adjustedBrightness = rawBrightness / (4.0F - 3.0F * rawBrightness);
        return Mth.lerp(this.level().dimensionType().ambientLight(), adjustedBrightness, 1.0F);
    }

    protected Level.ExplosionInteraction getDaylightExplosionInteraction() {
        return this.griefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
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

    /**
     * Returns true if this mob should be damaged by the sun.
     **/
    @Override
    public boolean daylightBurns() {
        return !this.isMinion() && !this.hasMaster() && !this.isTamed() && !this.isRareVariant();
    }

    @Override
    public float getFallResistance() {
        return 100;
    }


    // ==================================================
    //                   Mount Ability
    // ==================================================
    public void mountAbility(Entity rider) {
        if (this.level().isClientSide())
            return;

        if (this.getStamina() < this.getStaminaCost())
            return;

        if (rider instanceof Player) {
            Player player = (Player) rider;
            ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("bloodleech");
            if (projectileInfo != null) {
                BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), player);
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
        return 0;
    }

    public float getStaminaRecoveryMax() {
        return 1.0F;
    }


    // ==================================================
    //                       Visuals
    // ==================================================

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    public Identifier getTexture() {
        if (!this.hasCustomName() || !"Vampire Bat".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_vampirebat";
        return AssetHelper.entityTexture(textureName);
    }

    // ========== Rendering Distance ==========

    /**
     * Returns a larger bounding box for rendering this large entity.
     **/
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(10, 10, 10).move(0, -5, 0);
    }
}
