package com.lycanitesmobs.core.entity.projectile.generic;

import com.lycanitesmobs.core.manager.ObjectManager;
import net.minecraft.core.registries.BuiltInRegistries;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.data.info.projectile.behaviours.ProjectileBehaviourLaser;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CustomProjectileEntity extends BaseProjectileEntity {
    /**
     * The Projectile Info to base this projectile from.
     **/
    protected ProjectileInfo projectileInfo;

    /**
     * The id of this projectile's thrower if any, used for network sync by some behaviours, -1 for none.
     **/
    protected int throwerId = -1;

    /**
     * The projectile that fired this projectile, if any.
     **/
    protected BaseProjectileEntity parent;

    /**
     * TThe target of this projectile if any, used for network sync by some behaviours.
     **/
    protected Entity target;

    /**
     * The id of the projectile that fired this projectile if any, used for network sync by some behaviours, -1 for none.
     **/
    protected int parentId = -1;

    /**
     * The id of the target entity of this projectile, used by some behaviours.
     **/
    protected int targetId = -1;

    /**
     * Used by laser behaviours to keep track of the laser ending position.
     **/
    protected Vec3 laserEnd;

    /**
     * The width of this projectile's laser, used by laser behaviours.
     **/
    protected float laserWidth;

    /**
     * The angle to fire a laser from where there is no entity aiming the laser, used by laser behaviours.
     **/
    protected float laserAngle;

    /**
     * A list of projectiles that was spawned by this projectile, used by behaviours.
     **/
    protected List<BaseProjectileEntity> spawnedProjectiles = new ArrayList<>();

    // Data Parameters:
    protected static final EntityDataAccessor<String> PROJECTILE_NAME = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.STRING);
    protected static final EntityDataAccessor<Integer> THROWING_ENTITY_ID = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> PARENT_PROJECTILE_ID = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> LASER_ANGLE = SynchedEntityData.defineId(CustomProjectileEntity.class, EntityDataSerializers.FLOAT);


    // ==================================================
    //                   Constructors
    // ==================================================
    public CustomProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
        this.modInfo = LycanitesMobs.modInfo;
    }

    public CustomProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, ProjectileInfo projectileInfo) {
        super(entityType, world);
        this.modInfo = LycanitesMobs.modInfo;
        this.setProjectileInfo(projectileInfo);
    }

    public CustomProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLiving, ProjectileInfo projectileInfo) {
        super(entityType, world, entityLiving);
        if (projectileInfo != null)
            this.shootFromRotation(entityLiving, entityLiving.xRotO, entityLiving.yRotO, 0.0F, (float) projectileInfo.getVelocity(), 1.0F); // Shoot from Entity
        this.modInfo = LycanitesMobs.modInfo;
        this.setProjectileInfo(projectileInfo);
    }

    public CustomProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z, ProjectileInfo projectileInfo) {
        super(entityType, world, x, y, z);
        this.modInfo = LycanitesMobs.modInfo;
        this.setProjectileInfo(projectileInfo);
    }

    @Override
    public EntityType getType() {
        if (this.projectileInfo == null) {
            return super.getType();
        }
        return this.projectileInfo.getEntityType();
    }

    @Override
    public void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PROJECTILE_NAME, "");
        builder.define(THROWING_ENTITY_ID, this.throwerId);
        builder.define(PARENT_PROJECTILE_ID, this.parentId);
        builder.define(TARGET_ID, this.targetId);
        builder.define(LASER_ANGLE, this.laserAngle);
    }


    // ==================================================
    //                Projectile Custom
    // ==================================================

    /**
     * Loads the projectile info for this projectile to use.
     *
     * @param projectileName The name of the projectile info to use.
     */
    public void loadProjectileInfo(String projectileName) {
        this.setProjectileInfo(ProjectileManager.getInstance().getProjectile(projectileName));
    }

    /**
     * Sets the projectile info for this projectile to use.
     *
     * @param projectileInfo The projectile info to use.
     */
    public void setProjectileInfo(ProjectileInfo projectileInfo) {
        this.projectileInfo = projectileInfo;
        if (this.projectileInfo == null) {
            return;
        }
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(PROJECTILE_NAME, this.projectileInfo.getName());
        }
        this.modInfo = this.projectileInfo.getModInfo();
        this.entityName = this.projectileInfo.getName();

        // Stats:
        this.setProjectileScale(this.projectileInfo.getScale());
        this.projectileLife = this.projectileInfo.getLifetime();
        this.setDamage(this.projectileInfo.getDamage());
        this.setPierce(this.projectileInfo.getPierce());
        this.knockbackChance = this.projectileInfo.getKnockbackChance();
        this.weight = this.projectileInfo.getWeight();

        // Visual:
        this.rollSpeed = this.projectileInfo.getRollSpeed();
        if (this.rollSpeed > 0 && this.random.nextBoolean()) {
            this.rollSpeed = -this.rollSpeed;
        }
        this.animationFrameMax = this.projectileInfo.getAnimationFrames();

        // Flags:
        this.waterProof = this.projectileInfo.isWaterproof();
        this.lavaProof = this.projectileInfo.isLavaproof();
        this.cutsGrass = this.projectileInfo.cutsGrass();
        this.ripper = this.projectileInfo.isRipper();
        this.pierceBlocks = this.projectileInfo.piercesBlocks();
    }

    public ProjectileInfo getProjectileInfo() {
        return this.projectileInfo;
    }

    public boolean hasProjectileInfo() {
        return this.projectileInfo != null;
    }

    public float getLaserWidth() {
        return this.laserWidth;
    }

    public void setLaserWidth(float laserWidth) {
        this.laserWidth = laserWidth;
    }

    public float getLaserAngle() {
        return this.laserAngle;
    }

    public void setLaserAngle(float laserAngle) {
        this.laserAngle = laserAngle;
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(LASER_ANGLE, this.laserAngle);
        }
    }

    public int getSpawnedProjectileCount() {
        return this.spawnedProjectiles.size();
    }

    public BaseProjectileEntity getSpawnedProjectile(int index) {
        return this.spawnedProjectiles.get(index);
    }

    public void addSpawnedProjectile(BaseProjectileEntity projectile) {
        this.spawnedProjectiles.add(projectile);
    }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        if (this.getCommandSenderWorld().isClientSide) {
            if (this.projectileInfo == null) {
                this.loadProjectileInfo(this.getStringFromDataManager(PROJECTILE_NAME));
            }
        }

        super.tick();

        if (this.projectileInfo != null) {
            this.projectileInfo.onProjectileUpdate(this);

            // Particles:
            if (this.getCommandSenderWorld().isClientSide && this.projectileInfo.getParticleCount() > 0) {
                for (int i = 0; i < this.projectileInfo.getParticleCount(); ++i) {
                    if (this.isInWater()) {
                        if (this.projectileInfo.getWaterParticleType() != null) {
                            this.getCommandSenderWorld().addParticle(this.projectileInfo.getWaterParticleType().getType(), this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), 0.0D, 0.0D, 0.0D);
                        } else if ("minecraft:dust_redstone".equalsIgnoreCase(this.projectileInfo.getWaterParticleId())) {
                            this.getCommandSenderWorld().addParticle(DustParticleOptions.REDSTONE, this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), 0.0D, 0.0D, 0.0D);
                        } else if ("minecraft:dust_falling_dirt".equalsIgnoreCase(this.projectileInfo.getWaterParticleId())) {
                            this.getCommandSenderWorld().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), 0.0D, 0.0D, 0.0D);
                        }
                    } else {
                        if (this.projectileInfo.getParticleType() != null) {
                            this.getCommandSenderWorld().addParticle(this.projectileInfo.getParticleType().getType(), this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), 0.0D, 0.0D, 0.0D);
                        } else if ("minecraft:dust_redstone".equalsIgnoreCase(this.projectileInfo.getParticleId())) {
                            this.getCommandSenderWorld().addParticle(DustParticleOptions.REDSTONE, this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), 0.0D, 0.0D, 0.0D);
                        } else if ("minecraft:dust_falling_dirt".equalsIgnoreCase(this.projectileInfo.getParticleId())) {
                            this.getCommandSenderWorld().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), this.getX() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), this.getY() + this.random.nextDouble() * (double) this.getBbHeight(), this.getZ() + (this.random.nextDouble() - 0.5D) * (double) this.getBbWidth(), 0.0D, 0.0D, 0.0D);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canDamage(LivingEntity targetEntity) {
        boolean canDamage = super.canDamage(targetEntity);
        if (this.projectileInfo != null) {
            canDamage = this.projectileInfo.canDamage(this, this.getCommandSenderWorld(), targetEntity, canDamage);
        }
        return canDamage;
    }

    /**
     * Syncs the Throwing Entity from server to client.
     */
    public void syncThrower() {
        if (!this.getCommandSenderWorld().isClientSide) {
            this.throwerId = this.getOwner() != null ? this.getOwner().getId() : -1;
            this.entityData.set(THROWING_ENTITY_ID, this.throwerId);
        } else {
            this.throwerId = this.entityData.get(THROWING_ENTITY_ID);
            if (this.throwerId == -1) {
                this.owner = null;
            } else if (this.getOwner() == null || this.getOwner().getId() != this.throwerId) {
                Entity possibleThrower = this.getCommandSenderWorld().getEntity(this.throwerId);
                if (possibleThrower instanceof LivingEntity) {
                    this.owner = (LivingEntity) possibleThrower;
                } else {
                    this.owner = null;
                }
            }
        }
    }

    /**
     * Returns true if this projectile should be channels where it's lifetime is reset constantly.
     *
     * @return True if this projectile can be channeled.
     */
    public boolean shouldChannel() {
        if (this.projectileInfo != null) {
            return this.projectileInfo.hasBehaviour(ProjectileBehaviourLaser.class);
        }
        return false;
    }


    // ==================================================
    //                      Projectile
    // ==================================================
    @Override
    public void onDamage(LivingEntity target, float damage, boolean attackSuccess) {
        super.onDamage(target, damage, attackSuccess);
        if (!this.getCommandSenderWorld().isClientSide && attackSuccess && this.projectileInfo != null) {
            if (this.projectileInfo.getDebuffsOverride() != null) {
                int duration = this.projectileInfo.getEffectDuration() * 20;
                int amplifier = this.projectileInfo.getEffectAmplifier();
                for (String debuff : this.projectileInfo.getDebuffsOverride()) {
                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(debuff));
                    if (effect != null) {
                        target.addEffect(new MobEffectInstance(ObjectManager.holder(effect), duration, amplifier));
                    }
                }
            } else {
                for (ElementInfo element : this.projectileInfo.getElements()) {
                    element.debuffEntity(target, this.projectileInfo.getEffectDuration() * 20, this.projectileInfo.getEffectAmplifier());
                }
            }
        }

        if (attackSuccess && this.projectileInfo != null) {
            this.projectileInfo.onProjectileDamage(this, this.getCommandSenderWorld(), target, damage);
        }
    }

    @Override
    public void onImpactComplete(BlockPos impactPos) {
        super.onImpactComplete(impactPos);
        if (this.projectileInfo == null) {
            return;
        }

        this.projectileInfo.onProjectileImpact(this, this.getCommandSenderWorld(), impactPos);
    }

    /**
     * Sets the projectile that fired this projectile.
     */
    public void setParent(BaseProjectileEntity parent) {
        this.parent = parent;
        if (!this.getCommandSenderWorld().isClientSide) {
            this.parentId = this.parent != null ? this.parent.getId() : -1;
            this.entityData.set(PARENT_PROJECTILE_ID, this.parentId);
        }
    }

    /**
     * Gets the projectile that fired this projectile
     *
     * @return The projectile that fired this projectile or null.
     */
    public BaseProjectileEntity getParent() {
        if (this.getCommandSenderWorld().isClientSide) {
            this.parentId = this.entityData.get(PARENT_PROJECTILE_ID);
            if (this.parentId == -1) {
                this.parent = null;
            } else if (this.parent == null || this.parent.getId() != this.parentId) {
                Entity possibleParent = this.getCommandSenderWorld().getEntity(this.parentId);
                if (possibleParent instanceof BaseProjectileEntity) {
                    this.parent = (BaseProjectileEntity) possibleParent;
                }
            }
        }
        return this.parent;
    }

    /**
     * Sets the target of this projectile, used by some behaviours.
     */
    public void setTarget(Entity target) {
        this.target = target;
        if (!this.getCommandSenderWorld().isClientSide) {
            this.targetId = this.target != null ? this.target.getId() : -1;
            this.entityData.set(TARGET_ID, this.targetId);
        }
    }

    /**
     * Gets the target of this projectile, used by some behaviours.
     *
     * @return The projectile target entity if any.
     */
    public Entity getTarget() {
        if (this.getCommandSenderWorld().isClientSide) {
            this.targetId = this.entityData.get(TARGET_ID);
            if (this.targetId == -1) {
                this.target = null;
            } else if (this.target == null || this.target.getId() != this.targetId) {
                this.target = this.getCommandSenderWorld().getEntity(this.targetId);
            }
        }
        return this.target;
    }

    /**
     * Sets the laser end used by this projectile or clears it if null. Also updates the laser angle.
     */
    public void setLaserEnd(Vec3 laserEnd) {
        this.laserEnd = laserEnd;
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(LASER_ANGLE, this.laserAngle);
        }
    }

    /**
     * Gets the laser end used by this projectile if any. Also updates the laser angle.
     *
     * @return The laser end for laser projectile behaviours.
     */
    public Vec3 getLaserEnd() {
        if (this.getCommandSenderWorld().isClientSide) {
            this.laserAngle = this.entityData.get(LASER_ANGLE);
        }
        return this.laserEnd;
    }

    @Override
    public boolean isOnFire() {
        return this.projectileInfo != null && this.projectileInfo.hasBurningEffect();
    }


    // ==================================================
    //                       NBT
    // ==================================================
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        if (this.projectileInfo != null) {
            compound.putString("ProjectileName", this.projectileInfo.getName());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("ProjectileName")) {
            this.loadProjectileInfo(compound.getString("ProjectileName"));
        }
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public String getTextureName() {
        return this.entityName;
    }

    @Override
    public float getBrightness() {
        if (this.projectileInfo == null || !this.projectileInfo.glows())
            return LMHelperClass.getBrightness(this);
        return 1.0F;
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    public SoundEvent getLaunchSound() {
        if (this.projectileInfo != null) {
            return this.projectileInfo.getLaunchSound();
        }
        return super.getLaunchSound();
    }

    public SoundEvent getImpactSound() {
        if (this.projectileInfo != null) {
            return this.projectileInfo.getImpactSound();
        }
        return super.getImpactSound();
    }
}
