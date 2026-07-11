package com.lycanitesmobs.core.entity.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.base.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.item.summoningstaff.ItemStaffSummoning;
import com.lycanitesmobs.core.entity.pets.SummonSet;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class PortalEntity extends BaseProjectileEntity {
    // Summoning Portal:
    private double targetX;
    private double targetY;
    private double targetZ;
    protected int summonAmount = 0;
    protected int summonTick = 0;
    protected int summonTime = 5 * 20;
    protected double portalRange = 32.0D;
    protected int summonDuration = 60 * 20;

    // Properties:
    protected Player shootingEntity;
    protected EntityType summonType;
    protected CreatureInfo creatureInfo;
    protected ItemStaffSummoning portalItem;
    protected UUID ownerUUID;
    protected TileEntitySummoningPedestal summoningPedestal;

    // Datawatcher:
    protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(PortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    // ==================================================
    //                   Constructors
    // ==================================================
    public PortalEntity(EntityType<? extends PortalEntity> entityType, Level world) {
        super(entityType, world);
        this.setStats();
    }

    public PortalEntity(EntityType<? extends PortalEntity> entityType, Level world, Player shooter, SummonSet summonSet, ItemStaffSummoning portalItem) {
        super(entityType, world, shooter);
        this.shootingEntity = shooter;
        this.summonType = summonSet.getCreatureType();
        this.creatureInfo = summonSet.getCreatureInfo();
        this.portalItem = portalItem;
        this.setStats();
    }

    public PortalEntity(EntityType<? extends PortalEntity> entityType, Level world, Player shooter, CreatureInfo creatureInfo, ItemStaffSummoning portalItem) {
        super(entityType, world, shooter);
        this.shootingEntity = shooter;
        this.creatureInfo = creatureInfo;
        this.summonType = creatureInfo.getEntityType();
        this.portalItem = portalItem;
        this.setStats();
    }

    public PortalEntity(EntityType<? extends PortalEntity> entityType, Level world, TileEntitySummoningPedestal summoningPedestal) {
        super(entityType, world);
        this.summoningPedestal = summoningPedestal;
        this.setStats();
        this.setPos(
                summoningPedestal.getBlockPos().getX() + 0.5D,
                summoningPedestal.getBlockPos().getY() + 1.5D,
                summoningPedestal.getBlockPos().getZ() + 0.5D
        );
    }

    public void setStats() {
        this.entityName = "summoningportal";
        this.setProjectileScale(6);
        this.moveToTarget();

        this.textureOffsetY = -0.5f;
        this.animationFrameMax = 7;
        this.movement = false;

        this.waterProof = true;
        this.lavaProof = true;

    }

    @Override
    public void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
    }

    public int getSummonAmount() {
        return this.summonAmount;
    }

    // ==================================================
    //                     Updates
    // ==================================================
    // ========== Main Update ==========
    @Override
    public void tick() {
        if (this.shootingEntity != null || this.summoningPedestal != null) {
            this.projectileLife = 5;
        }
        super.tick();

        // ========= Summoning Pedestal sync on server =========
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.summoningPedestal != null) {
                this.shootingEntity = this.summoningPedestal.getPlayer();
                this.summonType = this.summoningPedestal.getSummonType();
                this.creatureInfo = this.summoningPedestal.getCreatureInfo();
            }
        }

        // ========= Check for despawn =========
        if (!this.getCommandSenderWorld().isClientSide && this.isAlive()) {
            // From pedestal
            if (this.summoningPedestal != null) {
                if (this.summonType == null) {
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }
            }
            // From staff
            else {
                if (this.shootingEntity == null || !this.shootingEntity.isAlive() || this.portalItem == null) {
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }

                ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);

                boolean holdingStaff =
                        this.shootingEntity.getMainHandItem().getItem() instanceof ItemStaffSummoning ||
                                this.shootingEntity.getOffhandItem().getItem() instanceof ItemStaffSummoning;

                if (!holdingStaff || !this.shootingEntity.isUsingItem()) {
                    if (playerExt != null) {
                        playerExt.clearStaffPortalIf(this);
                    }
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }

                if (playerExt != null && !playerExt.isStaffPortal(this)) {
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }
            }
        }

        // ========= Sync owner UUID =========
        if (!this.getCommandSenderWorld().isClientSide) {
            if (this.shootingEntity != null) {
                this.entityData.set(OWNER_UUID, Optional.of(this.shootingEntity.getUUID()));
            } else if (this.summoningPedestal != null && this.summoningPedestal.getOwnerUUID() != null) {
                this.entityData.set(OWNER_UUID, Optional.of(this.summoningPedestal.getOwnerUUID()));
            } else {
                this.entityData.set(OWNER_UUID, Optional.empty());
            }
        } else {
            this.ownerUUID = this.entityData.get(OWNER_UUID).orElse(null);
        }

        // ========= Move to target and rest of your existing tick logic =========
        this.moveToTarget();

        // ========== Stat Sync ==========
        // Summoning Staff:
        if (this.shootingEntity != null && this.summoningPedestal == null) {
            ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);
            if (playerExt != null && this.portalItem != null) {
                if (++this.summonTick >= this.portalItem.getRapidTime(null)) {
                    this.grantStaffSummonAmount();
                    this.summonTick = 0;
                }
            }
        }

        // Summoning Pedestal:
        else if (this.summonType != null) {
            if (++this.summonTick >= this.summonTime) {
                this.summonAmount = this.summoningPedestal.getSummonAmount();
                this.summonTick = 0;
            }
        }

        // ========== Client ==========
        if (this.getCommandSenderWorld().isClientSide) {
            for (int i = 0; i < 32; ++i) {
                double angle = Math.toRadians(this.random.nextFloat() * 360);
                float distance = this.random.nextFloat() * 2;
                double x = distance * Math.cos(angle) + Math.sin(angle);
                double z = distance * Math.sin(angle) - Math.cos(angle);
                this.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL,
                        this.position().x() + x,
                        this.position().y() + (4.0F * this.random.nextFloat()) - 2.0F,
                        this.position().z() + z,
                        0.0D, 0.0D, 0.0D);
            }
            return;
        }
    }

    private void grantStaffSummonAmount() {
        if (this.portalItem == null || this.shootingEntity == null) {
            return;
        }
        this.summonDuration = this.portalItem.getSummonDuration();
        if (this.shootingEntity.getAbilities().instabuild) {
            this.summonAmount += this.portalItem.getSummonAmount();
            return;
        }

        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.shootingEntity);
        if (playerExt == null) {
            return;
        }
        int creatureSummonCost = 4;
        if (this.creatureInfo != null) {
            creatureSummonCost = this.creatureInfo.getSummonCost();
        }
        float summonMultiplier = (float) (creatureSummonCost + this.portalItem.getSummonCostBoost()) * this.portalItem.getSummonCostMod();
        int summonCost = Math.round((float) playerExt.getSummonFocusCharge() * summonMultiplier);
        if (playerExt.hasSummonFocus(summonCost)
                && this.portalItem.getAdditionalCosts(this.shootingEntity)) {
            playerExt.consumeSummonFocus(summonCost);
            this.summonAmount += this.portalItem.getSummonAmount();
        }
    }


    // ==================================================
    //                 Summon Creatures
    // ==================================================
    public int summonCreatures() {
        if (this.getCommandSenderWorld().isClientSide) {
            return 1;
        }
        if (this.summonType == null) {
            return 0;
        }

        int totalSummonAmount = this.summonAmount;
        for (int i = 0; i < totalSummonAmount; i++) {
            Entity entity = this.summonType.create(this.getCommandSenderWorld());
            if (entity == null) {
                return 0;
            }
            entity.moveTo(this.position().x(), this.position().y(), this.position().z(), this.random.nextFloat() * 360.0F, 0.0F);

            if (entity instanceof BaseCreatureEntity) {
                BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;

                // Summoning Staff:
                if (this.shootingEntity != null && this.summoningPedestal == null) {
                    entityCreature.setMinion(true);
                    if (entityCreature instanceof TameableCreatureEntity) {
                        ((TameableCreatureEntity) entityCreature).setPlayerOwner(this.shootingEntity);
                        if (this.portalItem != null) {
                            this.portalItem.applyMinionBehaviour((TameableCreatureEntity) entityCreature, this.shootingEntity);
                            this.portalItem.applyMinionEffects(entityCreature);
                        }
                    }
                }

                // Summoning Pedestal:
                else if (this.summoningPedestal != null && this.summoningPedestal.getOwnerUUID() != null) {
                    entityCreature.setMinion(true);
                    entityCreature.bindSummoningPedestal(this.summoningPedestal);
                    if (entityCreature instanceof TameableCreatureEntity) {
                        ((TameableCreatureEntity) entityCreature).setOwnerId(this.summoningPedestal.getOwnerUUID());
                        this.summoningPedestal.applyMinionBehaviour((TameableCreatureEntity) entityCreature);
                    }
                }

                if (this.summonDuration > 0)
                    entityCreature.setTemporary(this.summonDuration);

                if (this.shootingEntity != null) {
                    //this.shootingEntity.addStat(ObjectManager.getStat(entityCreature.creatureInfo.getName() + ".summon"), 1); TODO Player Stats
                }
            }
            if (this.isAlive()) {
                DeferredLevelActionManager.spawnEntity(this.getCommandSenderWorld(), this.blockPosition(), "portal_summon:" + entity.getUUID(), entity);
            }
        }
        int amount = totalSummonAmount;
        this.summonAmount = 0;
        return amount;
    }


    // ==================================================
    //                   Movement
    // ==================================================
    // ========== Gravity ==========
    @Override
    protected double getDefaultGravity() {
        return 0.0F;
    }

    // ========== Move to Target ==========

    public void moveToTarget() {
        if (this.shootingEntity != null && this.summoningPedestal == null) {
            Vec3 eyePos = this.shootingEntity.getEyePosition(1.0F);
            Vec3 lookDirection = this.shootingEntity.getLookAngle();

            Vec3 desiredEnd = eyePos.add(lookDirection.scale(this.portalRange));

            HitResult hitResult = this.getCommandSenderWorld().clip(
                    new ClipContext(
                            eyePos,
                            desiredEnd,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            this.shootingEntity
                    )
            );

            Vec3 hitPos;
            if (hitResult.getType() != HitResult.Type.MISS) {
                hitPos = hitResult.getLocation();
            } else {
                hitPos = desiredEnd;
            }

            this.targetX = hitPos.x;
            this.targetY = hitPos.y + 1.0D;
            this.targetZ = hitPos.z;

            this.setPos(this.targetX, this.targetY, this.targetZ);
        }
    }

    // ========== Get Coord Behind ==========

    /**
     * Returns the XYZ coordinate in front or behind this entity (using rotation angle) this entity with the given distance, use a negative distance for behind.
     **/
    public double[] getFacingPosition(Entity entity, double distance) {
        double angle = Math.toRadians(this.yRotO);
        double xAmount = -Math.sin(angle);
        double zAmount = Math.cos(angle);
        double[] coords = new double[3];
        coords[0] = entity.position().x() + (distance * xAmount);
        coords[1] = entity.position().y();
        coords[2] = entity.position().z() + (distance * zAmount);
        return coords;
    }


    // ==================================================
    //                     Impact
    // ==================================================
    @Override
    protected void onHit(HitResult movingObjectPos) {
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public ResourceLocation getTexture() {
        ResourceLocation baseTexture = AssetHelper.texture("textures/particles/" + this.entityName.toLowerCase() + ".png");
        ResourceLocation clientTexture = AssetHelper.texture("textures/particles/" + this.entityName.toLowerCase() + "_client.png");
        ResourceLocation playerTexture = AssetHelper.texture("textures/particles/" + this.entityName.toLowerCase() + "_player.png");

        if (this.ownerUUID != null) {
            Player clientPlayer = LycanitesMobs.PROXY.getClientPlayer();
            if (clientPlayer != null && this.ownerUUID.equals(clientPlayer.getUUID())) {
                return clientTexture;
            }
            return playerTexture;
        }
        return baseTexture;
    }

    @Override
    public float getTextureOffsetY() {
        return 0.2F;
    }
}
