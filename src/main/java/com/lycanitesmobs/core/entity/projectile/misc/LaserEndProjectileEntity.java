package com.lycanitesmobs.core.entity.projectile.misc;


import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.LaserProjectileEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class LaserEndProjectileEntity extends BaseProjectileEntity {
	// Laser End:
	private double targetX;
	private double targetY;
	private double targetZ;
	
	// Properties:
	protected LivingEntity shootingEntity;
	protected BaseProjectileEntity laserEntity;
	private double projectileSpeed;

    // Datawatcher:
    protected static final EntityDataAccessor<Float> POS_X = SynchedEntityData.defineId(LaserEndProjectileEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> POS_Y = SynchedEntityData.defineId(LaserEndProjectileEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> POS_Z = SynchedEntityData.defineId(LaserEndProjectileEntity.class, EntityDataSerializers.FLOAT);
	
    // ==================================================
 	//                   Constructors
 	// ==================================================
    public LaserEndProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
        this.setStats();
    }

    public LaserEndProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6, LaserProjectileEntity laser) {
        super(entityType, world, par2, par4, par6);
        this.laserEntity = laser;
        this.setStats();
    }
    
    public LaserEndProjectileEntity(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity shooter, LaserProjectileEntity laser) {
        super(entityType, world, shooter);
        this.shootingEntity = shooter;
        this.laserEntity = laser;
        this.setStats();
    }
    
    public void setStats() {
        this.setSpeed(1.0D);
        //this.setSize(projectileWidth, projectileHeight);
        if(laserEntity != null) {
	        this.targetX = this.laserEntity.position().x();
	        this.targetY = this.laserEntity.position().y();
	        this.targetZ = this.laserEntity.position().z();
        }
        this.entityData.set(POS_X, (float) this.position().x());
        this.entityData.set(POS_Y, (float) this.position().y());
        this.entityData.set(POS_Z, (float) this.position().z());
        this.noPhysics = true;
    }

    @Override
    public void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POS_X, 0.0F);
        builder.define(POS_Y, 0.0F);
        builder.define(POS_Z, 0.0F);
    }
    
    
    // ==================================================
 	//                     Updates
 	// ==================================================
    // ========== Main Update ==========
    @Override
    public void tick() {
    	if(this.level().isClientSide()) {
    		this.setPos(this.entityData.get(POS_X), this.entityData.get(POS_Y), this.entityData.get(POS_Z));
    		return;
    	}
    	
    	if((this.laserEntity == null || !this.laserEntity.isAlive()) && this.isAlive())
    		this.remove(RemovalReason.DISCARDED);
    	
    	if(this.isAlive())
    		this.moveToTarget();
    	
    	this.entityData.set(POS_X, (float) this.position().x());
    	this.entityData.set(POS_Y, (float) this.position().y());
    	this.entityData.set(POS_Z, (float) this.position().z());
    }
    
    // ========== End Update ==========
	public void onUpdateEnd(double newTargetX, double newTargetY, double newTargetZ) {
		if(this.level().isClientSide())
			return;
		
		this.targetX = newTargetX;
		this.targetY = newTargetY;
		this.targetZ = newTargetZ;
		
		if(this.getLaunchSound() != null)
			this.playSound(this.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
	}
	
    
    // ==================================================
 	//                   Movement
 	// ==================================================
    // ========== Gravity ==========
    @Override
    protected double getDefaultGravity() {
        return 0.0F;
    }
    
    // ========== Move to Target ==========
    public void moveToTarget() {
    	this.setPos(this.targetX, this.targetY, this.targetZ);
    	/*this.setPosition(
    			this.moveCoordToTarget(this.getPositionVec().getX(), this.targetX, this.laserEntity.getPositionVec().getX()),
    			this.moveCoordToTarget(this.getPositionVec().getY(), this.targetY, this.laserEntity.getPositionVec().getY()),
    			this.moveCoordToTarget(this.getPositionVec().getZ(), this.targetZ, this.laserEntity.getPositionVec().getZ())
		);*/
    }
    
    // ========== Move Coord ==========
    public double moveCoordToTarget(double coord, double targetCoord, double originCoord) {
    	double distance = targetCoord - coord;
    	double moveSpeed = this.projectileSpeed;
    	if(distance > 0) {
    		if(distance < moveSpeed + 1)
    			moveSpeed = distance;
    		if((targetCoord - originCoord) > (coord - originCoord))
    			return coord + moveSpeed;
    		else
    			return targetCoord;
    	}
    	else if(distance < 0) {
    		if(distance > -moveSpeed - 1)
    			moveSpeed = -distance;
    		if((targetCoord - originCoord) < (coord - originCoord))
    			return coord - moveSpeed;
    		else
    			return targetCoord;
    	}
    	return targetCoord;
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    @Override
    protected void onHit(HitResult rayTraceResult) {}
    
    
    // ==================================================
 	//                      Speed
 	// ==================================================
    public void setSpeed(double speed) {
    	this.projectileSpeed = speed;
    }
    
    
    // ==================================================
 	//                      Visuals
 	// ==================================================
    @Override
    public Identifier getTexture() {
    	return null;
    }
}
