package com.lycanitesmobs.core.entity.creature.insect;

import com.lycanitesmobs.core.entity.base.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.util.CreatureRelationshipEntry;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.abilities.PlaceBlockGoal;
import com.lycanitesmobs.core.entity.goals.targeting.FindMasterGoal;
import com.lycanitesmobs.core.util.ContextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.lycanitesmobs.core.entity.creature.beast.EntityConba;

public class EntityVespid extends AgeableCreatureEntity implements Enemy {
    protected PlaceBlockGoal aiPlaceBlock;
    protected ContextUtils.CreatureBuildTask creatureBuildTask;

    private boolean hiveBuilding = true;

    public EntityVespid(EntityType<? extends EntityVespid> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.ARTHROPOD;
        this.hasAttackSound = true;
        this.setupMob();

        this.canGrow = true;
        this.babySpawnChance = 0.1D;

        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(1.0F);
        this.setAttackCooldownMax(10);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackMeleeGoal(this).setLongMemory(true));
        this.aiPlaceBlock = new PlaceBlockGoal(this).setMaxDistance(128D).setSpeed(3D).setReplaceLiquid(true).setReplaceSolid(true);
        this.goalSelector.addGoal(this.claimIdleGoalIndex(), this.aiPlaceBlock);

        this.targetSelector.addGoal(this.claimFindTargetGoalIndex(), new FindMasterGoal(this).setTargetClass(EntityVespidQueen.class).setRange(64.0D));
    }

    @Override
    public void loadCreatureFlags() {
        this.hiveBuilding = this.creatureInfo.getFlag("hiveBuilding", this.hiveBuilding);
    }

    @Override
    public boolean isPersistant() {
        if (this.getMasterTarget() instanceof BaseCreatureEntity)
            return ((BaseCreatureEntity) this.getMasterTarget()).isPersistant();
        return super.isPersistant();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Building AI:
        if (!this.getCommandSenderWorld().isClientSide && this.hiveBuilding && this.getMasterTarget() instanceof EntityVespidQueen && !this.aiPlaceBlock.hasBlockPlacement()) {
            EntityVespidQueen queen = (EntityVespidQueen) this.getMasterTarget();
            this.creatureBuildTask = queen.getBuildTaskFor(this);
            if (this.creatureBuildTask != null) {
                this.aiPlaceBlock.setBlockPlacement(this.creatureBuildTask.blockState, this.creatureBuildTask.pos);
            }
        }

        // Don't Keep Infected Conbas Targeted:
        if (!this.getCommandSenderWorld().isClientSide && this.getTarget() instanceof EntityConba) {
            if (((EntityConba) this.getTarget()).isVespidInfected()) {
                this.setTarget(null);
            }
        }
    }

    @Override
    public void onBlockPlaced(BlockPos blockPos, BlockState blockState) {
        if (this.getMasterTarget() instanceof EntityVespidQueen) {
            EntityVespidQueen queen = (EntityVespidQueen) this.getMasterTarget();
            if (this.creatureBuildTask != null) {
                queen.completeBuildTask(this.creatureBuildTask);
                this.creatureBuildTask = null;
            }
        }
    }

    @Override
    public boolean canAttack(LivingEntity targetEntity) {
        if (targetEntity == this.getMasterTarget())
            return false;
        if (targetEntity instanceof EntityConba)
            return false;
        if (targetEntity instanceof EntityVespid) {
            if (!((EntityVespid) targetEntity).hasMaster() || ((EntityVespid) targetEntity).getMasterTarget() == this.getMasterTarget())
                return false;
        }
        if (targetEntity instanceof EntityVespidQueen) {
            if (!this.hasMaster() || this.getMasterTarget() == targetEntity)
                return false;
        }
        if (this.hasMaster() && this.getMasterTarget() instanceof EntityVespidQueen) {
            EntityVespidQueen entityVespidQueen = (EntityVespidQueen) this.getMasterTarget();
            CreatureRelationshipEntry creatureRelationshipEntry = entityVespidQueen.getRelationshipEntry(targetEntity);
            if (creatureRelationshipEntry != null && !creatureRelationshipEntry.canAttack()) {
                return false;
            }
        }
        return super.canAttack(targetEntity);
    }

    @Override
    public boolean isFlying() {
        return true;
    }

    @Override
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 4.0F; // 1.15.2 parity: fire vulnerability x4
        return super.getDamageModifier(damageSrc);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypes.IN_WALL)) return true;
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean hasLineOfSight(Entity entity) {
        if (entity instanceof EntityVespidQueen) {
            return true;
        }
        return super.hasLineOfSight(entity);
    }
}