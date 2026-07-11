package com.lycanitesmobs.core.entity.creature.undead;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

public class EntityNecrovore extends TameableCreatureEntity {
    public EntityNecrovore(EntityType<? extends EntityNecrovore> entityType, Level world) {
        super(entityType, world);

        this.attribute = LycanitesMobType.UNDEAD;
        this.hasAttackSound = true;
        this.spreadFire = true;

        this.canGrow = false;
        this.babySpawnChance = 0.01D;

        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setSpeed(1.5D));

        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.STRAY, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER));
        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(EntityGeist.class, EntityGhoul.class, EntityCryptkeeper.class));

        if(this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation)this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
            pathNavigateGround.setAvoidSun(true);
        }
    }

    @Override
    public boolean daylightBurns() {
        return !this.isBaby() && !this.isTamed();
    }

    @Override
    public boolean canBurn() {
        return true;
    }
}
