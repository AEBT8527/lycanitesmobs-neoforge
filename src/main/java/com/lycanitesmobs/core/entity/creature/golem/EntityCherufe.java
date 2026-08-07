package com.lycanitesmobs.core.entity.creature.golem;

import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.item.CustomItemEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import com.lycanitesmobs.core.entity.LycanitesMobType;
import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.joml.Vector3d;

public class EntityCherufe extends BaseCreatureEntity implements Enemy {

    private int blockMeltingRadius = 2;

    // ==================================================
    //                    Constructor
    // ==================================================
    public EntityCherufe(EntityType<? extends EntityCherufe> entityType, Level world) {
        super(entityType, world);

        // Setup:
        this.attribute = LycanitesMobType.UNDEFINED;
        this.spawnsOnLand = true;
        this.spawnsInWater = true;
        this.isLavaCreature = true;
        this.hasAttackSound = false;

        this.setupMob();
        this.setPathfindingMalus(PathType.LAVA, 0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.claimCombatGoalIndex(), new AttackRangedGoal(this).setSpeed(1.0D).setRange(16.0F).setMinChaseDistance(8.0F));
    }

    @Override
    public void loadCreatureFlags() {
        this.blockMeltingRadius = this.creatureInfo.getFlag("blockMeltingRadius", this.blockMeltingRadius);
    }


    // ==================================================
    //                      Updates
    // ==================================================
    // ========== Living Update ==========
    @Override
    public void aiStep() {
        super.aiStep();

        // Trail:
        if (!this.level().isClientSide() && this.isMoving() && this.tickCount % 5 == 0) {
            int trailHeight = 1;
            int trailWidth = 1;
            if (this.isRareVariant())
                trailWidth = 3;
            for (int y = 0; y < trailHeight; y++) {
                BlockState trailState = this.level().getBlockState(this.blockPosition().offset(0, y, 0));
                if (trailState.is(LycanitesBlockTags.CHERUFE_FIRE_TRAIL_REPLACEABLE)) {
                    if (trailWidth == 1)
                        this.level().setBlockAndUpdate(this.blockPosition().offset(0, y, 0), Blocks.FIRE.defaultBlockState());
                    else
                        for (int x = -(trailWidth / 2); x < (trailWidth / 2) + 1; x++) {
                            for (int z = -(trailWidth / 2); z < (trailWidth / 2) + 1; z++) {
                                this.level().setBlockAndUpdate(this.blockPosition().offset(x, y, z), Blocks.FIRE.defaultBlockState());
                            }
                        }
                }
            }
        }

        // Rare Subspecies Powers:
        if (!this.level().isClientSide() && this.isRareVariant() && LMHelperClass.getGameRuleBool(this.level(), GameRules.MOB_GRIEFING, true) && this.blockMeltingRadius > 0 && this.tickCount % 10 == 0) {

            // Melt Blocks:
            int range = this.blockMeltingRadius;
            for (int w = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); w <= (Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); w++)
                for (int d = -((int) Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); d <= (Math.ceil(this.getDimensions(Pose.STANDING).width()) + range); d++)
                    for (int h = 0; h <= Math.ceil(this.getDimensions(Pose.STANDING).height()); h++) {
                        BlockState meltState = this.level().getBlockState(this.blockPosition().offset(w, h, d));
                        if (meltState.is(LycanitesBlockTags.CHERUFE_MELTABLE)) {
                            BlockState blockState = Blocks.LAVA.defaultBlockState().setValue(BlockStateProperties.LEVEL, 5);
                            this.level().setBlockAndUpdate(this.blockPosition().offset(w, h, d), blockState);
                        }
                    }

            // Random Projectiles:
            if (this.tickCount % 40 == 0) {
                ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile("magma");
                if (projectileInfo != null) {
                    BaseProjectileEntity projectile = projectileInfo.createProjectile(this.level(), this);
                    projectile.setProjectileScale(2f);
                    projectile.shoot((2 * this.getRandom().nextFloat()) - 1, this.getRandom().nextFloat(), (2 * this.getRandom().nextFloat()) - 1, 1.2F, 6.0F);
                    this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                    DeferredLevelActionManager.spawnEntity(this.level(), this.blockPosition(), null, projectile);
                }
            }
        }

        // Particles:
        if (this.level().isClientSide()) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.FLAME, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
                this.level().addParticle(ParticleTypes.DRIPPING_LAVA, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
            }
            if (this.tickCount % 10 == 0)
                for (int i = 0; i < 2; ++i) {
                    this.level().addParticle(ParticleTypes.FLAME, this.position().x() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), this.position().y() + this.getRandom().nextDouble() * (double) this.getDimensions(Pose.STANDING).height(), this.position().z() + (this.getRandom().nextDouble() - 0.5D) * (double) this.getDimensions(Pose.STANDING).width(), 0.0D, 0.0D, 0.0D);
                }
        }
    }


    // ==================================================
    //                      Movement
    // ==================================================
    // ========== Movement Speed Modifier ==========
    @Override
    public float getAISpeedModifier() {
        if (this.lavaContact())
            return 2.0F;
        return 1.0F;
    }

    // Pathing Weight:
    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        int waterWeight = 10;
        BlockPos pos = new BlockPos(x, y, z);
        if (this.level().getBlockState(pos).getBlock() == Blocks.LAVA)
            return (super.getBlockPathWeight(x, y, z) + 1) * (waterWeight + 1);

        if (this.getTarget() != null)
            return super.getBlockPathWeight(x, y, z);
        if (this.lavaContact())
            return -999999.0F;

        return super.getBlockPathWeight(x, y, z);
    }

    // Pushed By Water:
    @Override
    public boolean isPushedByFluid() {
        return false;
    }


    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Ranged Attack ==========
    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("magma", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
        super.attackRanged(target, range);
    }

    // ========== Is Aggressive ==========
    @Override
    public boolean isAggressive() {
        if (this.getAirSupply() <= -100)
            return false;
        return super.isAggressive();
    }


    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean canBurn() {
        return false;
    }

    @Override
    public boolean waterDamage() {
        return !this.isRareVariant();
    }

    @Override
    public boolean canBreatheUnderlava() {
        return true;
    }

    @Override
    public boolean canBreatheAir() {
        return true;
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
    //                    Taking Damage
    // ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        if (damageSrc.is(DamageTypeTags.IS_FIRE))
            return 0F;
        else return super.getDamageModifier(damageSrc);
    }


    // ==================================================
    //                       Drops
    // ==================================================
    // ========== Apply Drop Effects ==========

    /**
     * Used to add effects or alter the dropped entity item.
     **/
    @Override
    public void applyDropEffects(CustomItemEntity entityitem) {
        entityitem.setCanBurn(false);
    }


    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }
}
