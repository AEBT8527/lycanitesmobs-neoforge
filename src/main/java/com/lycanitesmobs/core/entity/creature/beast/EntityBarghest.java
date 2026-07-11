package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.RideableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class EntityBarghest extends RideableCreatureEntity {

    protected boolean leapedAbilityQueued = false;
    protected boolean leapedAbilityReady = false;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityBarghest(EntityType<? extends EntityBarghest> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.spreadFire = false;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
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
        
        // Random Leaping:
        if(!this.isTamed() && this.onGround() && !this.level().isClientSide()) {
        	if(this.hasAttackTarget()) {
        		if(this.getRandom().nextInt(10) == 0)
        			this.leap(4.0F, 0.5D, this.getTarget());
        	}
        }

        // Leap Landing Paralysis:
        if(this.leapedAbilityQueued && !this.onGround() && !this.level().isClientSide()) {
            this.leapedAbilityQueued = false;
            this.leapedAbilityReady = true;
        }
        if(this.leapedAbilityReady && this.onGround() && !this.level().isClientSide()) {
            this.leapedAbilityReady = false;
            double distance = 4.0D;
            List<LivingEntity> possibleTargets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(distance, distance, distance), possibleTarget -> {
				if (!possibleTarget.isAlive()
						|| possibleTarget == EntityBarghest.this
						|| EntityBarghest.this.isEntityPassenger(possibleTarget, EntityBarghest.this)
						|| EntityBarghest.this.isAlliedTo(possibleTarget)
						|| !EntityBarghest.this.canAttackType(possibleTarget.getType())
						|| !EntityBarghest.this.canAttack(possibleTarget))
					return false;

				return true;
			});
            if(!possibleTargets.isEmpty()) {
                for(LivingEntity possibleTarget : possibleTargets) {
                    boolean doDamage = true;
                    if(this.getRider() instanceof Player) {
                        if(NeoForge.EVENT_BUS.post(new AttackEntityEvent((Player)this.getRider(), possibleTarget)).isCanceled()) {
                            doDamage = false;
                        }
                    }
                    if(doDamage) {
                        if (ObjectManager.getEffect("weight") != null)
                            possibleTarget.addEffect(new MobEffectInstance(ObjectManager.holder(ObjectManager.getEffect("weight")), this.getEffectDuration(5), 1));
                        else
                            possibleTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10 * 20, 0));
                    }
                }
            }
            this.playAttackSound();
        }
    }
    
    public void riderEffects(LivingEntity rider) {
    	if(rider.hasEffect(MobEffects.SLOWNESS))
    		rider.removeEffect(MobEffects.SLOWNESS);
    	if(rider.hasEffect(ObjectManager.holder(ObjectManager.getEffect("weight"))))
    		rider.removeEffect(ObjectManager.holder(ObjectManager.getEffect("weight")));
    }

	
    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
    	if(!this.onGround())
    		return 2.0F;
    	return 1.0F;
    }

    // ========== Mounted Offset ==========
        public double getPassengersRidingOffset() {
        return (double)this.getDimensions(Pose.STANDING).height() * 0.8D;
    }

    // ========== Leap ==========
    @Override
    public void leap(double distance, double leapHeight) {
        super.leap(distance, leapHeight);
        if(!this.level().isClientSide())
            this.leapedAbilityQueued = true;
    }

    // ========== Leap to Target ==========
    @Override
    public void leap(float range, double leapHeight, Entity target) {
        super.leap(range, leapHeight, target);
        if(!this.level().isClientSide())
            this.leapedAbilityQueued = true;
    }

    
    // ==================================================
    //                   Mount Ability
    // ==================================================
    public void mountAbility(Entity rider) {
    	if(this.level().isClientSide())
    		return;

        if(!this.onGround())
            return;
    	if(this.abilityToggled)
    		return;
    	if(this.getStamina() < this.getStaminaCost())
    		return;
    	
    	this.playJumpSound();
    	this.leap(2.0D, 1.5D);
    	
    	this.applyStaminaCost();
    }
    
    public float getStaminaCost() {
    	return 15;
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
    //                     Abilities
    // ==================================================
    @Override
    public boolean canClimb() { return true; }


	// ==================================================
	//                     Pet Control
	// ==================================================
	public boolean petControlsEnabled() { return true; }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
    	return 20;
    }
}
