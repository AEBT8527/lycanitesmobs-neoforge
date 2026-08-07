package com.lycanitesmobs.core.entity.creature.reptile;

import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityAspid extends AgeableCreatureEntity {
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityAspid(EntityType<? extends EntityAspid> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.attackCooldownMax = 10;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new TemptGoal(this).setIncludeDiet(true));
		this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        if(!this.getCommandSenderWorld().isClientSide && this.hasCustomName()) {
			// LMHelperClass.logDebug("Entity", "Distraction goals registered.");
		}
        
        // Trail:
        if(!this.getCommandSenderWorld().isClientSide && (this.tickCount % 10 == 0 || this.isMoving() && this.tickCount % 5 == 0)) {
        	int trailHeight = 2;
        	if(this.isBaby())
        		trailHeight = 1;
        	for(int y = 0; y < trailHeight; y++) {
        		BlockState trailState = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, y, 0));
        		if(trailState.is(LycanitesBlockTags.ASPID_POISON_CLOUD_REPLACEABLE))
        			this.getCommandSenderWorld().setBlockAndUpdate(this.blockPosition().offset(0, y, 0), ObjectManager.getBlock("poisoncloud").defaultBlockState());
        	}
		}
    }
	
	
	// ==================================================
   	//                      Movement
   	// ==================================================
	// ========== Pathing Weight ==========
	@Override
	public float getBlockPathWeight(int x, int y, int z) {
        if(this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z)).getBlock() != Blocks.AIR) {
            BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
            if(Material.GRASS.contains(blockState.getBlock()))
                return 10F;
            if(Material.DIRT.contains(blockState.getBlock()))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }
    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.getBagSize(); }
	// ========== Can leash ==========
    @Override
    public boolean canBeLeashed() {
	    if(!this.hasAttackTarget() && !this.hasMaster())
	        return true;
	    return super.canBeLeashed();
    }
}
