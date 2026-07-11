package com.lycanitesmobs.core.data.info.projectile;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.manager.ObjectManager;
import com.lycanitesmobs.core.entity.dispenser.BaseProjectileDispenseBehaviour;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.generic.CustomProjectileModelEntity;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.manager.ElementManager;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.projectile.behaviours.ProjectileBehaviour;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class ProjectileInfo {

    // Core Info:
    /**
     * The name of this projectile. Lowercase, no space, used for language entries and for generating the projectile id, etc. Required.
     **/
    protected String name;

    /**
     * Whether this projectile is enabled or not.
     **/
    protected boolean enabled = true;

    /**
     * The entity class used by this projectile. Defaults to EntityProjectileCustom but can be changed to special classes for unique behaviour, etc.
     **/
    @Nonnull
    protected Class<? extends BaseProjectileEntity> entityClass = CustomProjectileEntity.class;

    /**
     * The constructor used by this projectile to create entity instances.
     **/
    protected Constructor<? extends BaseProjectileEntity> entityConstructor;

    /**
     * The class of the model this projectile should use, loaded client side only.
     **/
    protected String modelClassName;

    /**
     * The group that this projectile belongs to.
     **/
    protected ModInfo modInfo;

    /**
     * The entity type used to store base attributes of this projectile.
     **/
    protected EntityType<? extends BaseProjectileEntity> entityType;

    // Item:
    /**
     * The item used to fire this projectile from a dispenser and on use.
     **/
    protected Lazy<Item> chargeItem;
    /**
     * The name of the charge item for this projectile. Can be automatically generated using the name of this projectile or overridden.
     **/
    protected String chargeItemName;
    /**
     * If true, no charge item is generated for this projectile.
     **/
    protected boolean noChargeItem = false;
    /**
     * The dispenser behaviour used by this projectile. If null, this item cannot be fired from a dispenser.
     **/
    protected BaseProjectileDispenseBehaviour dispenserBehaviour;

    // Stats:
    /**
     * The width of the projectile.
     **/
    protected float width = 0.75F;
    /**
     * The height of the projectile.
     **/
    protected float height = 0.75F;
    /**
     * The scale of the projectile.
     **/
    protected float scale = 0.5F;
    /**
     * How many ticks the projectile is active for.
     **/
    protected int lifetime = 200;
    /**
     * The base amount of damage that this projectile deals.
     **/
    protected int damage = 1;
    /**
     * The base amount of damage caused by this projectile that can ignore armor and similar defenses.
     **/
    protected int pierce = 1;
    /**
     * The chance of this projectile knocking back an entity hit by it.
     **/
    protected double knockbackChance = 0;
    /**
     * How long (in seconds) any element debuffs applied by this projectile last for.
     **/
    protected int effectDuration = 1;
    /**
     * How strong any element debuffs applied by this projectile are.
     **/
    protected int effectAmplifier = 1;
    /**
     * The default velocity that this projectile is launched at.
     **/
    protected double velocity = 1.1D;
    /**
     * How much gravity affects this projectile.
     **/
    protected double weight = 1.0D;
    /**
     * How fast the projectile sprite spins.
     **/
    protected float rollSpeed = 0;

    // Elements:
    /**
     * The Elements of this projectile, affects buffs and debuffs amongst other things.
     **/
    protected List<ElementInfo> elements = new ArrayList<>();
    /**
     * When non-null, overrides element-derived debuffs on impact. An empty list means no debuffs are applied.
     **/
    protected List<String> debuffsOverride = null;

    // Behaviours:
    /**
     * A list of behaviours that this projectile has.
     **/
    protected List<ProjectileBehaviour> behaviours = new ArrayList<>();

    // Flags:
    /**
     * If true, this projectile wont be destroyed when hitting the water or underwater.
     **/
    protected boolean waterproof = false;
    /**
     * If true, this projectile wont be destroyed when hitting the lava or submerged in lava.
     **/
    protected boolean lavaproof = false;
    /**
     * If true, this projectile will destroy long grass and similar blocks.
     **/
    protected boolean cutGrass = false;
    /**
     * If true, this projectile will cut through entities hit.
     **/
    protected boolean ripper = false;
    /**
     * If true, this projectile will cut through blocks hit.
     **/
    protected boolean pierceBlocks = false;
    /**
     * If true, this projectile will play a sound on impact.
     **/
    protected boolean impactSound = false;

    /**
     * How many vertical animation frames the sprite has, set to 1 for no animation.
     **/
    protected int animationFrames = 1;
    /**
     * If true, this projectile glow in the dark.
     **/
    protected boolean glow = false;
    /**
     * If true, this projectile will have a burning effect on it.
     **/
    protected boolean burningEffect = false;
    /**
     * How many particles to create per tick.
     **/
    protected int particleCount = 0;
    /**
     * The vanilla particle to play from this projectile.
     **/
    protected String particleId = null;
    /**
     * The vanilla particle to play from this projectile when in water.
     **/
    protected String waterParticleId = null;
    /**
     * The vanilla particle to play from this projectile.
     **/
    protected SimpleParticleType particleType = null;
    /**
     * The vanilla particle to play from this projectile when in water.
     **/
    protected SimpleParticleType waterParticleType = null;


    /**
     * Constructor
     *
     * @param modInfo The group that this projectile definition will belong to.
     */
    public ProjectileInfo(ModInfo modInfo) {
        this.modInfo = modInfo;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    @Nonnull
    public Class<? extends BaseProjectileEntity> getEntityClass() {
        return this.entityClass;
    }

    public String getModelClassName() {
        return this.modelClassName;
    }

    public boolean hasModelClass() {
        return this.modelClassName != null;
    }

    public boolean hasModel() {
        return this.modelClassName != null;
    }

    public ModInfo getModInfo() {
        return this.modInfo;
    }

    public void bindEntityType(EntityType<? extends BaseProjectileEntity> entityType) {
        this.entityType = entityType;
    }

    public Lazy<Item> getChargeItem() {
        return this.chargeItem;
    }

    public String getChargeItemName() {
        return this.chargeItemName;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getScale() {
        return this.scale;
    }

    public int getDamage() {
        return this.damage;
    }

    public int getPierce() {
        return this.pierce;
    }

    public double getKnockbackChance() {
        return this.knockbackChance;
    }

    public int getEffectDuration() {
        return this.effectDuration;
    }

    public int getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public double getVelocity() {
        return this.velocity;
    }

    public double getWeight() {
        return this.weight;
    }

    public float getRollSpeed() {
        return this.rollSpeed;
    }

    public int getLifetime() {
        return this.lifetime;
    }

    public List<ElementInfo> getElements() {
        return Collections.unmodifiableList(this.elements);
    }

    public List<String> getDebuffsOverride() {
        return this.debuffsOverride;
    }

    public boolean isWaterproof() {
        return this.waterproof;
    }

    public boolean isLavaproof() {
        return this.lavaproof;
    }

    public boolean cutsGrass() {
        return this.cutGrass;
    }

    public int getParticleCount() {
        return this.particleCount;
    }

    public String getParticleId() {
        return this.particleId;
    }

    public String getWaterParticleId() {
        return this.waterParticleId;
    }

    public boolean isRipper() {
        return this.ripper;
    }

    public boolean piercesBlocks() {
        return this.pierceBlocks;
    }

    public int getAnimationFrames() {
        return this.animationFrames;
    }

    public boolean hasBurningEffect() {
        return this.burningEffect;
    }

    public boolean glows() {
        return this.glow;
    }

    public void onProjectileUpdate(BaseProjectileEntity projectile) {
        for (ProjectileBehaviour behaviour : this.behaviours) {
            behaviour.onProjectileUpdate(projectile);
        }
    }

    public boolean canDamage(BaseProjectileEntity projectile, Level world, LivingEntity target, boolean canDamage) {
        boolean result = canDamage;
        for (ProjectileBehaviour behaviour : this.behaviours) {
            if (!behaviour.canDamage(projectile, world, target, result)) {
                result = false;
            }
        }
        return result;
    }

    public void onProjectileDamage(BaseProjectileEntity projectile, Level world, LivingEntity target, float damage) {
        for (ProjectileBehaviour behaviour : this.behaviours) {
            behaviour.onProjectileDamage(projectile, world, target, damage);
        }
    }

    public void onProjectileImpact(BaseProjectileEntity projectile, Level world, net.minecraft.core.BlockPos impactPos) {
        for (ProjectileBehaviour behaviour : this.behaviours) {
            behaviour.onProjectileImpact(projectile, world, impactPos);
        }
    }

    public boolean hasBehaviour(Class<? extends ProjectileBehaviour> behaviourClass) {
        for (ProjectileBehaviour behaviour : this.behaviours) {
            if (behaviourClass.isInstance(behaviour)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Loads this projectile from a JSON object.
     *
     * @return true if loaded successfully, false if this entry should be skipped.
     **/
    public boolean loadFromJSON(JsonObject json) {
        this.name = json.get("name").getAsString();

        if (json.has("chargeItemName")) {
            this.chargeItemName = json.get("chargeItemName").getAsString();
        } else {
            this.chargeItemName = this.name + "charge";
        }

        if (json.has("noChargeItem")) {
            this.noChargeItem = json.get("noChargeItem").getAsBoolean();
        }

        try {
            if (json.has("entityClass")) {
                this.entityClass = (Class<? extends BaseProjectileEntity>) Class.forName(json.get("entityClass").getAsString());
            }
            this.entityConstructor = this.entityClass.getConstructor(EntityType.class, Level.class);
        } catch (Exception e) {
            String entityClassName = json.has("entityClass") ? json.get("entityClass").getAsString() : this.entityClass.getName();
            LMHelperClass.logWarningMessage("[Projectile] Skipping projectile '" + this.getName() + "': unable to find entity class '" + entityClassName + "'. If you have updated the mod, clear your projectile configs at: config/lycanitesmobs/projectiles/" + this.getName() + ".json");
            return false;
        }

        if (json.has("modelClass")) {
            this.modelClassName = json.get("modelClass").getAsString();
        }

        // Size:
        if (json.has("width"))
            this.width = json.get("width").getAsFloat();
        if (json.has("height"))
            this.height = json.get("height").getAsFloat();
        if (json.has("scale"))
            this.scale = json.get("scale").getAsFloat();

        // Stats:
        if (json.has("damage"))
            this.damage = json.get("damage").getAsInt();
        if (json.has("pierce"))
            this.pierce = json.get("pierce").getAsInt();
        if (json.has("knockbackChance"))
            this.knockbackChance = json.get("knockbackChance").getAsDouble();
        if (json.has("effectDuration"))
            this.effectDuration = json.get("effectDuration").getAsInt();
        if (json.has("effectAmplifier"))
            this.effectAmplifier = json.get("effectAmplifier").getAsInt();
        if (json.has("velocity"))
            this.velocity = json.get("velocity").getAsDouble();
        if (json.has("weight"))
            this.weight = json.get("weight").getAsDouble();
        if (json.has("lifetime"))
            this.lifetime = json.get("lifetime").getAsInt();

        // Visual:
        if (json.has("rollSpeed"))
            this.rollSpeed = json.get("rollSpeed").getAsFloat();

        // Elements:
        List<String> elementNames = new ArrayList<>();
        if (json.has("elements")) {
            elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
        }
        this.elements.clear();
        for (String elementName : elementNames) {
            ElementInfo element = ElementManager.getInstance().getElement(elementName);
            if (element == null) {
                LMHelperClass.logWarningMessage("[Projectile] Skipping unknown element '" + elementName + "' for projectile '" + this.getName() + "'. If you have updated the mod, clear your projectile configs at: config/lycanitesmobs/projectiles/" + this.getName() + ".json");
                continue;
            }
            this.elements.add(element);
        }

        if (json.has("debuffs")) {
            this.debuffsOverride = JSONHelper.getJsonStrings(json.get("debuffs").getAsJsonArray());
        } else {
            this.debuffsOverride = null;
        }

        // Behaviours:
        this.behaviours.clear();
        if (json.has("behaviours")) {
            JsonArray jsonArray = json.get("behaviours").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject behaviorJson = jsonIterator.next().getAsJsonObject();
                ProjectileBehaviour projectileBehaviour = ProjectileBehaviour.createFromJSON(behaviorJson);
                if (projectileBehaviour != null) {
                    this.behaviours.add(projectileBehaviour);
                } else {
                    LMHelperClass.logWarningMessage("Unable to load Projectile Behaviour: " + behaviorJson.get("type").getAsString());
                }
            }
        }

        // Flags:
        if (json.has("waterproof"))
            this.waterproof = json.get("waterproof").getAsBoolean();
        if (json.has("lavaproof"))
            this.lavaproof = json.get("lavaproof").getAsBoolean();
        if (json.has("cutGrass"))
            this.cutGrass = json.get("cutGrass").getAsBoolean();
        if (json.has("ripper"))
            this.ripper = json.get("ripper").getAsBoolean();
        if (json.has("pierceBlocks"))
            this.pierceBlocks = json.get("pierceBlocks").getAsBoolean();

        if (json.has("animationFrames"))
            this.animationFrames = json.get("animationFrames").getAsInt();
        if (json.has("impactSound"))
            this.impactSound = json.get("impactSound").getAsBoolean();
        if (json.has("glow"))
            this.glow = json.get("glow").getAsBoolean();
        if (json.has("burningEffect"))
            this.burningEffect = json.get("burningEffect").getAsBoolean();
        if (json.has("particleCount"))
            this.particleCount = json.get("particleCount").getAsInt();
        if (json.has("particleId")) {
            this.particleId = json.get("particleId").getAsString();
        }
        if (json.has("waterParticleId")) {
            this.waterParticleId = json.get("waterParticleId").getAsString();
        }
        return true;
    }

    /**
     * Loads this projectile (should only be called during startup), generates charge items, etc.
     */
    public void load() {
        // Charge Item:
        if (!this.noChargeItem) {
            // NeoForge's Lazy rejects null suppliers, so probe the registry directly first.
            if (ObjectManager.getItem(this.chargeItemName) != null) {
                this.chargeItem = Lazy.of(() -> ObjectManager.getItem(this.chargeItemName));
            } else {
                Item.Properties properties = new Item.Properties();
                //properties.tab(ItemManager.getInstance().chargesGroup);
                this.chargeItem = Lazy.of(() -> new ChargeItem(properties, this));
                ObjectManager.addItem(this.chargeItemName, this.chargeItem);
            }
        }

        // Sounds:
        ObjectManager.addSound(name, "projectile." + name);
        if (this.impactSound) {
            ObjectManager.addSound(name + "_impact", "projectile." + name + ".impact");
        }
    }


    public void initAfterRegistry() {
        if (!this.noChargeItem && this.dispenserBehaviour != null && this.chargeItem != null && this.chargeItem.get() != null) {
            DispenserBlock.registerBehavior(this.chargeItem.get(), this.dispenserBehaviour);
        }
    }

    /**
     * Returns the name of this projectile, this is the unformatted lowercase name. Ex: chaosorb
     *
     * @return Projectile name.
     */
    public String getName() {
        return this.name.toLowerCase();
    }

    /**
     * Returns the registry id of this projectile. Ex: elementalmobs:chaosorb
     *
     * @return Projectile registry entity id.
     */
    public String getEntityId() {
        return this.modInfo.modid + ":" + this.getName();
    }

    public SimpleParticleType getParticleType() {
        if (this.particleCount <= 0 || this.particleId == null) {
            return null;
        }
        if (this.particleType == null) {
            ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(this.particleId));
            if (particleType instanceof SimpleParticleType) {
                this.particleType = (SimpleParticleType) particleType;
            }
        }
        return this.particleType;
    }

    public SimpleParticleType getWaterParticleType() {
        if (this.particleCount <= 0 || this.waterParticleId == null) {
            return null;
        }
        if (this.waterParticleType == null) {
            ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(this.waterParticleId));
            if (particleType instanceof SimpleParticleType) {
                this.waterParticleType = (SimpleParticleType) particleType;
            }
        }
        return this.waterParticleType;
    }


    /**
     * Returns the entity type of this projectile.
     *
     * @return Projectiles's entity type.
     */
    public EntityType getEntityType() {
        this.entityType = (EntityType) BuiltInRegistries.ENTITY_TYPE.get(AssetHelper.modResource(this.getName()));
        return this.entityType;
    }
    /**
     * Returns the resource location for this projectile.
     *
     * @return Projectile resource location.
     */
    public ResourceLocation getResourceLocation() {
        return AssetHelper.resource(this.modInfo.modid, this.getName());
    }

    /**
     * Returns the language key for this projectile. Ex: lycanitesmobs.chaosorb
     *
     * @return Creature language key.
     */
    public String getLocalisationKey() {
        return this.modInfo.modid + "." + this.getName();
    }

    /**
     * Returns a translated title for this projectile. Ex: Chaos Orb
     *
     * @return The display name of this projectile.
     */
    public Component getTitle() {
        return Component.translatable("entity." + this.getLocalisationKey());
    }

    /**
     * Creates a projectile instance using this info.
     *
     * @param world            The world to create the projectile in.
     * @param entityLivingBase The entity that created the projectile.
     */
    public BaseProjectileEntity createProjectile(Level world, LivingEntity entityLivingBase) {
        if (this.modelClassName != null) {
            return new CustomProjectileModelEntity(this.getEntityType(), world, entityLivingBase, this);
        }
        return new CustomProjectileEntity(this.getEntityType(), world, entityLivingBase, this);
    }

    /**
     * Creates a projectile instance using this info.
     *
     * @param world The world to create the projectile in.
     * @param x     The x position of the projectile.
     * @param y     The y position of the projectile.
     * @param z     The z position of the projectile.
     */
    public BaseProjectileEntity createProjectile(Level world, double x, double y, double z) {
        if (this.modelClassName != null) {
            return new CustomProjectileModelEntity(this.getEntityType(), world, x, y, z, this);
        }
        return new CustomProjectileEntity(this.getEntityType(), world, x, y, z, this);
    }

    public BaseProjectileEntity createEntity(EntityType<?> entityType, Level world) {
        try {
            return this.entityConstructor.newInstance(entityType, world);
        } catch (Exception e) {
            LMHelperClass.logWarningMessage("[Projectile] Failed to create entity for " + this.getName() + ": " + e.getMessage());
            return null;
        }
    }

    public SoundEvent getLaunchSound() {
        return ObjectManager.getSound(this.name);
    }

    public SoundEvent getImpactSound() {
        return ObjectManager.getSound(this.name + "_impact");
    }

    /**
     * Returns if this Projectile has the provided element.
     *
     * @param element The element to check for.
     * @return True if this projectile has the element.
     */
    public boolean hasElement(ElementInfo element) {
        return this.elements.contains(element);
    }
}
