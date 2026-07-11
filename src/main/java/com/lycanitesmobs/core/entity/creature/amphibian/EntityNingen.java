package com.lycanitesmobs.core.entity.creature.amphibian;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.WanderGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

public class EntityNingen extends TameableCreatureEntity implements Enemy {
	
	WanderGoal wanderAI;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityNingen(EntityType<? extends EntityNingen> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.hasAttackSound = true;

        this.babySpawnChance = 0.01D;
        this.canGrow = false;
        this.setupMob();
        this.setPathfindingMalus(PathType.WATER, 0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false).setRange(1));
    }

	
    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
	@Override
    public float getAISpeedModifier() {
    	if(this.isInWater()) // Checks specifically just for water.
    		return 2.0F;
    	else if(this.waterContact()) // Checks for water, rain, etc.
    		return 1.5F;
        return super.getAISpeedModifier();
    }
    
	// Pathing Weight:
	@Override
	public float getBlockPathWeight(int x, int y, int z) {
		int waterWeight = 10;
        BlockPos pos = new BlockPos(x, y, z);
        BlockState blockState = this.level().getBlockState(pos);
        if(blockState.getBlock() == Blocks.WATER)
        	return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);
        if(this.level().isRaining() && this.level().canSeeSkyFromBelowWater(pos))
        	return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);
        
        if(this.getTarget() != null)
        	return super.getBlockPathWeight(x, y, z);
        if(this.waterContact())
			return -999999.0F;
		
		return super.getBlockPathWeight(x, y, z);
    }
	
	// Pushed By Water:
	@Override
	public boolean isPushedByFluid() {
        return false;
    }


    // ========== Get Wander Position ==========
    public BlockPos getWanderPosition(BlockPos wanderPosition) {
        BlockPos groundPos;
        for(groundPos = wanderPosition.below(); groundPos.getY() > 0 && !this.level().getBlockState(groundPos).isSolid(); groundPos = groundPos.below()) {}
        return groundPos.above();
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Is Aggressive ==========
    @Override
    public boolean isAggressive() {
    	if(this.getAirSupply() <= -100)
    		return false;
    	return super.isAggressive();
    }

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.getBagSize(); }
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean canBreatheUnderwaterCreature() {
        return true;
    }
    
    @Override
    public boolean canBreatheAir() {
        return false;
    }
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }
}
