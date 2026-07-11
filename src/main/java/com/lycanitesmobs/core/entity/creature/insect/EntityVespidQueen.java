package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.data.info.creature.CreatureType;
import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureStructure;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.StayByHomeGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindAttackTargetGoal;
import com.lycanitesmobs.core.manager.CreatureManager;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.item.consumable.entity.CreatureTreatItem;
import com.lycanitesmobs.core.util.ContextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.lycanitesmobs.core.entity.creature.beast.EntityConba;

public class EntityVespidQueen extends TameableCreatureEntity implements Enemy {
    private final CreatureStructure creatureStructure;
    protected int swarmLimit = 10;

    public EntityVespidQueen(EntityType<? extends EntityVespidQueen> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.solidCollision = true;
        this.setupMob();

        this.canGrow = true;
        this.babySpawnChance = 0D;

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
        this.setAttackCooldownMax(10);

        this.creatureStructure = new CreatureStructure(this, DungeonManager.getInstance().getTheme("vespid_hive"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(true));
        this.goalSelector.addGoal(this.claimTravelGoalIndex(), new StayByHomeGoal(this));

        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(this.getType()));
        EntityType conbaType = CreatureManager.getInstance().getEntityType("conba");
        if (conbaType != null)
            this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindAttackTargetGoal(this).addTargets(conbaType));
    }

    @Override
    public void loadCreatureFlags() {
        this.swarmLimit = this.creatureInfo.getFlag("swarmLimit", this.swarmLimit);
    }

    @Override
    public boolean isPersistant() {
        if (this.hasHome() && this.level().getDifficulty() != Difficulty.PEACEFUL)
            return true;
        return super.isPersistant();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }

        if (this.updateTick > 0 && this.updateTick % 20 == 0) {
            // Hive Structure:
            if (!this.hasHome()) {
                this.creatureStructure.setOrigin(this.blockPosition());
            }
            boolean structureStarted = this.creatureStructure.isPhaseComplete(0);
            if (!structureStarted || this.updateTick % 200 == 0) {
                this.creatureStructure.refreshBuildTasks();
            }
            if (structureStarted && !this.hasHome()) {
                this.setHome(this.creatureStructure.getOrigin().getX(), this.creatureStructure.getOrigin().getY(), this.creatureStructure.getOrigin().getZ(), 8F);
            }

            // Spawn Babies:
            if (structureStarted && this.creatureStructure.getFinalPhaseBuildTaskSize() <= 10 && this.updateTick % 60 == 0) {
                this.allyUpdate();
            }
        }

        // Don't Keep Infected Conbas Targeted:
        if (this.getTarget() instanceof EntityConba) {
            if (((EntityConba) this.getTarget()).isVespidInfected()) {
                this.setTarget(null);
            }
        }
    }

    @Override
    public void setHomePosition(int x, int y, int z) {
        super.setHomePosition(x, y, z);
        this.creatureStructure.setOrigin(new BlockPos(x, y, z));
    }

    @Override
    public boolean rollWanderChance() {
        if (this.hasHome()) {
            return false;
        }
        return this.getRandom().nextDouble() <= 0.0008D;
    }

    public void allyUpdate() {
        if (this.level().isClientSide())
            return;

        // Spawn Babies:
        if (this.swarmLimit > 0 && this.nearbyCreatureCount(CreatureManager.getInstance().getCreature("vespid").getEntityType(), 32D) < this.swarmLimit) {
            float random = this.getRandom().nextFloat();
            if (random <= 0.05F) {
                LivingEntity minion = this.spawnAlly(this.position().x() - 2 + (random * 4), this.position().y(), this.position().z() - 2 + (random * 4));
                if (minion instanceof AgeableCreatureEntity) {
                    AgeableCreatureEntity ageableMinion = (AgeableCreatureEntity) minion;
                    ageableMinion.setGrowingAge(ageableMinion.getInitialGrowthTime());
                }
            }
        }
    }

    public LivingEntity spawnAlly(double x, double y, double z) {
        LivingEntity minion = CreatureManager.getInstance().getCreature("vespid").createEntity(this.level());
        minion.snapTo(x, y, z, this.getRandom().nextFloat() * 360.0F, 0.0F);
        if (minion instanceof BaseCreatureEntity) {
            ((BaseCreatureEntity) minion).applyVariant(this.getVariantIndex());
        }
        DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, minion);
        if (this.getTarget() != null)
            minion.setLastHurtByMob(this.getTarget());
        return minion;
    }

    public ContextUtils.CreatureBuildTask getBuildTaskFor(EntityVespid vespid) {
        return this.creatureStructure.getBuildTask(vespid);
    }

    public void completeBuildTask(ContextUtils.CreatureBuildTask creatureBuildTask) {
        this.creatureStructure.completeBuildTask(creatureBuildTask);
    }

    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        if (target instanceof EntityConba) {
            ((EntityConba) target).infectWithVespids();
            return true;
        }

        return true;
    }

    @Override
    public boolean canAttack(LivingEntity targetEntity) {
        if (targetEntity instanceof EntityConba)
            if (((EntityConba) targetEntity).isVespidInfected())
                return false;
        if (targetEntity instanceof EntityVespid) {
            if (!((EntityVespid) targetEntity).hasMaster() || ((EntityVespid) targetEntity).getMasterTarget() == this)
                return false;
        }
        return super.canAttack(targetEntity);
    }

    @Override
    public boolean canAttackOwnSpecies() {
        return true;
    }

    @Override
    public boolean canBeTempted() {
        return true;
    }

    @Override
    public boolean isTamingItem(ItemStack itemStack) {
        CreatureType creatureType = this.creatureInfo.getCreatureType();
        if (itemStack.isEmpty() || creatureType == null) {
            return false;
        }

        if (itemStack.getItem() instanceof CreatureTreatItem) {
            CreatureTreatItem itemTreat = (CreatureTreatItem) itemStack.getItem();
            if (itemTreat.getCreatureType() == creatureType) {
                return true;
            }
        }

        return super.isTamingItem(itemStack);
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 2.0F;
        return super.getDamageModifier(damageSrc);
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
        if (source.is(DamageTypes.IN_WALL)) return true;
        return super.isInvulnerableTo(lycLevel, source);
    }
}