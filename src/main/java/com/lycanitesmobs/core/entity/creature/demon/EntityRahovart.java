package com.lycanitesmobs.core.entity.creature.demon;

import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.IGroupBoss;
import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.util.GoalConditions;
import com.lycanitesmobs.core.entity.goals.actions.FindNearbyPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FaceTargetGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.FireProjectilesGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.HealWhenNoPlayersGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.SummonMinionsGoal;
import com.lycanitesmobs.core.entity.projectile.hellfire.EntityHellfireBarrier;
import com.lycanitesmobs.core.entity.projectile.hellfire.EntityHellfireOrb;
import com.lycanitesmobs.core.entity.projectile.hellfire.EntityHellfireWave;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

import com.lycanitesmobs.core.entity.creature.elemental.EntityWraith;

public class EntityRahovart extends BaseCreatureEntity implements Enemy, IGroupHeavy, IGroupBoss {

    protected int hellfireEnergy = 0;
    protected List<EntityHellfireOrb> hellfireOrbs = new ArrayList<>();

    // Data Manager:
    protected static final EntityDataAccessor<Integer> HELLFIRE_ENERGY = SynchedEntityData.defineId(EntityRahovart.class, EntityDataSerializers.INT);

    // First Phase:
    protected List<EntityBelphegor> hellfireBelphegorMinions = new ArrayList<>();

    // Second Phase:
    protected List<EntityBehemophet> hellfireBehemophetMinions = new ArrayList<>();
    protected int hellfireWallTime = 0;
    protected int hellfireWallTimeMax = 20 * 20;
    protected boolean hellfireWallClockwise = false;
    protected EntityHellfireBarrier hellfireWallLeft;
    protected EntityHellfireBarrier hellfireWallRight;

    // Third Phase:
    protected List<EntityHellfireBarrier> hellfireBarriers = new ArrayList<>();
    protected int hellfireBarrierHealth = 100;


    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityRahovart(EntityType<? extends EntityRahovart> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEAD;
        this.hasAttackSound = false;
        this.setAttackCooldownMax(40);
        this.solidCollision = true;
        //this.pushthrough = 1.0F;
        this.setupMob();
        this.hitAreaWidthScale = 2F;

        // Boss:
        this.damageMax = BaseCreatureEntity.getBossDamageLimit();
        this.damageLimit = BaseCreatureEntity.getBossDamageLimit();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(this.currentFindTargetGoalIndex(), new FindNearbyPlayersGoal(this));

        // All Phases:
        this.goalSelector.addGoal(this.currentIdleGoalIndex(), new FaceTargetGoal(this));
        this.goalSelector.addGoal(this.currentIdleGoalIndex(), new HealWhenNoPlayersGoal(this));
        this.goalSelector.addGoal(this.currentIdleGoalIndex(), new SummonMinionsGoal(this).setMinionInfo("wraith").setAntiFlight(true));
        this.goalSelector.addGoal(this.currentIdleGoalIndex(), new FireProjectilesGoal(this).setProjectile("hellfireball").setFireRate(40).setVelocity(1.6F).setScale(8F).setAllPlayers(true));
        this.goalSelector.addGoal(this.currentIdleGoalIndex(), new FireProjectilesGoal(this).setProjectile("hellfireball").setFireRate(60).setVelocity(1.6F).setScale(8F));

        // Phase 3:
        this.goalSelector.addGoal(this.currentIdleGoalIndex(), new SummonMinionsGoal(this).setMinionInfo("apollyon").setSummonRate(20 * 10).setSummonCap(1).setPerPlayer(true).setSizeScale(2)
                .setConditions(new GoalConditions().setBattlePhase(2)));

        super.registerGoals();
    }

    // ========== Init ==========

    /**
     * Initiates the entity setting all the values to be watched by the data manager.
     **/
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(EntityRahovart.HELLFIRE_ENERGY, this.hellfireEnergy);
    }

    // ========== Rendering Distance ==========

    /**
     * Returns a larger bounding box for rendering this large entity.
     **/
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(200, 50, 200).move(0, -25, 0);
    }

    // ========== First Spawn ==========
    @Override
    public void onFirstSpawn() {
        super.onFirstSpawn();
        if (this.getArenaCenter() == null) {
            this.setArenaCenter(this.blockPosition());
        }
    }


    // ==================================================
    //                      Positions
    // ==================================================
    // ========== Arena Center ==========

    /**
     * Sets the central arena point for this mob to use.
     **/
    public void setArenaCenter(BlockPos pos) {
        super.setArenaCenter(pos);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Look At Target:
        if (this.hasAttackTarget() && !this.level().isClientSide()) {
            this.getLookControl().setLookAt(this.getTarget(), 30.0F, 30.0F);
        }

        // Arena Snapping:
        if (this.hasArenaCenter()) {
            BlockPos arenaPos = this.getArenaCenter();
            double arenaY = this.position().y();
            if (this.level().isEmptyBlock(arenaPos))
                arenaY = arenaPos.getY();
            else if (this.level().isEmptyBlock(arenaPos.offset(0, 1, 0)))
                arenaY = arenaPos.offset(0, 1, 0).getY();

            if (this.position().x() != arenaPos.getX() || this.position().y() != arenaY || this.position().z() != arenaPos.getZ())
                this.setPos(arenaPos.getX(), arenaY, arenaPos.getZ());
        }

        // Sync Hellfire Energy:
        if (!this.level().isClientSide())
            this.entityData.set(HELLFIRE_ENERGY, this.hellfireEnergy);
        else
            this.hellfireEnergy = this.entityData.get(HELLFIRE_ENERGY);

        // Hellfire Update:
        updateHellfireOrbs(this, this.updateTick, 5, this.hellfireEnergy, 10, this.hellfireOrbs);

        // Update Phases:
        if (!this.level().isClientSide()) {
            this.updatePhases();
        }
    }

    @Override
    public boolean rollWanderChance() {
        return false;
    }

    // ========== Phases Update ==========
    public void updatePhases() {

        // ===== First Phase - Hellfire Wave =====
        if (this.getBattlePhase() == 0) {
            // Clean Up:
            if (!this.hellfireBehemophetMinions.isEmpty()) {
                for (EntityBehemophet minion : this.hellfireBehemophetMinions.toArray(new EntityBehemophet[this.hellfireBehemophetMinions.size()])) {
                    minion.resetHellfireEnergy();
                }
                this.hellfireBehemophetMinions = new ArrayList<>();
            }
            this.hellfireWallTime = 0;
            this.hellfireBarrierCleanup();

            // Hellfire Minion Update - Every Second:
            if (this.updateTick % 20 == 0) {
                for (EntityBelphegor minion : this.hellfireBelphegorMinions.toArray(new EntityBelphegor[this.hellfireBelphegorMinions.size()])) {
                    if (!minion.isAlive()) {
                        this.onMinionDeath(minion, null);
                        continue;
                    }
                    minion.addHellfireEnergy(5); // Charged after 20 secs.
                    if (minion.hasFullHellfireEnergy()) {
                        this.hellfireEnergy += 20;
                        this.onMinionDeath(minion, null);
                        this.level().explode(minion, minion.position().x(), minion.position().y(), minion.position().z(), 1, Level.ExplosionInteraction.NONE);
                        minion.resetHellfireEnergy();
                        minion.remove(RemovalReason.DISCARDED);
                        continue;
                    }
                }
            }

            // Hellfire Charged:
            if (this.hellfireEnergy >= 100) {
                this.hellfireEnergy = 0;
                double angle = this.getRandom().nextFloat() * 360;
                if (this.hasAttackTarget()) {
                    double deltaX = this.getTarget().position().x() - this.position().x();
                    double deltaZ = this.getTarget().position().z() - this.position().z();
                    angle = Math.atan2(deltaZ, deltaX) * 180 / Math.PI;
                }
                this.hellfireWaveAttack(angle);
            }

            // Every 5 Secs:
            if (this.updateTick % 100 == 0) {
                int summonAmount = this.getRandom().nextInt(4); // 0-3 Hellfire Belphegors
                summonAmount *= this.getPlayerTargetCount();
                if (summonAmount > 0)
                    for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                        EntityBelphegor minion = (EntityBelphegor) CreatureManager.getInstance().getCreature("belphegor").createEntity(this.level());
                        this.summonMinion(minion, this.getRandom().nextDouble() * 360, 5);
                        this.hellfireBelphegorMinions.add(minion);
                    }
            }
        }

        // ===== Second Phase - Hellfire Wall =====
        if (this.getBattlePhase() == 1) {
            // Clean Up:
            if (!this.hellfireBelphegorMinions.isEmpty()) {
                for (EntityBelphegor minion : this.hellfireBelphegorMinions.toArray(new EntityBelphegor[this.hellfireBelphegorMinions.size()])) {
                    minion.resetHellfireEnergy();
                }
                this.hellfireBelphegorMinions = new ArrayList<>();
            }
            this.hellfireBarrierCleanup();

            // Hellfire Minion Update - Every Second:
            if (this.hellfireWallTime <= 0 && this.updateTick % 20 == 0) {
                for (EntityBehemophet minion : this.hellfireBehemophetMinions.toArray(new EntityBehemophet[this.hellfireBehemophetMinions.size()])) {
                    if (!minion.isAlive()) {
                        this.onMinionDeath(minion, null);
                        continue;
                    }
                    minion.addHellfireEnergy(5); // Charged after 20 secs.
                    if (minion.hasFullHellfireEnergy()) {
                        this.hellfireEnergy += 20;
                        this.onMinionDeath(minion, null);
                        this.level().explode(minion, minion.position().x(), minion.position().y(), minion.position().z(), 1, Level.ExplosionInteraction.NONE);
                        minion.resetHellfireEnergy();
                        minion.remove(RemovalReason.DISCARDED);
                        continue;
                    }
                }
            }

            // Hellfire Charged:
            if (this.hellfireEnergy >= 100) {
                this.hellfireEnergy = 0;
                this.hellfireWallAttack(this.yRot);
            }

            // Hellfire Wall:
            if (this.hellfireWallTime > 0) {
                this.hellfireWallUpdate();
                this.hellfireWallTime--;
            }

            // Every 20 Secs:
            if (this.updateTick % 400 == 0) {
                int summonAmount = 2; // 2 Hellfire Behemophet
                summonAmount *= this.getPlayerTargetCount();
                if (summonAmount > 0)
                    for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                        EntityBehemophet minion = (EntityBehemophet) CreatureManager.getInstance().getCreature("behemophet").createEntity(this.level());
                        this.summonMinion(minion, this.getRandom().nextDouble() * 360, 5);
                        this.hellfireBehemophetMinions.add(minion);
                    }
            }

            // Every 10 Secs:
            if (this.updateTick % 200 == 0) {
                int summonAmount = this.getRandom().nextInt(4) - 1; // 0-2 Belphegors with 50% fail chance.
                for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                    EntityBelphegor minion = (EntityBelphegor) CreatureManager.getInstance().getCreature("belphegor").createEntity(this.level());
                    this.summonMinion(minion, this.getRandom().nextDouble() * 360, 5);
                }
            }
        }

        // ===== Third Phase - Hellfire Barrier =====
        if (this.getBattlePhase() >= 2) {
            // Clean Up:
            if (!this.hellfireBelphegorMinions.isEmpty()) {
                for (EntityBelphegor minion : this.hellfireBelphegorMinions.toArray(new EntityBelphegor[this.hellfireBelphegorMinions.size()])) {
                    minion.resetHellfireEnergy();
                }
                this.hellfireBelphegorMinions = new ArrayList<>();
            }
            if (!this.hellfireBehemophetMinions.isEmpty()) {
                for (EntityBehemophet minion : this.hellfireBehemophetMinions.toArray(new EntityBehemophet[this.hellfireBehemophetMinions.size()])) {
                    minion.resetHellfireEnergy();
                }
                this.hellfireBehemophetMinions = new ArrayList<>();
            }
            this.hellfireWallTime = 0;

            // Hellfire Energy - Every Second:
            if (this.updateTick % 20 == 0) {
                if (this.hellfireEnergy < 100)
                    this.hellfireEnergy += 5;
            }

            // Hellfire Charged:
            if (this.hellfireEnergy >= 100 && this.hellfireBarriers.size() < 20) {
                this.hellfireEnergy = 0;
                this.hellfireBarrierAttack(360F * this.getRandom().nextFloat());
            }

            // Hellfire Barriers:
            if (this.hellfireBarriers.size() > 0)
                this.hellfireBarrierUpdate();

            // Every 10 Secs:
            if (this.updateTick % 200 == 0) {
                int summonAmount = this.getRandom().nextInt(2); // 0-1 Hellfire Behemophet
                summonAmount *= this.getPlayerTargetCount();
                if (summonAmount > 0)
                    for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                        EntityBehemophet minion = (EntityBehemophet) CreatureManager.getInstance().getCreature("behemophet").createEntity(this.level());
                        this.summonMinion(minion, this.getRandom().nextDouble() * 360, 5);
                        this.hellfireBehemophetMinions.add(minion);
                    }
            }

            // Every 20 Secs:
            if (this.updateTick % 400 == 0) {
                int summonAmount = this.getRandom().nextInt(4); // 0-3 Belphegors
                summonAmount *= this.getPlayerTargetCount();
                if (summonAmount > 0)
                    for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                        EntityBelphegor minion = (EntityBelphegor) CreatureManager.getInstance().getCreature("belphegor").createEntity(this.level());
                        this.summonMinion(minion, this.getRandom().nextDouble() * 360, 5);
                    }
                summonAmount = this.getRandom().nextInt(3); // 0-2 Wraiths
                summonAmount *= this.getPlayerTargetCount();
                if (summonAmount > 0)
                    for (int summonCount = 0; summonCount <= summonAmount; summonCount++) {
                        EntityWraith minion = (EntityWraith) CreatureManager.getInstance().getCreature("wraith").createEntity(this.level());
                        this.summonMinion(minion, this.getRandom().nextDouble() * 360, 5);
                    }
            }
        }

        if (this.hellfireWallTime <= 0)
            this.hellfireWallCleanup();
    }

    // ========== Minion Death ==========
    @Override
    public void onMinionDeath(LivingEntity minion, DamageSource damageSource) {
        if (minion instanceof EntityBelphegor && this.hellfireBelphegorMinions.contains(minion)) {
            this.hellfireBelphegorMinions.remove(minion);
            return;
        }
        if (minion instanceof EntityBehemophet && this.hellfireBehemophetMinions.contains(minion)) {
            this.hellfireBehemophetMinions.remove(minion);
            return;
        }
        if (this.hellfireBarriers.size() > 0) {
            if (minion instanceof EntityBehemophet)
                this.hellfireBarrierHealth -= 100;
            else
                this.hellfireBarrierHealth -= 50;
        }
        super.onMinionDeath(minion, damageSource);
    }


    // ==================================================
    //                  Battle Phases
    // ==================================================
    @Override
    public void updateBattlePhase() {
        double healthNormal = this.getHealth() / this.getMaxHealth();
        if (healthNormal <= 0.2D) {
            this.setBattlePhase(2);
            return;
        }
        if (healthNormal <= 0.6D) {
            this.setBattlePhase(1);
            return;
        }
        this.setBattlePhase(0);
    }


    // ==================================================
    //                     Hellfire
    // ==================================================
    public static void updateHellfireOrbs(LivingEntity entity, long orbTick, int hellfireOrbMax, int hellfireOrbEnergy, float orbSize, List<EntityHellfireOrb> hellfireOrbs) {
        if (!entity.level().isClientSide())
            return;

        int hellfireChargeCount = Math.round((float) Math.min(hellfireOrbEnergy, 100) / (100F / hellfireOrbMax));
        int hellfireOrbRotationTime = 5 * 20;
        double hellfireOrbAngle = 360 * ((float) (orbTick % hellfireOrbRotationTime) / hellfireOrbRotationTime);
        double hellfireOrbAngleOffset = 360.0D / hellfireOrbMax;

        // Add Required Orbs:
        while (hellfireOrbs.size() < hellfireChargeCount) {
            EntityHellfireOrb hellfireOrb = new EntityHellfireOrb(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireOrb.class), entity.level(), entity);
            hellfireOrb.setClientOnly(true);
            hellfireOrbs.add(hellfireOrb);
            DeferredLevelActionManager.spawnEntityNow(entity.level(), hellfireOrb);
            hellfireOrb.setProjectileScale(orbSize);
        }

        // Remove Excess Orbs:
        while (hellfireOrbs.size() > hellfireChargeCount) {
            hellfireOrbs.get(hellfireOrbs.size() - 1).remove(RemovalReason.DISCARDED);
            hellfireOrbs.remove(hellfireOrbs.size() - 1);
        }

        // Update Orbs:
        for (int i = 0; i < hellfireOrbs.size(); i++) {
            EntityHellfireOrb hellfireOrb = hellfireOrbs.get(i);
            double rotationRadians = Math.toRadians((hellfireOrbAngle + (hellfireOrbAngleOffset * i)) % 360);
            double x = (entity.getDimensions(Pose.STANDING).width() * 1.25D) * Math.cos(rotationRadians) + Math.sin(rotationRadians);
            double z = (entity.getDimensions(Pose.STANDING).width() * 1.25D) * Math.sin(rotationRadians) - Math.cos(rotationRadians);
            hellfireOrb.setPos(
                    entity.position().x() - x,
                    entity.position().y() + (entity.getDimensions(Pose.STANDING).height() * 0.75F),
                    entity.position().z() - z
            );
            hellfireOrb.setPos(entity.position().x() - x, entity.position().y() + (entity.getDimensions(Pose.STANDING).height() * 0.75F), entity.position().z() - z);
            hellfireOrb.setProjectileLife(5);
        }
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    public boolean canAttack(LivingEntity targetEntity) {
        if (targetEntity instanceof EntityBelphegor || targetEntity instanceof EntityBehemophet || targetEntity instanceof EntityApollyon || targetEntity instanceof EntityWraith) {
            if (targetEntity instanceof TameableCreatureEntity)
                return ((TameableCreatureEntity) targetEntity).getOwner() instanceof Player;
            else
                return false;
        }
        return super.canAttack(targetEntity);
    }

    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("hellfireball", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 8f, 1F);
        super.attackRanged(target, range);
    }

    // ========== Hellfire Wave ==========
    public void hellfireWaveAttack(double angle) {
        this.triggerAttackCooldown();
        this.playAttackSound();
        EntityHellfireWave hellfireWave = new EntityHellfireWave(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireWave.class), this.level(), this);
        hellfireWave.setPos(
                hellfireWave.position().x(),
                this.position().y(),
                hellfireWave.position().z()
        );
        hellfireWave.setRotation(angle);
        DeferredLevelActionManager.spawnEntity(this.level(), hellfireWave.blockPosition(), null, hellfireWave);
    }

    // ========== Hellfire Wall ==========
    public void hellfireWallAttack(double angle) {
        this.playAttackSound();
        this.triggerAttackCooldown();

        this.hellfireWallTime = this.hellfireWallTimeMax;
        this.hellfireWallClockwise = this.getRandom().nextBoolean();
    }

    public void hellfireWallUpdate() {
        this.triggerAttackCooldown();

        double hellfireWallNormal = (double) this.hellfireWallTime / this.hellfireWallTimeMax;
        double hellfireWallAngle = 360;
        if (this.hellfireWallClockwise)
            hellfireWallAngle = -360;

        // Left (Positive) Wall:
        if (this.hellfireWallLeft == null) {
            this.hellfireWallLeft = new EntityHellfireBarrier(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireBarrier.class), this.level(), this);
            this.hellfireWallLeft.setWallMode(true);
            DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, this.hellfireWallLeft);
        }
        this.hellfireWallLeft.resetTime();
        this.hellfireWallLeft.setPos(
                this.position().x(),
                this.position().y(),
                this.position().z()
        );
        this.hellfireWallLeft.setRotation(hellfireWallNormal * hellfireWallAngle);

        // Right (Negative) Wall:
        if (this.hellfireWallRight == null) {
            this.hellfireWallRight = new EntityHellfireBarrier(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireBarrier.class), this.level(), this);
            this.hellfireWallRight.setWallMode(true);
            DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, this.hellfireWallRight);
        }
        this.hellfireWallRight.resetTime();
        this.hellfireWallRight.setPos(
                this.position().x(),
                this.position().y(),
                this.position().z()
        );
        this.hellfireWallRight.setRotation(180 + (hellfireWallNormal * hellfireWallAngle));
    }

    public void hellfireWallCleanup() {
        if (this.hellfireWallLeft != null) {
            this.hellfireWallLeft.remove(RemovalReason.DISCARDED);
            this.hellfireWallLeft = null;
        }
        if (this.hellfireWallRight != null) {
            this.hellfireWallRight.remove(RemovalReason.DISCARDED);
            this.hellfireWallRight = null;
        }
    }

    // ========== Hellfire Barrier ==========
    public void hellfireBarrierAttack(double angle) {
        if (this.hellfireBarriers.size() >= 10) {
            return;
        }
        this.triggerAttackCooldown();
        this.playAttackSound();

        EntityHellfireBarrier hellfireBarrier = new EntityHellfireBarrier(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireBarrier.class), this.level(), this);
        hellfireBarrier.resetTime();
        hellfireBarrier.setPos(
                this.position().x(),
                this.position().y(),
                this.position().z()
        );
        hellfireBarrier.setRotation(angle);
        this.hellfireBarriers.add(hellfireBarrier);
        DeferredLevelActionManager.spawnEntity(this.level(), hellfireBarrier.blockPosition(), null, hellfireBarrier);
    }

    public void hellfireBarrierUpdate() {
        if (this.hellfireBarrierHealth <= 0) {
            this.hellfireBarrierHealth = 100;
            if (this.hellfireBarriers.size() > 0) {
                EntityHellfireBarrier hellfireBarrier = this.hellfireBarriers.get(this.hellfireBarriers.size() - 1);
                hellfireBarrier.remove(RemovalReason.DISCARDED);
                this.hellfireBarriers.remove(this.hellfireBarriers.size() - 1);
            }
        }
        for (EntityHellfireBarrier hellfireBarrier : this.hellfireBarriers) {
            hellfireBarrier.resetTime();
            hellfireBarrier.setPos(
                    this.position().x(),
                    this.position().y(),
                    this.position().z()
            );
        }
    }

    public void hellfireBarrierCleanup() {
        if (this.level().isClientSide() || this.hellfireBarriers.size() < 1)
            return;
        for (EntityHellfireBarrier hellfireBarrier : this.hellfireBarriers) {
            hellfireBarrier.remove(RemovalReason.DISCARDED);
        }
        this.hellfireBarriers = new ArrayList<>();
        this.hellfireBarrierHealth = 100;
    }


    // ==================================================
    //                     Movement
    // ==================================================
    // ========== Can Be Pushed ==========
    @Override
    public boolean isPushable() {
        return false;
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean canBeAffected(MobEffectInstance potionEffect) {
        if (potionEffect.getEffect() == MobEffects.WITHER)
            return false;
        if (ObjectManager.getEffect("decay") != null)
            if (potionEffect.getEffect() == ObjectManager.getEffect("decay")) return false;
        super.canBeAffected(potionEffect);
        return true;
    }

    @Override
    public boolean canBurn() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        var entity = source.getEntity();
        if (entity instanceof ZombifiedPiglin) {
            entity.remove(RemovalReason.DISCARDED);
            return true;
        }
        if (entity instanceof IronGolem) {
            entity.remove(RemovalReason.DISCARDED);
            return true;
        }
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!player.getAbilities().invulnerable && player.position().y() > this.position().y() + CreatureManager.getInstance().getConfig().bossAntiFlight()) {
                return true;
            }
        }
        return super.isInvulnerableTo(lycLevel, source);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("HellfireEnergy")) {
            this.hellfireEnergy = nbt.getIntOr("HellfireEnergy", 0);
        }
        if (nbt.contains("HellfireWallTime")) {
            this.hellfireWallTime = nbt.getIntOr("HellfireWallTime", 0);
        }
        if (nbt.contains("BelphegorIDs")) {
            ListTag belphegorIDs = nbt.getListOrEmpty("BelphegorIDs");
            for (int i = 0; i < belphegorIDs.size(); i++) {
                CompoundTag belphegorID = belphegorIDs.getCompoundOrEmpty(i);
                if (belphegorID.contains("ID")) {
                    Entity entity = this.level().getEntity(belphegorID.getIntOr("ID", -1));
                    if (entity != null && entity instanceof EntityBelphegor)
                        this.hellfireBelphegorMinions.add((EntityBelphegor) entity);
                }
            }
        }
        if (nbt.contains("BehemophetIDs")) {
            ListTag behemophetIDs = nbt.getListOrEmpty("BehemophetIDs");
            for (int i = 0; i < behemophetIDs.size(); i++) {
                CompoundTag behemophetID = behemophetIDs.getCompoundOrEmpty(i);
                if (behemophetID.contains("ID")) {
                    Entity entity = this.level().getEntity(behemophetID.getIntOr("ID", -1));
                    if (entity != null && entity instanceof EntityBehemophet)
                        this.hellfireBehemophetMinions.add((EntityBehemophet) entity);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("HellfireEnergy", this.hellfireEnergy);
        nbt.putInt("HellfireWallTime", this.hellfireWallTime);
        if (this.getBattlePhase() == 0) {
            ListTag belphegorIDs = new ListTag();
            for (EntityBelphegor entityBelphegoregor : this.hellfireBelphegorMinions) {
                CompoundTag belphegorID = new CompoundTag();
                belphegorID.putInt("ID", entityBelphegoregor.getId());
                belphegorIDs.add(belphegorID);
            }
            nbt.put("BelphegorIDs", belphegorIDs);
        }
        if (this.getBattlePhase() == 1) {
            ListTag behemophetIDs = new ListTag();
            for (EntityBehemophet entityBehemophet : this.hellfireBehemophetMinions) {
                CompoundTag behemophetID = new CompoundTag();
                behemophetID.putInt("ID", entityBehemophet.getId());
                behemophetIDs.add(behemophetID);
            }
            nbt.put("BehemophetIDs", behemophetIDs);
        }
    }


    // ==================================================
    //                       Sounds
    // ==================================================
    // ========== Step ==========
    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        if (this.hasArenaCenter())
            return;
        super.playStepSound(pos, block);
    }


    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
}