package com.lycanitesmobs.core.manager;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.entity.projectile.generic.ModelProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.RapidFireProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.hellfire.*;
import com.lycanitesmobs.core.entity.projectile.misc.EntityDevilGatling;
import com.lycanitesmobs.core.entity.projectile.misc.EntityShadowfireBarrier;
import com.lycanitesmobs.core.entity.projectile.misc.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.special.PortalEntity;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.util.EntityFactory;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProjectileManager extends JSONLoader {
    protected static ProjectileManager INSTANCE;

    /**
     * A map of all projectiles by name.
     **/
    protected final Map<String, ProjectileInfo> projectiles = new HashMap<>();

    /**
     * A map of old projectile classes that are hardcoded instead of using json definitions that use the default item sprite renderer.
     **/
    protected final Map<String, Class<? extends Entity>> oldSpriteProjectiles = new HashMap<>();

    /**
     * A map of old projectiles that use the obj model renderer. Newer json based projectiles provide their model class in their ProjectileInfo definition instead.
     **/
    protected final Map<String, Class<? extends Entity>> oldModelProjectiles = new HashMap<>();

    /**
     * A map of old projectile classes to types for creating new instances.
     **/
    protected final Map<Class<? extends Entity>, EntityType<? extends BaseProjectileEntity>> oldProjectileTypes = new HashMap<>();

    /**
     * A map of old projectile classes to simple names for translation, etc.
     **/
    protected final Map<Class<? extends Entity>, String> oldProjectileNames = new HashMap<>();

    /**
     * A map of old projectile classes to typed factories for creating instances without reflection.
     **/
    protected final Map<Class<? extends BaseProjectileEntity>, OldProjectileRegistration> oldProjectileRegistrations = new HashMap<>();

    /**
     * Returns the main Projectile Manager instance or creates it and returns it.
     **/
    public static ProjectileManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectileManager();
        }
        return INSTANCE;
    }

    public Collection<ProjectileInfo> getProjectiles() {
        return Collections.unmodifiableCollection(this.projectiles.values());
    }

    public Set<Map.Entry<String, Class<? extends Entity>>> getOldSpriteProjectileEntries() {
        return Collections.unmodifiableMap(this.oldSpriteProjectiles).entrySet();
    }

    public Set<Map.Entry<String, Class<? extends Entity>>> getOldModelProjectileEntries() {
        return Collections.unmodifiableMap(this.oldModelProjectiles).entrySet();
    }

    public EntityType<? extends BaseProjectileEntity> getOldProjectileType(Class<? extends Entity> projectileClass) {
        return this.oldProjectileTypes.get(projectileClass);
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        this.loadAllFromJSON(modInfo);
        for (ProjectileInfo projectileInfo : this.projectiles.values()) {
            projectileInfo.load();
        }
        this.loadOldProjectiles();
        for (ProjectileInfo projectileInfo : this.projectiles.values()) {
            EntityType.Builder<?> b = EntityType.Builder.of((entityType, level) -> projectileInfo.createEntity(entityType, level), MobCategory.MISC)
                    .setTrackingRange(40).setUpdateInterval(3).setShouldReceiveVelocityUpdates(true)
                    .sized(projectileInfo.getWidth(), projectileInfo.getHeight());
            LycanitesMobs.ENTITY_TYPES.register(projectileInfo.getName(), () -> (EntityType<?>) b.build(projectileInfo.getName()));
        }
        for (String projectileInfo : this.oldSpriteProjectiles.keySet()) {
            LycanitesMobs.ENTITY_TYPES.register(projectileInfo, () -> this.createEntityType(projectileInfo, this.oldSpriteProjectiles.get(projectileInfo)));
        }
        for (String projectileInfo : this.oldModelProjectiles.keySet()) {
            LycanitesMobs.ENTITY_TYPES.register(projectileInfo, () -> this.createEntityType(projectileInfo, this.oldModelProjectiles.get(projectileInfo)));
        }
    }


    /**
     * Loads all JSON Creature Types. Should be done before creatures are loaded so that they can find their type on load.
     **/
    public void loadAllFromJSON(ModInfo groupInfo) {
        this.loadAllJson(groupInfo, "Projectile", "projectiles", "name", true, null, FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Projectile", "Complete! " + this.projectiles.size() + " JSON Projectile Info Loaded In Total.");
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        ProjectileInfo projectileInfo = new ProjectileInfo(modInfo);
        if (!projectileInfo.loadFromJSON(json)) {
            return;
        }
        if (projectileInfo.getName() == null) {
            LMHelperClass.logWarningMessage("[Projectile] Unable to load " + loadGroup + " json due to missing name.");
            return;
        }

        // Already Exists:
        if (this.projectiles.containsKey(projectileInfo.getName())) {
            projectileInfo = this.projectiles.get(projectileInfo.getName());
            if (!projectileInfo.loadFromJSON(json)) {
                return;
            }
        }

        this.projectiles.put(projectileInfo.getName(), projectileInfo);
        return;
    }

    public void bindRegisteredTypes() {
        for (ProjectileInfo projectileInfo : this.projectiles.values()) {
            ResourceLocation id = AssetHelper.modResource(projectileInfo.getName());
            EntityType<?> t = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (t == null || !id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(t))) continue;
            EntityType<? extends BaseProjectileEntity> entityType = (EntityType<? extends BaseProjectileEntity>) t;
            projectileInfo.bindEntityType(entityType);
            EntityFactory.getInstance().addEntityType(entityType, (type, level) -> projectileInfo.createEntity(type, level), projectileInfo.getName());
            projectileInfo.initAfterRegistry();
        }
    }


    /**
     * Creates an Entity Type for older projectiles.
     *
     * @param entityName  The projectile name to register with.
     * @param entityClass The projectile entity class to register.
     * @return The projectile's Entity Type.
     */
    public EntityType createEntityType(String entityName, Class<? extends Entity> entityClass) {
        // LMHelperClass.logInfoMessage("EntityClass: " + entityClass + ", EntityName: " + entityName);
        OldProjectileRegistration registration = this.oldProjectileRegistrations.get(entityClass);
        if (registration == null) {
            throw new IllegalStateException("Missing old projectile registration for " + entityClass.getName());
        }

        EntityType.Builder<BaseProjectileEntity> entityTypeBuilder = EntityType.Builder.of(registration::create, MobCategory.MISC);
        entityTypeBuilder.setTrackingRange(40);
        entityTypeBuilder.setUpdateInterval(3);
        entityTypeBuilder.setShouldReceiveVelocityUpdates(true);
        entityTypeBuilder.sized(0.25F, 0.25F);
        EntityType entityType = entityTypeBuilder.build(entityName);

        LMHelperClass.logDebug("Projectile", "Added (Projectile) Entity Type: " + entityName + " Type: " + entityType);
        this.oldProjectileTypes.put(entityClass, entityType);
        return entityType;
    }

    /**
     * Gets a projectile by name.
     *
     * @param projectileName The name of the projectile to get.
     * @return The Projectile Info.
     */
    @Nullable
    public ProjectileInfo getProjectile(String projectileName) {
        if (!this.projectiles.containsKey(projectileName)) {
            return null;
        }
        return this.projectiles.get(projectileName);
    }

    /**
     * Gets a Projectile Entity Type by name.
     *
     * @param projectileName The name of the projectile to get.
     * @return The Entity Type or null.
     */
    @Nullable
    public EntityType<? extends BaseProjectileEntity> getEntityType(String projectileName) {
        ProjectileInfo projectileInfo = this.getProjectile(projectileName);
        if (projectileInfo == null)
            return null;
        return projectileInfo.getEntityType();
    }

    /**
     * Called during early start up, loads all items.
     **/
    public void loadOldProjectiles() {
        this.addOldProjectile("summoningportal", PortalEntity.class,
                (type, world) -> new PortalEntity((EntityType<? extends PortalEntity>) type, world),
                null,
                null);
        this.addOldProjectile("rapidfire", RapidFireProjectileEntity.class,
                RapidFireProjectileEntity::new,
                null,
                null);
        this.addOldProjectile("laserend", LaserEndProjectileEntity.class,
                LaserEndProjectileEntity::new,
                null,
                null);

        this.addOldProjectile("shadowfirebarrier", EntityShadowfireBarrier.class, EntityShadowfireBarrier::new, EntityShadowfireBarrier::new, EntityShadowfireBarrier::new, false);
        this.addOldProjectile("hellfirewall", EntityHellfireWall.class, EntityHellfireWall::new, EntityHellfireWall::new, EntityHellfireWall::new, false);
        this.addOldProjectile("hellfireorb", EntityHellfireOrb.class, EntityHellfireOrb::new, EntityHellfireOrb::new, EntityHellfireOrb::new, false);
        this.addOldProjectile("hellfirewave", EntityHellfireWave.class, EntityHellfireWave::new, EntityHellfireWave::new, EntityHellfireWave::new, false);
        this.addOldProjectile("hellfirewavepart", EntityHellfireWavePart.class, EntityHellfireWavePart::new, EntityHellfireWavePart::new, EntityHellfireWavePart::new, false);
        this.addOldProjectile("hellfirebarrier", EntityHellfireBarrier.class, EntityHellfireBarrier::new, EntityHellfireBarrier::new, EntityHellfireBarrier::new, false);
        this.addOldProjectile("hellfirebarrierpart", EntityHellfireBarrierPart.class, EntityHellfireBarrierPart::new, EntityHellfireBarrierPart::new, EntityHellfireBarrierPart::new, false);
        this.addOldProjectile("devilgatling", EntityDevilGatling.class, EntityDevilGatling::new, EntityDevilGatling::new, EntityDevilGatling::new, false);
        this.addOldProjectile("hellshield", EntityHellShield.class, EntityHellShield::new, EntityHellShield::new, EntityHellShield::new, false);
        this.addOldProjectile("helllaser", EntityHellLaser.class, EntityHellLaser::new, EntityHellLaser::new, EntityHellLaser::new, false);
        this.addOldProjectile("helllaserend", EntityHellLaserEnd.class, EntityHellLaserEnd::new, null, null, false);
    }

    public void addOldProjectile(String name, Class<? extends BaseProjectileEntity> entityClass, OldProjectileBaseFactory baseFactory, OldProjectileOwnerFactory ownerFactory, OldProjectilePositionFactory positionFactory) {
        this.oldProjectileNames.put(entityClass, name);
        this.oldProjectileRegistrations.put(entityClass, new OldProjectileRegistration(entityClass, baseFactory, ownerFactory, positionFactory));
        if (ModelProjectileEntity.class.isAssignableFrom(entityClass)) {
            this.oldModelProjectiles.put(name, entityClass);
            return;
        }
        this.oldSpriteProjectiles.put(name, entityClass);
    }

    public void addOldProjectile(String name, Class<? extends BaseProjectileEntity> entityClass, OldProjectileBaseFactory baseFactory, OldProjectileOwnerFactory ownerFactory, OldProjectilePositionFactory positionFactory, boolean impactSound) {
        ModInfo modInfo = LycanitesMobs.modInfo;
        ObjectManager.addSound(name, "projectile." + name);
        if (impactSound) {
            ObjectManager.addSound(name + "_impact", "projectile." + name + ".impact");
        }
        this.addOldProjectile(name, entityClass, baseFactory, ownerFactory, positionFactory);
    }

    public BaseProjectileEntity createOldProjectile(Class<? extends BaseProjectileEntity> projectileClass, Level world, LivingEntity entity) {
        OldProjectileRegistration registration = this.oldProjectileRegistrations.get(projectileClass);
        if (registration == null || registration.ownerFactory == null) {
            LMHelperClass.logWarning("Projectile", "Unable to create old projectile from owner: " + projectileClass.getName());
            return null;
        }
        return registration.create(world, entity);
    }

    public BaseProjectileEntity createOldProjectile(Class<? extends BaseProjectileEntity> projectileClass, Level world, double x, double y, double z) {
        OldProjectileRegistration registration = this.oldProjectileRegistrations.get(projectileClass);
        if (registration == null || registration.positionFactory == null) {
            LMHelperClass.logWarning("Projectile", "Unable to create old projectile from position: " + projectileClass.getName());
            return null;
        }
        return registration.create(world, x, y, z);
    }

    @FunctionalInterface
    public interface OldProjectileBaseFactory {
        BaseProjectileEntity create(EntityType<? extends BaseProjectileEntity> entityType, Level world);
    }

    @FunctionalInterface
    public interface OldProjectileOwnerFactory {
        BaseProjectileEntity create(EntityType<? extends BaseProjectileEntity> entityType, Level world, LivingEntity owner);
    }

    @FunctionalInterface
    public interface OldProjectilePositionFactory {
        BaseProjectileEntity create(EntityType<? extends BaseProjectileEntity> entityType, Level world, double x, double y, double z);
    }

    protected class OldProjectileRegistration {
        private final Class<? extends BaseProjectileEntity> entityClass;
        private final OldProjectileBaseFactory baseFactory;
        private final OldProjectileOwnerFactory ownerFactory;
        private final OldProjectilePositionFactory positionFactory;

        protected OldProjectileRegistration(Class<? extends BaseProjectileEntity> entityClass, OldProjectileBaseFactory baseFactory, OldProjectileOwnerFactory ownerFactory, OldProjectilePositionFactory positionFactory) {
            this.entityClass = entityClass;
            this.baseFactory = baseFactory;
            this.ownerFactory = ownerFactory;
            this.positionFactory = positionFactory;
        }

        protected BaseProjectileEntity create(EntityType<BaseProjectileEntity> entityType, Level world) {
            return this.baseFactory.create(entityType, world);
        }

        protected BaseProjectileEntity create(Level world, LivingEntity owner) {
            return this.ownerFactory.create(getOldProjectileType(this.entityClass), world, owner);
        }

        protected BaseProjectileEntity create(Level world, double x, double y, double z) {
            return this.positionFactory.create(getOldProjectileType(this.entityClass), world, x, y, z);
        }
    }
}
