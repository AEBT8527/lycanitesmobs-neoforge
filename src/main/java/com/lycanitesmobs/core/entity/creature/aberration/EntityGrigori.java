package com.lycanitesmobs.core.entity.creature.aberration;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.CopyMasterAttackTargetGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class EntityGrigori extends TameableCreatureEntity implements Enemy {
    public EntityGrigori(EntityType<? extends EntityGrigori> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
		this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setSpeed(2.0D).setLongMemory(false));

		this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindMasterGoal(this).setTargetClass(EntityGrell.class).setSightCheck(false));
		this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new CopyMasterAttackTargetGoal(this));
    }

	@Override
	public boolean canAttack(LivingEntity target) {
		if(target.getVehicle() instanceof EntityGrell)
			return false;
		return super.canAttack(target);
	}

	@Override
	public boolean rollWanderChance() {
		return this.getRandom().nextDouble() <= 0.25D;
	}
	
	public boolean isFlying() { return true; }

    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.getBagSize(); }
    @Override
    public boolean canBurn() { return false; }
}
