package com.lycanitesmobs.core.entity.creature.reptile;

import com.lycanitesmobs.core.entity.IGroupHeavy;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.block.Material;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;

public class EntityArisaur extends AgeableCreatureEntity implements IGroupHeavy {

    public EntityArisaur(EntityType<? extends EntityArisaur> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        //this.solidCollision = true;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false));
    }

    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        if (this.level().getBlockState(new BlockPos(x, y - 1, z)).getBlock() != Blocks.AIR) {
            BlockState blocState = this.level().getBlockState(new BlockPos(x, y - 1, z));
            if (blocState.is(LycanitesBlockTags.CREATURE_PATH_GRASS_PREFERRED))
                return 10F;
            if (blocState.is(LycanitesBlockTags.CREATURE_PATH_DIRT_PREFERRED))
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
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
    public boolean canBeLeashed() {
        return true;
    }

    /**
     * Returns this creature's main texture. Also checks for for subspecies.
     **/
    @Override
    public Identifier getTexture() {
        if (!this.hasCustomName() || !"Flowersaur".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_flowersaur";
        return AssetHelper.entityTexture(textureName);
    }
}
