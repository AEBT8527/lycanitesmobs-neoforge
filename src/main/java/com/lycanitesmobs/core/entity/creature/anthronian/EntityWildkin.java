package com.lycanitesmobs.core.entity.creature.anthronian;

import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

public class EntityWildkin extends TameableCreatureEntity implements Enemy {

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityWildkin(EntityType<? extends EntityWildkin> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        this.canGrow = true;
        this.babySpawnChance = 0.01D;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false));

        if (this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation) this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Falling Speed Modifier ==========
    @Override
    public double getFallingMod() {
        return 0.8D;
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public float getFallResistance() {
        return 100;
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
    @Override
    public ResourceLocation getTexture() {
        if (!this.hasCustomName() || !"Gooderness".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_gooderness";
        return AssetHelper.entityTexture(textureName);
    }
}
