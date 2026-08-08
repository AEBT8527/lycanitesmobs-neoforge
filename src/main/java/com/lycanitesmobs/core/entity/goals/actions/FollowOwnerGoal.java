package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public class FollowOwnerGoal extends FollowGoal {
    private static final int FOLLOW_TELEPORT_RADIUS = 2;
    private static final int[] AIRBORNE_TELEPORT_Y_OFFSETS = {1, 0, 2, -1, 3};
    private static final int[] GROUNDED_TELEPORT_Y_OFFSETS = {0, 1, -1};

	// Targets:
	TameableCreatureEntity host;
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public FollowOwnerGoal(TameableCreatureEntity setHost) {
    	super(setHost);
        this.host = setHost;
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public FollowOwnerGoal setSpeed(double setSpeed) {
    	this.speed = setSpeed;
    	return this;
    }
    public FollowOwnerGoal setTargetClass(Class setTargetClass) {
    	this.targetClass = setTargetClass;
    	return this;
    }
    public FollowOwnerGoal setStrayDistance(double setDist) {
    	this.strayDistance = setDist;
    	return this;
    }
    public FollowOwnerGoal setLostDistance(double setDist) {
    	this.lostDistance = setDist;
    	return this;
    }
    
	
	// ==================================================
 	//                    Get Target
 	// ==================================================
    @Override
    public Entity getTarget() {
    	return this.host.getOwner();
    }

	@Override
	public void setTarget(Entity entity) {
    	// Do nothing here.
	}


	// ==================================================
	//                  Should Execute
	// ==================================================
	@Override
	public boolean canUse() {
		Entity target = this.getTarget();
		if(target == null)
			return false;
		if(!target.isAlive())
			return false;
		if(this.host.isSitting() || !this.host.isFollowing())
			return false;

		// Start straying when within the stray radius and the target.
		double distance = this.host.distanceTo(target);
		if(distance <= this.strayDistance && this.strayDistance != 0) {
			this.host.clearMovement();
			return false;
		}

		return true;
	}


	// ==================================================
	//                Continue Executing
	// ==================================================
	@Override
	public boolean canContinueToUse() {
    	return this.canUse();
	}
    
	
	// ==================================================
 	//                      Update
 	// ==================================================
	@Override
    public void tick() {
		if(this.host.distanceTo(this.getTarget()) >= this.lostDistance) {
			this.teleportToOwner();
		}
    	super.tick();
    }
    
    // ========== Teleport to Owner ==========
    public void teleportToOwner() {
    	if(this.getTarget() != null) {
			if(!this.host.canBreatheAir() && ((!this.host.isLavaCreature() && !this.getTarget().isInWater()) || (this.host.isLavaCreature() && !this.getTarget().isInLava()))) {
				return;
			}
			if(!this.host.canBreatheUnderwaterCreature() && this.getTarget().isInWater()) {
				return;
			}

            this.tryTeleportNearTarget(this.getTarget(), this.host.isFlying() || this.getTarget().isInWater());
    	}
    }

	protected boolean canTeleportTo(BlockPos blockPos) {
		BlockState blockState = this.host.level().getBlockState(blockPos.below());
		return blockState.isValidSpawn(this.host.level(), blockPos.below(), this.host.getType()) && this.host.level().isEmptyBlock(blockPos) && this.host.level().isEmptyBlock(blockPos.above());
	}
    
    //TODO Wait on the ChunkUnload Chunk event, if this mob is not sitting and the unloading chunk is what it's in, then teleport this mob to it's owner away from the unloaded chunk, unless it's player has disconnected.

    /**
     * Searches outward from the target for a spot the host can actually occupy, and moves it to
     * the SAME position it validated. Airborne hosts prefer to arrive slightly above the owner.
     */
    private boolean tryTeleportNearTarget(Entity target, boolean airborne) {
        BlockPos targetPos = target.blockPosition();
        int targetY = Mth.floor(target.getBoundingBox().minY);
        int[] yOffsets = airborne ? AIRBORNE_TELEPORT_Y_OFFSETS : GROUNDED_TELEPORT_Y_OFFSETS;

        for (int radius = 1; radius <= FOLLOW_TELEPORT_RADIUS; radius++) {
            for (int yOffset : yOffsets) {
                for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                    for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                        // ring only - the inner positions were covered by a smaller radius
                        if (Math.max(Math.abs(xOffset), Math.abs(zOffset)) != radius) {
                            continue;
                        }
                        BlockPos blockPos = targetPos.offset(xOffset, yOffset, zOffset);
                        if (airborne) {
                            double x = blockPos.getX() + 0.5D;
                            double y = targetY + yOffset;
                            double z = blockPos.getZ() + 0.5D;
                            if (this.canTeleportToPosition(x, y, z)) {
                                this.moveHostTo(x, y, z);
                                return true;
                            }
                        }
                        else if (this.canTeleportTo(blockPos)) {
                            this.moveHostTo(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Airborne hosts only need clear space, not a valid spawn surface below. */
    private boolean canTeleportToPosition(double x, double y, double z) {
        BlockPos blockPos = BlockPos.containing(x, y, z);
        return this.host.level().isEmptyBlock(blockPos) && this.host.level().isEmptyBlock(blockPos.above());
    }

    private void moveHostTo(double x, double y, double z) {
        this.host.snapTo(x, y, z, this.host.yRotO, this.host.xRotO);
        this.host.clearMovement();
    }
}
