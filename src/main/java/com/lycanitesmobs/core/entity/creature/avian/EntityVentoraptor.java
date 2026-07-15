package com.lycanitesmobs.core.entity.creature.avian;

import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityVentoraptor extends RideableCreatureEntity {
    private static final int MOUNT_ABILITY_BOOST_TICKS = 10;

    protected int mountAbilityBoostTicks = 0;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityVentoraptor(EntityType<? extends EntityVentoraptor> entityType, Level world) {
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
        			this.leap(6.0F, 0.5D, this.getTarget());
        	}
        	else {
        		if(this.getRandom().nextInt(50) == 0 && this.isMoving())
        			this.leap(2.0D, 0.5D);
        	}
        }
    }

    @Override
    public void riderEffects(LivingEntity rider) {
        if(rider.hasEffect(MobEffects.WEAKNESS))
            rider.removeEffect(MobEffects.WEAKNESS);
        if(rider.hasEffect(MobEffects.SLOWNESS))
            rider.removeEffect(MobEffects.SLOWNESS);
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
    	return 0.99D;
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
        this.leap(4D, 0.5D); // 1.15.2 parity
    	
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
