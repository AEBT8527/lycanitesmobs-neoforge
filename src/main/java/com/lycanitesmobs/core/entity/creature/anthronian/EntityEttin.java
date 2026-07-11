package com.lycanitesmobs.core.entity.creature.anthronian;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;

public class EntityEttin extends TameableCreatureEntity implements Enemy {
	protected boolean griefing = true;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityEttin(EntityType<? extends EntityEttin> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        
        this.solidCollision = true;
        this.setupMob();
        
        // Stats:
        this.attackPhaseMax = 2;
    }

    @Override
    protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false));

		if(this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation)this.getNavigation();
			pathNavigateGround.setCanOpenDoors(true);
		}
    }

	@Override
	public void loadCreatureFlags() {
		this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
	}

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 10; }
    @Override
    public int getBagSize() { return this.creatureInfo.getBagSize(); }
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
    	// Destroy Blocks:
		if(!this.level().isClientSide())
	        if(this.getTarget() != null && LMHelperClass.getGameRuleBool(this.level(), GameRules.MOB_GRIEFING, true) && this.griefing) {
		    	float distance = this.getTarget().distanceTo(this);
		    		if(distance <= this.getDimensions(Pose.STANDING).width() + 4.0F)
		    			this.destroyArea((int)this.position().x(), (int)this.position().y(), (int)this.position().z(), 0.5F, true);
	        }
        
        super.aiStep();
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	boolean success = super.attackMelee(target, damageScale);
    	if(success)
    		this.nextAttackPhase();
    	return success;
    }
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }
}

