package com.lycanitesmobs.core.entity.projectile.generic;

import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RapidFireProjectileEntity extends BaseProjectileEntity {
    // Properties:
    protected LivingEntity shootingEntity;
    private float projectileWidth = 0.2f;
    private float projectileHeight = 0.2f;

    // Rapid Fire:
    private Class projectileClass;
    private ProjectileInfo projectileInfo;
    private int rapidTime = 100;
    private int rapidDelay = 5;

    // Offsets:
    protected double offsetX = 0;
    protected double offsetY = 0;
    protected double offsetZ = 0;

    // ==================================================
    //                   Constructors
    // ==================================================
    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
        this.noPhysics = true;
    }

    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Class entityClass, Level world, int setTime, int setDelay) {
        super(entityType, world);
        this.projectileClass = entityClass;
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Class entityClass, Level world, double x, double y, double z, int setTime, int setDelay) {
        super(entityType, world, x, y, z);
        this.projectileClass = entityClass;
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Class entityClass, Level world, LivingEntity entityLivingBase, int setTime, int setDelay) {
        super(entityType, world, entityLivingBase);
        //this.setSize(projectileWidth, projectileHeight);
        this.projectileClass = entityClass;
        this.shootingEntity = entityLivingBase;
        this.offsetX = this.position().x() - entityLivingBase.position().x();
        this.offsetY = this.position().y() - entityLivingBase.position().y();
        this.offsetZ = this.position().z() - entityLivingBase.position().z();
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, ProjectileInfo projectileInfo, Level world, double par2, double par4, double par6, int setTime, int setDelay) {
        super(entityType, world, par2, par4, par6);
        //this.setSize(projectileWidth, projectileHeight);
        this.projectileInfo = projectileInfo;
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

    public RapidFireProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, ProjectileInfo projectileInfo, Level world, LivingEntity entityLivingBase, int setTime, int setDelay) {
        super(entityType, world, entityLivingBase);
        //this.setSize(projectileWidth, projectileHeight);
        this.projectileInfo = projectileInfo;
        this.shootingEntity = entityLivingBase;
        this.offsetX = this.position().x() - entityLivingBase.position().x();
        this.offsetY = this.position().y() - entityLivingBase.position().y();
        this.offsetZ = this.position().z() - entityLivingBase.position().z();
        this.rapidTime = setTime;
        this.rapidDelay = setDelay;
        this.noPhysics = true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity serverEntity) {
        return super.getAddEntityPacket(serverEntity);
    }
/*@Override
	public IPacket<?> getAddEntityPacket() {
		return new SSpawnObjectPacket(this);
	}*/


    // ==================================================
    //                   Update
    // ==================================================
    @Override
    public void tick() {
        if (this.shootingEntity != null) {
            this.setPos(
                    shootingEntity.position().x() + this.offsetX,
                    shootingEntity.position().y() + this.offsetY,
                    shootingEntity.position().z() + this.offsetZ
            );
        }
        if (rapidTime > 0) {
            if (projectileClass == null && this.projectileInfo == null) {
                rapidTime = 0;
                return;
            }

            if (rapidTime % rapidDelay == 0)
                fireProjectile();

            rapidTime--;
        } else if (this.isAlive()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }


    // ==================================================
    //                    Add Time
    // ==================================================
    public void addTime(int addTime) {
        this.rapidTime += addTime;
    }

    public void addOffset(double x, double y, double z) {
        this.offsetX += x;
        this.offsetY += y;
        this.offsetZ += z;
    }


    // ==================================================
    //                 Fire Projectile
    // ==================================================
    public void fireProjectile() {
        Level world = this.level();
        if (world.isClientSide())
            return;

        try {
            Projectile projectile;

            if (this.shootingEntity == null) {
                Vec3 shotDirection = this.getShotDirection();
                if (this.projectileInfo != null) {
                    projectile = this.projectileInfo.createProjectile(this.level(), this.position().x(), this.position().y(), this.position().z());
                    projectile.shoot(shotDirection.x, shotDirection.y, shotDirection.z, (float) this.projectileInfo.getVelocity(), 0);
                } else {
                    projectile = ProjectileManager.getInstance().createOldProjectile(this.projectileClass, world, this.position().x(), this.position().y(), this.position().z());
                    projectile.shoot(shotDirection.x, shotDirection.y, shotDirection.z, 1, 1);
                }
            } else {
                Vec3 shotDirection = this.getShotDirection();
                if (this.projectileInfo != null) {
                    projectile = this.projectileInfo.createProjectile(this.level(), this.shootingEntity);
                    projectile.shoot(shotDirection.x, shotDirection.y, shotDirection.z, (float) this.projectileInfo.getVelocity(), 0);
                } else {
                    projectile = ProjectileManager.getInstance().createOldProjectile(this.projectileClass, world, this.shootingEntity);
                    projectile.shoot(shotDirection.x, shotDirection.y, shotDirection.z, 1, 1);
                }
                if (projectile instanceof ThrowableProjectile) {
                    ThrowableProjectile entityThrowable = (ThrowableProjectile) projectile;
                    entityThrowable.setPos(this.shootingEntity.position().x() + this.offsetX, this.shootingEntity.position().y() + this.offsetY, this.shootingEntity.position().z() + this.offsetZ);
                }
            }

            if (projectile instanceof BaseProjectileEntity) {
                ((BaseProjectileEntity) projectile).setProjectileScale(this.projectileScale);
            }

            DeferredLevelActionManager.spawnEntity(world, this.blockPosition(), null, (net.minecraft.world.entity.Entity) projectile);
        } catch (Exception e) {
            System.out.println("[WARNING] [LycanitesMobs] EntityRapidFire was unable to instantiate the given projectile class.");
            e.printStackTrace();
        }
    }

    private Vec3 getShotDirection() {
        Vec3 direction = this.getDeltaMovement();
        if (direction.lengthSqr() > 0.0001D) {
            return direction;
        }
        if (this.shootingEntity != null) {
            direction = this.shootingEntity.getLookAngle();
            if (direction.lengthSqr() > 0.0001D) {
                return direction;
            }
        }
        return new Vec3(0, 0, 1);
    }


    // ==================================================
    //                   Movement
    // ==================================================
    // ========== Gravity ==========
    @Override
    protected double getDefaultGravity() {
        return 0.0F;
    }

    // ========== Set Position ==========
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (this.shootingEntity != null) {
            this.offsetX = x - this.shootingEntity.position().x();
            this.offsetY = y - this.shootingEntity.position().y();
            this.offsetZ = z - this.shootingEntity.position().z();
        }
    }


    // ==================================================
    //                     Impact
    // ==================================================
    @Override
    protected void onHit(HitResult rayTraceResult) {
        return;
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public Identifier getTexture() {
        return null;
    }
}
