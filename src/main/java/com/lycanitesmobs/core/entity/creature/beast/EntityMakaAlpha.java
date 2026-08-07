package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.DefendEntitiesGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityMakaAlpha extends AgeableCreatureEntity {
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityMakaAlpha(EntityType<? extends EntityMakaAlpha> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.attackCooldownMax = 10;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(this.getClass()));
		this.targetSelector.addGoal(this.claimSpecialTargetGoalIndex(), new DefendEntitiesGoal(this, EntityMaka.class));

		this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
		this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));
    }
	
	
	// ==================================================
  	//                      Update
  	// ==================================================
	@Override
	public void aiStep() {
		super.aiStep();
		
		// Alpha Sparring Cooldown:
		if(this.hasAttackTarget() && this.getTarget() instanceof EntityMakaAlpha) {
			if(this.getHealth() / this.getMaxHealth() <= 0.25F || this.getTarget().getHealth() / this.getTarget().getMaxHealth() <= 0.25F) {
				this.setTarget(null);
			}
		}
	}

	@Override
	public boolean isProtective(Entity entity) {
		if(entity instanceof EntityMaka) {
			return true;
		}
		return super.isProtective(entity);
	}
	
	
	// ==================================================
   	//                      Movement
   	// ==================================================
    // ========== Pathing Weight ==========
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.level().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(blockState.is(LycanitesBlockTags.CREATURE_PATH_GRASS_PREFERRED))
                return 10F;
            if(blockState.is(LycanitesBlockTags.CREATURE_PATH_DIRT_PREFERRED))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    // ========== Can leash ==========
    @Override
    public boolean canBeLeashed() {
        return true;
    }

	// ==================================================
	//                     Equipment
	// ==================================================
	@Override
	public int getNoBagSize() { return 0; }
	@Override
	public int getBagSize() { return this.creatureInfo.getBagSize(); }
	// ==================================================
   	//                      Attacks
   	// ==================================================
	@Override
    public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityMaka)
			return false;
    	if(target instanceof EntityMakaAlpha && (this.getHealth() / this.getMaxHealth() <= 0.25F || target.getHealth() / target.getMaxHealth() <= 0.25F))
    		return false;
    	return super.canAttack(target);
    }

	@Override
	public boolean canAttackOwnSpecies() {
		return true;
	}

    @Override
    public void setTarget(LivingEntity entity) {
		// Capture the old target first and only heal if the clear actually took effect -
		// a target change can be vetoed, in which case no bond is being broken.
		LivingEntity previousTarget = this.getTarget();
		super.setTarget(entity);
		if(entity == null && previousTarget instanceof EntityMakaAlpha && this.getTarget() == null) {
    		this.heal((this.getMaxHealth() - this.getHealth()) / 2);
    		this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 2, false, false));
			previousTarget.heal((this.getMaxHealth() - this.getHealth()) / 2);
			previousTarget.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 2, false, false));
    	}
    }

	@Override
	public boolean rollAttackTargetChance(LivingEntity target) {
    	if(target instanceof Player || target.getType() == this.getType())
    		return this.getRandom().nextDouble() <= 0.01D;
		return true;
	}
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        float damageMod = super.getDamageModifier(damageSrc);
        if(damageSrc.getEntity() instanceof EntityMakaAlpha)
            damageMod *= 2;
        return damageMod;
    }
    
    
    // ==================================================
    //                     Breeding
    // ==================================================
    // ========== Create Child ==========
	@Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		return (AgeableCreatureEntity) CreatureManager.getInstance().getCreature("maka").createEntity(this.level());
	}
}
