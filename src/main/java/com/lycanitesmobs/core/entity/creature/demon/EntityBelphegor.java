package com.lycanitesmobs.core.entity.creature.demon;

import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.projectile.hellfire.EntityHellfireOrb;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class EntityBelphegor extends TameableCreatureEntity implements Enemy {

    // Data Manager:
    protected static final EntityDataAccessor<Integer> HELLFIRE_ENERGY = SynchedEntityData.defineId(EntityBelphegor.class, EntityDataSerializers.INT);

    protected int hellfireEnergy = 0;
    protected List<EntityHellfireOrb> hellfireOrbs = new ArrayList<>();
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityBelphegor(EntityType<? extends EntityBelphegor> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEAD;
        this.hasAttackSound = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(this.claimTravelGoalIndex(), new MoveRestrictionGoal(this));

        super.registerGoals();

        this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(1.0D).setRange(16.0F).setMinChaseDistance(8.0F).setChaseTime(-1));

        if(this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation)this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
        }
    }

    // ========== Init ==========
    /** Initiates the entity setting all the values to be watched by the datawatcher. **/
    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HELLFIRE_ENERGY, this.hellfireEnergy);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Sync Hellfire Energy:
        if (!this.level().isClientSide()) {
            this.entityData.set(HELLFIRE_ENERGY, this.hellfireEnergy);
        }
        else {
            try {
                this.hellfireEnergy = this.entityData.get(HELLFIRE_ENERGY);
            }
            catch(Exception e) {}
        }

        // Hellfire Update:
        if(this.level().isClientSide() && this.hellfireEnergy > 0)
            EntityRahovart.updateHellfireOrbs(this, this.updateTick, 3, this.hellfireEnergy, 0.5F, this.hellfireOrbs);
    }

    public void resetHellfireEnergy() {
        this.hellfireEnergy = 0;
    }

    public void addHellfireEnergy(int amount) {
        this.hellfireEnergy += amount;
    }

    public boolean hasFullHellfireEnergy() {
        return this.hellfireEnergy >= 100;
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttack(LivingEntity target) {
    	if(this.isTamed())
    		return super.canAttack(target);
    	if(target instanceof EntityBehemophet)
    		return false;
        return super.canAttack(target);
    }
    
    // ========== Ranged Attack ==========
	@Override
	public void attackRanged(Entity target, float range) {
		this.fireProjectile("doomfireball", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
		super.attackRanged(target, range);
	}
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }

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
    public boolean canBurn() { return false; }
}
