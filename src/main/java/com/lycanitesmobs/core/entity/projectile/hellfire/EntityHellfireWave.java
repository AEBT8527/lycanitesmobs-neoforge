package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.creature.demon.EntityRahovart;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityHellfireWave extends BaseProjectileEntity {

    // Properties:
    protected EntityHellfireWall[][] hellfireWalls;
    protected int hellfireWidth = 5;
    protected int hellfireHeight = 3;
    protected int hellfireSize = 10;
    protected int time = 0;
    protected int timeMax = 10 * 20;
    protected float angle = 90;
    protected double rotation = 0;

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityHellfireWave(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellfireWave(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity shooterEntity) {
        super(entityType, world, shooterEntity);
    }

    public EntityHellfireWave(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z) {
        super(entityType, world, x, y, z);
    }

    // ========== Setup Projectile ==========
    public void setup() { // Size 2F
        this.entityName = "hellfirewave";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(0);
        this.setProjectileScale(0F);
        this.movement = false;
        this.ripper = true;
        this.pierceBlocks = true;
        this.projectileLife = 5 * 20;
        this.animationFrameMax = 59;
        this.noPhysics = true;
        this.waterProof = true;
        this.lavaProof = true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }


    // ==================================================
    //                      Update
    // ==================================================
    @Override
    public void tick() {
        if (this.getCommandSenderWorld().isClientSide)
            return;

        // Time Update:
        if (this.time++ >= this.timeMax)
            this.remove(RemovalReason.DISCARDED);

        // Populate:
        if (this.hellfireWalls == null) {
            this.hellfireWalls = new EntityHellfireWall[this.hellfireHeight][this.hellfireWidth];
            for (int row = 0; row < this.hellfireHeight; row++) {
                for (int col = 0; col < this.hellfireWidth; col++) {
                    if (this.getOwner() != null)
                        this.hellfireWalls[row][col] = new EntityHellfireWavePart(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireWavePart.class), this.getCommandSenderWorld(), (LivingEntity) this.getShooter());
                    else
                        this.hellfireWalls[row][col] = new EntityHellfireWavePart(ProjectileManager.getInstance().getOldProjectileType(EntityHellfireWavePart.class), this.getCommandSenderWorld(), this.position().x(), this.position().y() + 5 + (this.hellfireSize * row), this.position().z());
                    this.hellfireWalls[row][col].setPos(
                            this.hellfireWalls[row][col].position().x(),
                            this.position().y() + (this.hellfireSize * row),
                            this.hellfireWalls[row][col].position().z()
                    );
                    this.hellfireWalls[row][col].setProjectileScale(this.hellfireSize * 2);
                    DeferredLevelActionManager.spawnEntity(this.getCommandSenderWorld(), this.hellfireWalls[row][col].blockPosition(), null, this.hellfireWalls[row][col]);
                }
            }
        }

        // Move:
        for (int row = 0; row < this.hellfireHeight; row++) {
            for (int col = 0; col < this.hellfireWidth; col++) {
                double rotationRadians = Math.toRadians(((((float) col / this.hellfireWidth) * this.angle) - (this.angle / 2) + this.rotation) % 360);
                double x = (((float) this.time / this.timeMax) * 200) * Math.cos(rotationRadians) - Math.sin(rotationRadians);
                double z = (((float) this.time / this.timeMax) * 200) * Math.sin(rotationRadians) + Math.cos(rotationRadians);
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
        if (!entityLiving.isInvulnerableTo(entityLiving.level().damageSources().inFire()))
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

    public ResourceLocation getTexture() {
        return AssetHelper.texture("textures/item/" + subTypeString() + "/" + this.entityName + ".png");
    }
}
