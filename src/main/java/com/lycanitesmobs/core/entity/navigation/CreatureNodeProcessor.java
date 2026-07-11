package com.lycanitesmobs.core.entity.navigation;

import net.minecraft.world.level.pathfinder.PathfindingContext;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class CreatureNodeProcessor extends WalkNodeEvaluator implements ICreatureNodeProcessor {

    private BaseCreatureEntity entityCreature;

    public static double getGroundY(BlockGetter blockReader, BlockPos pos) {
        BlockPos blockpos = pos.below();
        VoxelShape voxelshape = blockReader.getBlockState(blockpos).getCollisionShape(blockReader, blockpos);
        return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
    }

    @Override
    public void prepare(PathNavigationRegion region, Mob mob) {
        this.updateEntitySize(mob);
        super.prepare(region, mob);
        if(mob instanceof BaseCreatureEntity) {
            this.entityCreature = (BaseCreatureEntity) mob;
        }
    }

    @Override
    public void updateEntitySize(Entity updateEntity) {
        this.entityWidth = Math.min(Mth.floor(this.getWidth(true, updateEntity) + 1.0F), 3);
        this.entityHeight = Math.min(Mth.floor(updateEntity.getDimensions(Pose.STANDING).height() + 1.0F), 3);
        this.entityDepth = Math.min(Mth.floor(this.getWidth(true, updateEntity) + 1.0F), 3);
    }

    /** Returns the starting position to create a new path from. **/
    @Override
    public Node getStart() {

        // Flying/Strong Swimming:
        if(this.flying() || (this.entityCreature.isStrongSwimmer() && this.entityCreature.isUnderWater())) {
            return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ));
        }

        return super.getStart();
    }

    /** Returns true if the entity is capable of pathing/moving in water at all. **/
    @Override
    public boolean canFloat() {
        if(this.entityCreature != null)
            return this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer();
        return super.canFloat();
    }

    /** Returns a PathPoint to the given coordinates. **/
    @Override
    public Target getTarget(double x, double y, double z) {
        // Flying/Strong Swimming:
        if(this.flying() || this.swimming()) {
            return new Target(this.getNode(Mth.floor(x - this.getWidth(false)), Mth.floor(y + 0.5D), Mth.floor(z - this.getWidth(false))));
        }
        return super.getTarget(x, y, z);
    }

    /** Checks points around the provided fromPoint and adds it to path options if it is a valid point to travel to. **/
    @Override
    public int getNeighbors(Node[] pathOptions, Node fromPoint) {
        this.updateEntitySize(mob);

        // Flying/Strong Swimming/Diving:
        if(this.flying() || this.swimming()) {
            int i = 0;
            for (Direction direction : Direction.values()) {
                Node pathPoint = null;
                if(this.swimming()) {
                    pathPoint = this.getWaterNode(fromPoint.x + direction.getStepX(), fromPoint.y + direction.getStepY(), fromPoint.z + direction.getStepZ());
                }
                if(pathPoint == null) {
                    pathPoint = this.getFlightNode(fromPoint.x + direction.getStepX(), fromPoint.y + direction.getStepY(), fromPoint.z + direction.getStepZ());
                }
                if(pathPoint != null && !pathPoint.closed) {
                    pathOptions[i++] = pathPoint;
                }
            }
            return i;
        }

        return super.getNeighbors(pathOptions, fromPoint);
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext pathContext, int x, int y, int z, Mob entity) {
        if(this.swimming()) {
            return PathType.WATER;
        }
        return super.getPathTypeOfMob(pathContext, x, y, z, entity);
    }
    /** Returns true if the entity should use swimming focused pathing. **/
    public boolean swimming() {
        if(this.entityCreature == null) {
            return false;
        }
        if(this.entityCreature.isInWater()) {
            return this.entityCreature.isStrongSwimmer() || (this.entityCreature.canWade() && this.entityCreature.shouldDive());
        }
        return false;
    }

    /** Returns true if the entity should use flight focused pathing. **/
    public boolean flying() {
        return this.entityCreature != null && this.entityCreature.isFlying() && !this.entityCreature.isUnderWater();
    }

    /**
     * Returns a width to path with.
     * @param blockChecks If true, this width is used for checking blocks, a reduced width can be returned for better performance here.
     * @return The entity width to path with.
     */
    public double getWidth(boolean blockChecks) {
		return this.getWidth(blockChecks, this.mob);
    }

    /**
     * Returns a width to path with.
     * @param blockChecks If true, this width is used for checking blocks, a reduced width can be returned for better performance here.
     * @param entity The entity to get the width of.
     * @return The entity width to path with.
     */
    public double getWidth(boolean blockChecks, Entity entity) {
		return Math.min(3, (double)entity.getDimensions(Pose.STANDING).width());
	}

    /** Flight Pathing **/
    protected Node getFlightNode(int x, int y, int z) {
        PathType pathnodetype = this.isFlyablePathNode(x, y, z);
        if(this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
            if(pathnodetype == PathType.WATER)
                return this.getNode(x, y, z);
        }
        return pathnodetype == PathType.OPEN ? this.getNode(x, y, z) : null;
    }

    protected PathType isFlyablePathNode(int x, int y, int z) {
        BlockPos centerPos = new BlockPos(x, y, z);
        for (int i = 0; i <= this.entityWidth; ++i) {
            for (int j = 0; j <= Math.min(this.entityHeight, 2); ++j) {
                for (int k = 0; k <= this.entityDepth; ++k) {
                    BlockState iblockstate = this.currentContext.getBlockState(centerPos.offset(i, k, j));

                    // Non-Solid:
					if (!iblockstate.isSolid() && !(iblockstate.getBlock() instanceof LiquidBlock)) {
						return PathType.OPEN;
					}

                    // Check For Open Air:
                    if (!iblockstate.isAir()) {
                        // If Can Swim Check For Swimmable Node:
                        if(this.entityCreature != null && this.entityCreature.isStrongSwimmer()) {
                            return this.isSwimmablePathNode(x, y, z);
                        }
                        return PathType.BLOCKED;
                    }
                }
            }
        }

        return PathType.OPEN;
    }

    /** Power Swim Pathing **/
    @Nullable
    protected Node getWaterNode(int x, int y, int z) {
        PathType pathnodetype;
        if(this.entityCreature != null && this.entityCreature.isFlying()) {
            pathnodetype = this.isFlyablePathNode(x, y, z);
            if(pathnodetype == PathType.OPEN)
                return this.getNode(x, y, z);
        }
        else {
            pathnodetype = this.isSwimmablePathNode(x, y, z);
        }
        return pathnodetype == PathType.WATER ? this.getNode(x, y, z) : null;
    }

    protected PathType isSwimmablePathNode(int x, int y, int z) {
        BlockPos centerPos = new BlockPos(x, y, z);
        for (int i = 0; i <= this.entityWidth; ++i) {
            for (int j = 0; j <= Math.min(this.entityHeight, 2); ++j) {
                for (int k = 0; k <= this.entityDepth; ++k) {
                    BlockPos blockPos = centerPos.offset(i, k, j);

                    // Block State Checks:
                    BlockState blockState = this.currentContext.getBlockState(blockPos);

                    if(this.entityCreature == null || !blockState.isPathfindable(PathComputationType.WATER)) {
                        if(j == y) { // Y must be water.
                            return PathType.BLOCKED;
                        }
                        if(!blockState.isPathfindable(PathComputationType.AIR) && !blockState.isPathfindable(PathComputationType.WATER)) { // Blocked above water.
                            return PathType.BLOCKED;
                        }
                    }

                    // Water Damages:
                    if (this.entityCreature.waterDamage() && blockState.getBlock() == Blocks.WATER) {
                        return PathType.BLOCKED;
                    }

                    // Lava Damages:
                    if (this.entityCreature.canBurn() && blockState.getBlock() == Blocks.LAVA) {
                        return PathType.BLOCKED;
                    }

                    // Ooze Swimming (With Water Damage):
                    if(!this.entityCreature.canFreeze() && ObjectManager.getBlock("ooze") != null && blockState.getBlock() == ObjectManager.getBlock("ooze")) {
                        return PathType.WATER;
                    }

                    // Custom Fluid State Checks: - Added some direct checks to improve performance.
                    if (!blockState.getFluidState().isSource()) {
                        FluidState fluidState = this.currentContext.level().getFluidState(blockPos);

                        // Water Damages:
                        if (this.entityCreature.waterDamage() && fluidState.is(FluidTags.WATER)) {
                            return PathType.BLOCKED;
                        }

                        // Lava Damages:
                        if (this.entityCreature.canBurn() && fluidState.is(FluidTags.LAVA)) {
                            return PathType.BLOCKED;
                        }
                    }
                }
            }
        }
        return PathType.WATER;
    }
}
