package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.demon.EntityRahovart;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityHellfireBarrier extends BaseProjectileEntity {

    // Properties:
    protected EntityHellfireWall[][] hellfireWalls;
    protected int hellfireWidth = 5;
    protected int hellfireHeight = 3;
    protected int hellfireSize = 10;
    protected boolean wall = false;
    protected int time = 0;
    protected int timeMax = 2 * 20;
    protected float angle = 90;
    protected double rotation = 0;

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityHellfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityHellfireBarrier(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }

    // ========== Setup Projectile ==========
    public void setup() { // Size 2F
        this.entityName = "hellfirebarrier";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(0);
        this.setProjectileScale(0F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 5 * 20;
        this.animationFrameMax = 59;
        this.noPhysics = true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public void setWallMode(boolean wall) {
        this.wall = wall;
    }

    public void resetTime() {
        this.time = 0;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        if (this.level().isClientSide())
            return;

        // Time Update:
        if (this.time++ >= this.timeMax)
            this.remove(RemovalReason.DISCARDED);

        // Populate:
        if (this.hellfireWalls == null) {
            hellfireWalls = new EntityHellfireWall[this.hellfireHeight][this.hellfireWidth];
            for (int row = 0; row < this.hellfireHeight; row++) {
                for (int col = 0; col < this.hellfireWidth; col++) {
                    if (this.getOwner() != null) {
                        if (this.wall) {
                            hellfireWalls[row][col] = new EntityHellfireWall(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireWall.class), this.level(), (LivingEntity) this.getShooter());
                        } else {
                            hellfireWalls[row][col] = new EntityHellfireBarrierPart(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireBarrierPart.class), this.level(), (LivingEntity) this.getShooter());
                        }
                    } else {
                        if (this.wall) {
                            hellfireWalls[row][col] = new EntityHellfireWall(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireWall.class), this.level(), this.position().x(), this.position().y() + (this.hellfireSize * row), this.position().z());
                        } else {
                            hellfireWalls[row][col] = new EntityHellfireBarrierPart(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireBarrierPart.class), this.level(), this.position().x(), this.position().y() + (this.hellfireSize * row), this.position().z());
                        }
                    }

                    double rotationRadians = Math.toRadians(this.rotation);
                    double x = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.cos(rotationRadians) + Math.sin(rotationRadians);
                    double z = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.sin(rotationRadians) - Math.cos(rotationRadians);
                    this.hellfireWalls[row][col].setPos(
                            this.position().x() + x,
                            this.position().y() + (this.hellfireSize * row),
                            this.position().z() + z
                    );
                    this.hellfireWalls[row][col].setProjectileLife(2 * 20);
                    this.hellfireWalls[row][col].setProjectileScale(this.hellfireSize * 2.5F);
                    DeferredLevelActionManager.spawnEntity(this.level(), this.hellfireWalls[row][col].blockPosition(), null, this.hellfireWalls[row][col]);
                }
            }
        }

        // Move:
        for (int row = 0; row < this.hellfireHeight; row++) {
            for (int col = 0; col < this.hellfireWidth; col++) {

                double rotationRadians = Math.toRadians(this.rotation);
                double x = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.cos(rotationRadians) + Math.sin(rotationRadians);
                double z = (((float) col / this.hellfireWidth) * (this.hellfireSize * (this.hellfireWidth - 1))) * Math.sin(rotationRadians) - Math.cos(rotationRadians);
                this.hellfireWalls[row][col].setPos(
                        this.position().x() + x,
                        this.position().y() + (this.hellfireSize * row),
                        this.position().z() + z
                );
                this.hellfireWalls[row][col].setProjectileLife(2 * 20);

                if (!this.isAlive())
                    this.hellfireWalls[row][col].remove(RemovalReason.DISCARDED);
            }
        }
    }


    // ==================================================
    //                     Impact
    // ==================================================
    //========== Entity Living Collision ==========
    @Override
    public boolean onEntityLivingDamage(LivingEntity entityLiving) {
        if (!com.lycanitesmobs.core.util.helpers.LMHelperClass.isInvulnerableTo(entityLiving, entityLiving.level().damageSources().onFire()))
            entityLiving.igniteForSeconds(this.getEffectDuration(10) / 20);
        return true;
    }

    //========== Do Damage Check ==========
    public boolean canDamage(LivingEntity targetEntity) {
        LivingEntity owner = (LivingEntity) this.getShooter();
        if (owner == null) {
            if (targetEntity instanceof EntityRahovart)
                return false;
        }
        return super.canDamage(targetEntity);
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound("hellfirewave");
    }


    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness() {
        return 1.0F;
    }

    public int getBrightnessForRender() {
        return 15728880;
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public String subTypeString() {
        return "events";
    }

    public Identifier getTexture() {
        return AssetHelper.texture("textures/particles/" + subTypeString() + "/" + this.entityName + ".png");
    }
}
