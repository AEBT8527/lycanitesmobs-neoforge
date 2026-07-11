package com.lycanitesmobs.core.entity.creature.avian;

import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
public class EntityUvaraptor extends RideableCreatureEntity {
    private static final int MOUNT_ABILITY_BOOST_TICKS = 10;

    protected int mountAbilityBoostTicks = 0;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityUvaraptor(EntityType<? extends EntityUvaraptor> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsUnderground = false;
        this.hasAttackSound = true;
        this.hasJumpSound = true;
        this.spreadFire = false;

        this.canGrow = true;
        this.babySpawnChance = 0.01D;
        this.setupMob();
        
        // Stats:
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        if (this.mountAbilityBoostTicks > 0) {
            this.mountAbilityBoostTicks--;
        }
        
        // Random Leaping:
        if(!this.isTamed() && this.onGround() && !this.level().isClientSide()) {
        	if(this.hasAttackTarget()) {
        		if(this.getRandom().nextInt(10) == 0)
        			this.leap(6.0F, 1.0D, this.getTarget());
        	}
        	else {
        		if(this.getRandom().nextInt(50) == 0 && this.isMoving())
        			this.leap(1.0D, 1.0D);
    		}
        }
    }

	
    // ==================================================
    //                      Movement
    // ==================================================
	// ========== Movement Speed Modifier ==========
	@Override
	public float getAISpeedModifier() {
		if(!this.onGround() && !this.hasRiderTarget())
			return 5.0F;
		if(!this.onGround())
			return this.mountAbilityBoostTicks > 0 ? 5.0F : 2.0F;
		return 1.0F;
	}

	// ========== Falling Speed Modifier ==========
    @Override
    public double getFallingMod() {
    	return 0.9D;
    }
    
        public double getPassengersRidingOffset() {
        return (double)this.getDimensions(Pose.STANDING).height() * 0.9D;
    }
    
    
    // ==================================================
    //                   Mount Ability
    // ==================================================
    @Override
    public void mountAbility(Entity rider) {
        if(this.level().isClientSide())
            return;
    	if(this.abilityToggled)
    		return;
    	if(this.getStamina() < this.getStaminaCost())
    		return;
    	
    	this.playJumpSound();
        this.mountAbilityBoostTicks = MOUNT_ABILITY_BOOST_TICKS;
        this.leap(2.0D, 3D);
    	
    	this.applyStaminaCost();
    }
    
    public float getStaminaCost() {
    	return 20;
    }
    
    public int getStaminaRecoveryWarmup() {
    	return 5 * 20;
    }
    
    public float getStaminaRecoveryMax() {
    	return 1.0F;
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
    public float getFallResistance() {
    	return 100;
    }


	// ==================================================
	//                     Pet Control
	// ==================================================
	public boolean petControlsEnabled() { return true; }
}
