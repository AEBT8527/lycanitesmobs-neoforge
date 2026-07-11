package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveVillageGoal;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityJabberwock extends TameableCreatureEntity implements Enemy {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityJabberwock(EntityType<? extends EntityJabberwock> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        this.canGrow = false;
        this.babySpawnChance = 0.01D;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(this.claimTravelGoalIndex(), new MoveVillageGoal(this));

        super.registerGoals();

        this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));

        if (this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation) this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
        }
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Random Lunging:
        if (this.onGround() && !this.level().isClientSide()) {
            if (this.hasAttackTarget()) {
                if (this.getRandom().nextInt(10) == 0)
                    this.leap(6.0F, 0.1D, this.getTarget());
            }
        }
    }

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() {
        return 0;
    }

    @Override
    public int getBagSize() {
        return this.creatureInfo.getBagSize();
    }

    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public float getFallResistance() {
        return 50;
    }


    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() {
        return true;
    }


    // ==================================================
    //                       Visuals
    // ==================================================

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    public Identifier getTexture() {
        if (!this.hasCustomName() || !"Rudolph".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_rudolph";
        return AssetHelper.entityTexture(textureName);
    }
}
