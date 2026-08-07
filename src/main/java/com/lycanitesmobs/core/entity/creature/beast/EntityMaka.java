package com.lycanitesmobs.core.entity.creature.beast;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.block.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EntityMaka extends AgeableCreatureEntity {
	
	// ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityMaka(EntityType<? extends EntityMaka> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.hasAttackSound = true;

        this.babySpawnChance = 0.1D;
        this.attackCooldownMax = 10;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new TemptGoal(this).setIncludeDiet(true));
		this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(false));

		this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindMasterGoal(this).setTargetClass(EntityMakaAlpha.class).setSightCheck(false));
    }


    // ==================================================
    //                      Spawn
    // ==================================================
    // ========== On Spawn ==========
    @Override
    public void onFirstSpawn() {
        // Random Alpha:
        CreatureInfo alphaInfo = CreatureManager.getInstance().getCreature("makaalpha");
        if(alphaInfo != null) {
            float alphaChance = (float)alphaInfo.getCreatureSpawn().getSpawnWeight() / Math.max(this.creatureInfo.getCreatureSpawn().getSpawnWeight(), 1);
            if (this.getRandom().nextFloat() <= alphaChance) {
				EntityMakaAlpha alpha = (EntityMakaAlpha)CreatureManager.getInstance().getCreature("makaalpha").createEntity(this.getCommandSenderWorld());
                alpha.copyPosition(this);
                DeferredLevelActionManager.spawnEntity(this.getCommandSenderWorld(), this.blockPosition(), null, alpha);
                this.remove(RemovalReason.DISCARDED);
            }
        }
        super.onFirstSpawn();
    }
	
	
	// ==================================================
   	//                      Movement
   	// ==================================================
	// ========== Pathing Weight ==========
	@Override
	public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
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
    // ========== Attack Class ==========
	@Override
	public boolean canAttack(LivingEntity target) {
		if(target instanceof EntityMaka || target instanceof EntityMakaAlpha)
			return false;
		return super.canAttack(target);
	}
    
    
    // ==================================================
    //                     Growing
    // ==================================================
	@Override
	public void setGrowingAge(int age) {
		if(age == 0 && this.getAge() < 0) {
            if (this.getRandom().nextFloat() >= 0.9F) {
				EntityMakaAlpha alpha = (EntityMakaAlpha)CreatureManager.getInstance().getCreature("makaalpha").createEntity(this.getCommandSenderWorld());
                alpha.copyPosition(this);
                DeferredLevelActionManager.spawnEntity(this.getCommandSenderWorld(), this.blockPosition(), null, alpha);
                this.remove(RemovalReason.DISCARDED);
            }
        }
        super.setGrowingAge(age);
    }
}
