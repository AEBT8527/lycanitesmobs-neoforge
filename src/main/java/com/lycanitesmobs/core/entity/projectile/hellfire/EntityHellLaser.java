package com.lycanitesmobs.core.entity.projectile.hellfire;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.projectile.generic.LaserProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityHellLaser extends LaserProjectileEntity {

    // ==================================================
    //                   Constructors
    // ==================================================
    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6, int setTime, int setDelay) {
        super(entityType, world, par2, par4, par6, setTime, setDelay);
    }

    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6) {
        this(entityType, world, par2, par4, par6, 25, 20);
    }

    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world, double par2, double par4, double par6, int setTime, int setDelay, Entity followEntity) {
        super(entityType, world, par2, par4, par6, setTime, setDelay, followEntity);
    }

    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityShooter, int setTime, int setDelay) {
        super(entityType, world, entityShooter, setTime, setDelay);
    }

    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityLiving) {
        this(entityType, world, entityLiving, 25, 20);
    }

    public EntityHellLaser(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity entityShooter, int setTime, int setDelay, Entity followEntity) {
        super(entityType, world, entityShooter, setTime, setDelay, followEntity);
    }

    // ========== Setup Projectile ==========
    public void setup() {
        this.entityName = "helllaser";
        this.modInfo = LycanitesMobs.modInfo;
        this.setDamage(1);
    }

    // ========== Stats ==========
    @Override
    public void setStats() {
        super.setStats();
        this.setRange(16.0F);
        this.setLaserWidth(4.0F);
    }


    // ==================================================
    //                   Get laser End
    // ==================================================
    @Override
    protected LaserEndProjectileEntity createLaserEnd(Level world, double x, double y, double z) {
        return new EntityHellLaserEnd(ProjectileManager.getInstance().getOldProjectileType(EntityHellLaserEnd.class), world, x, y, z, this);
    }

    @Override
    protected LaserEndProjectileEntity createLaserEnd(Level world, LivingEntity shooter) {
        return new EntityHellLaserEnd(ProjectileManager.getInstance().getOldProjectileType(EntityHellLaserEnd.class), world, shooter, this);
    }


    // ==================================================
    //                      Damage
    // ==================================================
    @Override
    public boolean updateDamage(Entity target) {
        boolean damageDealt = super.updateDamage(target);
        if (this.getOwner() != null && damageDealt) {
            if (target instanceof LivingEntity)
                ((LivingEntity) target).addEffect(new MobEffectInstance(MobEffects.WITHER, this.getEffectDuration(5), 0));
        }
        return damageDealt;
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    @Override
    public ResourceLocation getBeamTexture() {
        return AssetHelper.texture("textures/item/" + this.entityName.toLowerCase() + "beam.png");
    }

    @Override
    public String subTypeString() {
        return "events";
    }

    public ResourceLocation getTexture() {
        return AssetHelper.texture("textures/item/" + subTypeString() + "/" + this.entityName + ".png");
    }


    // ==================================================
    //                      Sounds
    // ==================================================
    @Override
    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound(entityName);
    }

    @Override
    public SoundEvent getBeamSound() {
        return ObjectManager.getSound(entityName);
    }
}
