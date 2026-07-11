package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityBalayang extends TameableCreatureEntity implements Enemy {

    public EntityBalayang(EntityType<? extends EntityBalayang> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;
        this.flySoundSpeed = 20;
        this.setupMob();

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.getBagSize();
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel lycLevel, DamageSource source) {
        if (source.is(DamageTypes.CACTUS)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }
}
