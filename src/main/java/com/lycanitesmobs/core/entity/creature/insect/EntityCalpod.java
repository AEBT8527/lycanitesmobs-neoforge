package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;

public class EntityCalpod extends BaseCreatureEntity implements Enemy {
	private int swarmLimit = 5;
	private boolean griefing = true;

    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCalpod(EntityType<? extends EntityCalpod> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.setupMob();
	}

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(true));
    }

	@Override
	public void loadCreatureFlags() {
		this.swarmLimit = this.creatureInfo.getFlag("swarmLimit", this.swarmLimit);
		this.griefing = this.creatureInfo.getFlag("griefing", this.griefing);
	}
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
		if(!this.level().isClientSide() && this.hasAttackTarget() && this.getTarget() instanceof Player && this.updateTick % 60 == 0) {
			this.allyUpdate();
		}

		// Destroy Blocks:
		if(!this.level().isClientSide())
			if(this.getTarget() != null && LMHelperClass.getGameRuleBool(this.level(), GameRules.MOB_GRIEFING, true) && this.griefing) {
				float distance = this.getTarget().distanceTo(this);
				if(distance <= this.getDimensions(Pose.STANDING).width() + 1.0F)
					this.destroyAreaBlock((int)this.position().x(), (int)this.position().y(), (int)this.position().z(), WoodType.class, true, 0);
			}
        
        super.aiStep();
    }
    
    // ========== Spawn Minions ==========
	public void allyUpdate() {
		if(this.level().isClientSide())
			return;
		
		// Spawn Minions:
		if(this.swarmLimit > 0 && this.countAllies(64D) < this.swarmLimit) {
			float random = this.getRandom().nextFloat();
			if(random <= 0.125F)
				this.spawnAlly(this.position().x() - 2 + (random * 4), this.position().y(), this.position().z() - 2 + (random * 4));
		}
	}
	
    public void spawnAlly(double x, double y, double z) {
		BaseCreatureEntity minion = (BaseCreatureEntity) this.creatureInfo.createEntity(this.level());
    	minion.snapTo(x, y, z, this.getRandom().nextFloat() * 360.0F, 0.0F);
		minion.setMinion(true);
		minion.applyVariant(this.getVariantIndex());
		DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, minion);
        if(this.getTarget() != null)
        	minion.setLastHurtByMob(this.getTarget());
    }
    
    
    // ==================================================
    //                       Death
    // ==================================================
    @Override
    public void die(DamageSource par1DamageSource) {
    	allyUpdate();
        super.die(par1DamageSource);
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
	// ========== Movement ==========
	@Override
	public boolean canClimb() { return true; }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
        return 100;
    }
}
