package com.lycanitesmobs.core.entity.creature.undead;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.BreakDoorGoal;
import com.lycanitesmobs.core.entity.goals.actions.MoveVillageGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class EntityCryptkeeper extends AgeableCreatureEntity implements Enemy {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCryptkeeper(EntityType<? extends EntityCryptkeeper> entityType, Level world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = LycanitesMobType.UNDEAD;
        this.hasAttackSound = false;
        this.spreadFire = true;
        this.canGrow = false;
        this.babySpawnChance = 0.1D;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(this.claimTravelGoalIndex(), new MoveVillageGoal(this));

        super.registerGoals();

        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(EntityType.HUSK));

        this.goalSelector.addGoal(this.claimDistractionGoalIndex(), new BreakDoorGoal(this));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setTargetClass(Player.class).setLongMemory(false));
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this));

        if(this.getNavigation() instanceof GroundPathNavigation) {
            GroundPathNavigation pathNavigateGround = (GroundPathNavigation)this.getNavigation();
            pathNavigateGround.setCanOpenDoors(true);
            pathNavigateGround.setAvoidSun(true);
        }
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
    // ========== On Kill ==========
    @Override
    public void onKillEntity(LivingEntity entityLivingBase) {
        super.onKillEntity(entityLivingBase);

        if(this.getCommandSenderWorld().getDifficulty().getId() >= 2 && entityLivingBase instanceof Villager) {
            if (this.getCommandSenderWorld().getDifficulty().getId() == 2 && this.random.nextBoolean()) return;

            Villager villagerentity = (Villager)entityLivingBase;
            ZombieVillager zombievillagerentity = EntityType.ZOMBIE_VILLAGER.create(this.level());
            zombievillagerentity.copyPosition(villagerentity);
            villagerentity.remove(RemovalReason.DISCARDED);
            zombievillagerentity.finalizeSpawn((ServerLevelAccessor) this.getCommandSenderWorld(), this.getCommandSenderWorld().getCurrentDifficultyAt(zombievillagerentity.blockPosition()), MobSpawnType.CONVERSION, null);
            zombievillagerentity.setVillagerData(villagerentity.getVillagerData());
            zombievillagerentity.setTradeOffers(villagerentity.getOffers().copy());
            zombievillagerentity.setVillagerXp(villagerentity.getVillagerXp());
            zombievillagerentity.setBaby(villagerentity.isBaby());
            zombievillagerentity.setNoAi(villagerentity.isNoAi());

            if (villagerentity.hasCustomName()) {
                zombievillagerentity.setCustomName(villagerentity.getCustomName());
                zombievillagerentity.setCustomNameVisible(villagerentity.isCustomNameVisible());
            }

            DeferredLevelActionManager.spawnEntity(this.getCommandSenderWorld(), this.blockPosition(), null, zombievillagerentity);
            this.getCommandSenderWorld().levelEvent(null, 1016, zombievillagerentity.blockPosition(), 0);
        }
    }
}
