package com.lycanitesmobs.core.entity.special;

import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ParticleEntity extends ThrowableProjectile {
	// Particle:
	protected int particleAge = 0;
	protected int particleAgeMax = 20;
	protected double particleGravity = 0D;
	protected String texture;
	protected ModInfo group;
	
    // ==================================================
    //                      Constructor
    // ==================================================
	public ParticleEntity(Level world, double x, double y, double z, String texture, ModInfo group) {
		super(EntityType.SNOWBALL, world);
		this.setPos(x, y, z);
        this.xOld = x;
        this.yOld = y;
        this.zOld = z;
		this.texture = texture;
		this.group = group;
	}

	@Override
	public void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}
	
	
    // ==================================================
    //                       Update
    // ==================================================
	@Override
    public void tick() {
		System.out.println("Doing something!");
        this.xo = this.position().x();
        this.yo = this.position().y();
        this.zo = this.position().z();

        if(this.particleAge++ >= this.particleAgeMax)
            this.remove(RemovalReason.DISCARDED);

        /*this.motionY -= 0.04D * (double)this.particleGravity;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if(this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }*/
    }

	
    // ==================================================
    //                    Interaction
    // ==================================================
	public boolean canAttackWithItem() {
        return false;
    }
	
	protected boolean isMovementNoisy() {
        return false;
    }
	
	@Override
	protected void onHit(HitResult rayTraceResult) {
		return;
	}
	
	
    // ==================================================
    //                        NBT
    // ==================================================
	@Override
	public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {}

	@Override
    public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {}
	
	
    // ==================================================
    //                      Visuals
    // ==================================================
    public ResourceLocation getTexture() {
    	return AssetHelper.texture("textures/particles/" + this.texture.toLowerCase() + ".png");
    }
}
