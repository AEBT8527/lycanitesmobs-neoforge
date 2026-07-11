package com.lycanitesmobs.core.entity.spawner;


import net.minecraft.core.BlockPos;

import java.util.Comparator;

public class CoordSorterFurthest implements Comparator<BlockPos> {
    protected final BlockPos coord;

    // ==================================================
  	//                    Constructor
  	// ==================================================
    public CoordSorterFurthest(BlockPos coord) {
        this.coord = coord;
    }
    
    
    // ==================================================
  	//                     Compare
  	// ==================================================
	@Override
	public int compare(BlockPos targetA, BlockPos targetB) {
		double distanceA = this.getDistanceSqCoord(targetA);
        double distanceB = this.getDistanceSqCoord(targetB);
        return distanceA > distanceB ? -1 : (distanceA < distanceB ? 1 : 0);
	}
	
	
    // ==================================================
  	//                  Get Distance
  	// ==================================================
    public double getDistanceSqCoord(BlockPos targetCoord) {
        return this.coord.distSqr(targetCoord);
    }
}
