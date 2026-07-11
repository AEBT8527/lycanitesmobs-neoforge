package com.lycanitesmobs.core.item.equipment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import com.lycanitesmobs.core.data.info.element.ElementInfo;
import com.lycanitesmobs.core.manager.ElementManager;
import com.lycanitesmobs.core.item.base.BaseItem;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.equipment.features.DamageEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.EffectEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.EquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.HarvestEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.ProjectileEquipmentFeature;
import com.lycanitesmobs.core.item.equipment.features.SlotEquipmentFeature;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ItemEquipmentPart extends BaseItem {
    /**
     * The base amount of experience needed to level up, this is increased by the part's level scaled.
     **/
    protected static final int BASE_LEVELUP_EXPERIENCE = 500;

    /**
     * A map of mob classes and parts that they drop.
     **/
    protected static final Map<String, List<ItemEquipmentPart>> MOB_PART_DROPS = new HashMap<>();

    /**
     * A list of all features this part has.
     **/
    protected List<EquipmentFeature> features = new ArrayList<>();

    /**
     * The Elements of this part, used to determine what charges can be used to upgrade this part along with other future features.
     **/
    protected List<ElementInfo> elements = new ArrayList<>();

    /**
     * Name fragments used by {@link EquipmentNameGenerator} when building dynamic equipment names.
     * Loaded from the "equipmentNames" array in the part's JSON config.
     **/
    protected List<String> equipmentNames = new ArrayList<>();

    /**
     * The slot type that this part must fit into. Can be: base, head, blade, axe, pike or jewel.
     **/
    protected String slotType;

    /**
     * The id of the mob that drops this part.
     **/
    protected String dropMobId;

    /**
     * The default chance of the part being dropped by a mob.
     **/
    protected float dropChance = 1;

    /**
     * The minimum random level that this part can be.
     **/
    protected int levelMin = 1;

    /**
     * The maximum random level that this part can be.
     **/
    protected int levelMax = 3;


    /**
     * Constructor
     */
    public ItemEquipmentPart(Item.Properties properties, JsonObject jsonObject) {
        super(properties);
        loadFromJSON(jsonObject);
    }

    public static String getPartName(JsonObject json) {
        return "equipmentpart_" + json.get("itemName").getAsString();
    }

    public static List<ItemEquipmentPart> getMobPartDrops(String mobId) {
        return MOB_PART_DROPS.containsKey(mobId) ? Collections.unmodifiableList(MOB_PART_DROPS.get(mobId)) : Collections.emptyList();
    }

    public static boolean hasMobPartDrops(String mobId) {
        return MOB_PART_DROPS.containsKey(mobId);
    }

    public List<String> getEquipmentNames() {
        return Collections.unmodifiableList(this.equipmentNames);
    }

    public List<EquipmentFeature> getActiveFeaturesByType(ItemStack itemStack, String featureType) {
        List<EquipmentFeature> activeFeatures = new ArrayList<>();
        int level = this.getPartLevel(itemStack);
        for (EquipmentFeature feature : this.features) {
            if (feature.isActive(itemStack, level) && feature.getFeatureType().equalsIgnoreCase(featureType)) {
                activeFeatures.add(feature);
            }
        }
        return activeFeatures;
    }

    public List<String> getActiveSlotTypes(ItemStack itemStack) {
        List<String> slotTypes = new ArrayList<>();
        int level = this.getPartLevel(itemStack);
        for (EquipmentFeature feature : this.features) {
            if (feature instanceof SlotEquipmentFeature slotFeature && slotFeature.isActive(itemStack, level)) {
                slotTypes.add(slotFeature.getSlotType());
            }
        }
        return slotTypes;
    }

    public FeatureStats getActiveFeatureStats(ItemStack itemStack) {
        int level = this.getPartLevel(itemStack);
        double totalDamage = 0;
        double totalCooldown = 0;
        int damageFeatureCount = 0;
        double totalSweep = 0;
        double totalKnockback = 0;
        double totalRange = 0;
        float pickaxeSpeed = 0;
        float axeSpeed = 0;
        float shovelSpeed = 0;
        int pickaxeLevel = -1;
        int axeLevel = -1;
        int shovelLevel = -1;
        Set<String> harvestTypes = new HashSet<>();
        float totalHarvestSpeed = 0;
        double projectileScore = 0;
        int effectCount = 0;

        for (EquipmentFeature feature : this.features) {
            if (!feature.isActive(itemStack, level)) {
                continue;
            }
            if (feature instanceof DamageEquipmentFeature damageFeature) {
                totalDamage += damageFeature.getDamageAmount();
                totalCooldown += damageFeature.getDamageCooldown();
                damageFeatureCount++;
                totalSweep += damageFeature.getDamageSweep();
                totalKnockback += damageFeature.getDamageKnockback();
                totalRange += damageFeature.getDamageRange();
            } else if (feature instanceof HarvestEquipmentFeature harvestFeature) {
                String harvestType = harvestFeature.getHarvestType().toLowerCase();
                harvestTypes.add(harvestType);
                totalHarvestSpeed += harvestFeature.getHarvestSpeed();
                switch (harvestType) {
                    case "pickaxe" -> {
                        pickaxeSpeed += harvestFeature.getHarvestSpeed();
                        pickaxeLevel = Math.max(pickaxeLevel, harvestFeature.getHarvestLevel());
                    }
                    case "axe" -> {
                        axeSpeed += harvestFeature.getHarvestSpeed();
                        axeLevel = Math.max(axeLevel, harvestFeature.getHarvestLevel());
                    }
                    case "shovel" -> {
                        shovelSpeed += harvestFeature.getHarvestSpeed();
                        shovelLevel = Math.max(shovelLevel, harvestFeature.getHarvestLevel());
                    }
                }
            } else if (feature instanceof ProjectileEquipmentFeature projectileFeature) {
                double cooldown = Math.max(projectileFeature.getCooldown(), 1);
                projectileScore += (projectileFeature.getBonusDamage() + 1.0) * projectileFeature.getCount() / cooldown * 20.0;
            } else if (feature instanceof EffectEquipmentFeature) {
                effectCount++;
            }
        }

        return new FeatureStats(
                totalDamage,
                totalCooldown,
                damageFeatureCount,
                totalSweep,
                totalKnockback,
                totalRange,
                pickaxeSpeed,
                axeSpeed,
                shovelSpeed,
                pickaxeLevel,
                axeLevel,
                shovelLevel,
                Set.copyOf(harvestTypes),
                totalHarvestSpeed,
                projectileScore,
                effectCount);
    }

    public String getSlotType() {
        return this.slotType;
    }

    public float getDropChance() {
        return this.dropChance;
    }

    public int getLevelMax() {
        return this.levelMax;
    }

    public boolean isAtMaxLevel(ItemStack itemStack) {
        return this.getPartLevel(itemStack) >= this.levelMax;
    }

    /**
     * Loads this part from a JSON object.
     **/
    public void loadFromJSON(JsonObject json) {
        this.itemName = "equipmentpart_" + json.get("itemName").getAsString();

        this.slotType = json.get("slotType").getAsString();

        if (json.has("dropMobId")) {
            this.dropMobId = json.get("dropMobId").getAsString();
            if (!"".equals(this.dropMobId)) {
                if (!MOB_PART_DROPS.containsKey(this.dropMobId)) {
                    MOB_PART_DROPS.put(this.dropMobId, new ArrayList<>());
                }
                MOB_PART_DROPS.get(this.dropMobId).add(this);
            }
        }

        if (json.has("dropChance"))
            this.dropChance = json.get("dropChance").getAsFloat();

        if (json.has("levelMin"))
            this.levelMin = json.get("levelMin").getAsInt();

        if (json.has("levelMax"))
            this.levelMax = json.get("levelMax").getAsInt();

        if (json.has("equipmentNames")) {
            this.equipmentNames = JSONHelper.getJsonStrings(json.get("equipmentNames").getAsJsonArray());
        }

        // Elements:
        List<String> elementNames = new ArrayList<>();
        if (json.has("elements")) {
            elementNames = JSONHelper.getJsonStrings(json.get("elements").getAsJsonArray());
        }
        this.elements.clear();
        for (String elementName : elementNames) {
            ElementInfo element = ElementManager.getInstance().getElement(elementName);
            if (element == null) {
                throw new RuntimeException("[Equipment] Unable to initialise Equipment Part " + this.getDescription().getString() + " as the element " + elementName + " cannot be found.");
            }
            this.elements.add(element);
        }

        // Features:
        if (json.has("features")) {
            JsonArray jsonArray = json.get("features").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject featureJson = jsonIterator.next().getAsJsonObject();
                if (!this.addFeature(EquipmentFeature.createFromJSON(featureJson))) {
                    LMHelperClass.logWarningMessage("[Equipment] The feature " + featureJson.toString() + " was unable to be added, check the JSON format.");
                }
            }
        }
        this.sortFeatures();
    }

    @Override
    public Component getName(ItemStack itemStack) {
        MutableComponent displayName = Component.translatable(this.lycDescriptionId().replace("equipmentpart_", ""));
        displayName.append(" ")
                .append(Component.translatable("equipment.level"))
                .append(" " + this.getPartLevel(itemStack));
        return displayName;
    }

        public void appendHoverTextLegacy(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag) {
        if (!LycanitesMobs.PROXY.isShiftKeyDown()) {
            tooltip.add(Component.translatable("item.lycanitesmobs.equipment.shiftinfo").withStyle(ChatFormatting.GREEN));
            return;
        }

        tooltip.add(Component.translatable("item.lycanitesmobs.equipmentpart.description").withStyle(ChatFormatting.DARK_GREEN));

        for (Component description : this.getAdditionalDescriptions(stack, world, flag)) {
            tooltip.add(description);
        }
    }


    @Override
    public Component getDescription(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flag) {
        return Component.translatable("item.lycanitesmobs.equipmentpart.description").withStyle(ChatFormatting.DARK_GREEN);
    }

    public Component getDescription() {
        return Component.translatable("item.lycanitesmobs.equipmentpart.description").withStyle(ChatFormatting.DARK_GREEN);
    }

    public static final Component SEPARATOR = Component.literal("[[SEP]]");

    public List<Component> getAdditionalDescriptions(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext world, TooltipFlag tooltipFlag) {
        List<Component> descriptions = new ArrayList<>();
        int level = this.getPartLevel(itemStack);
        int experience = this.getExperience(itemStack);
        int experienceMax = this.getExperienceForNextLevel(itemStack);

        descriptions.add(SEPARATOR);
        descriptions.add(Component.literal("[[BAR:equipment.sharpness:" + this.getSharpness(itemStack) + ":" + ItemEquipment.SHARPNESS_MAX + "]]"));
        descriptions.add(Component.literal("[[BAR:equipment.mana:" + this.getMana(itemStack) + ":" + ItemEquipment.MANA_MAX + "]]"));

        descriptions.add(SEPARATOR);
        descriptions.add(Component.literal("[[SLOT_TITLE]]"));
        descriptions.add(Component.literal("[[SLOT_ICON:" + this.slotType + "]]"));
        descriptions.add(Component.literal("[[SLOT_ROW:" + this.slotType + ":" + level + ":" + this.levelMax + ":" + experience + ":" + experienceMax + "]]"));

        if (level < this.levelMax) {
        }
        if (!this.elements.isEmpty()) {
            descriptions.add(Component.literal("[[ELEM_TITLE]]"));
            String icons = this.elements.stream()
                    .map(e -> e.getName())
                    .collect(Collectors.joining(","));
            descriptions.add(Component.literal("[[ELEM_ICONS:" + icons + "]]"));
        }

        descriptions.add(SEPARATOR);

        for (EquipmentFeature feature : this.features) {
            Component featureDescription = feature.getDescription(itemStack, level);
            if (featureDescription != null && !"".equals(featureDescription.getString())) {
                descriptions.add(featureDescription);
            }
        }
        return descriptions;
    }


	/*@OnlyIn(Dist.CLIENT)
	@Nullable
	@Override
	public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack) {
		return ClientManager.getInstance().getFontRenderer();
	}*/

    /**
     * Sets up this equipment part, this is called when the provided stack is dropped and needs to have its level randomized, etc.
     **/
    public void randomizeLevel(Level world, ItemStack itemStack) {
        int level = this.levelMax;
        if (this.levelMin < this.levelMax) {
            level = this.levelMin + world.getRandom().nextInt(this.levelMax - this.levelMin + 1);
        }
        this.setLevel(itemStack, level);
    }

    /**
     * Adds a new Feature to this Equipment Part.
     *
     * @return True on success and false on failure.
     **/
    public boolean addFeature(EquipmentFeature feature) {
        if (feature == null) {
            LMHelperClass.logWarningMessage("[Equipment] Unable to add a null feature to " + this);
            return false;
        }
        if (feature.getFeatureType() == null) {
            LMHelperClass.logWarningMessage("[Equipment] Feature type not set for part " + this);
            return false;
        }
        this.features.add(feature);
        return true;
    }

    /**
     * Cycles through all features and organises them.
     **/
    public void sortFeatures() {
        Comparator<EquipmentFeature> comparator = (o1, o2) -> o1.getFeatureType().compareToIgnoreCase(o2.getFeatureType());
        this.features.sort(comparator);
    }

    /**
     * Sets the level of the provided Equipment Item Stack.
     **/
    public void setLevel(ItemStack itemStack, int level) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        nbt.putInt("equipmentLevel", level);
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
    }

    /**
     * Returns an Equipment Part Level for the provided ItemStack.
     **/
    public int getPartLevel(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        int level = 1;
        if (nbt.contains("equipmentLevel")) {
            level = nbt.getIntOr("equipmentLevel", 0);
        }
        return level;
    }

    /**
     * Sets the experience of the provided Equipment Item Stack.
     **/
    public void setExperience(ItemStack itemStack, int experience) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        nbt.putInt("equipmentExperience", experience);
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
    }

    /**
     * Increases the experience of the provided Equipment Item Stack. This will also level up the part if the experience is enough.
     **/
    public void addExperience(ItemStack itemStack, int experience) {
        int currentLevel = this.getPartLevel(itemStack);
        if (currentLevel >= this.levelMax) {
            this.setExperience(itemStack, 0);
        }
        int increasedExperience = this.getExperience(itemStack) + experience;
        int nextLevelExperience = this.getExperienceForNextLevel(itemStack);
        if (increasedExperience >= nextLevelExperience) {
            increasedExperience = increasedExperience - nextLevelExperience;
            this.setLevel(itemStack, currentLevel + 1);
        }
        this.setExperience(itemStack, increasedExperience);
    }

    /**
     * Returns the Equipment Part Experience for the provided ItemStack.
     **/
    public int getExperience(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        int experience = 0;
        if (nbt.contains("equipmentExperience")) {
            experience = nbt.getIntOr("equipmentExperience", 0);
        }
        return experience;
    }

    /**
     * Determines how much experience the part needs in order to level up.
     *
     * @return Experience required for a level up.
     */
    public int getExperienceForNextLevel(ItemStack itemStack) {
        return BASE_LEVELUP_EXPERIENCE + Math.round(BASE_LEVELUP_EXPERIENCE * (this.getPartLevel(itemStack) - 1) * 0.25F);
    }

    /**
     * Determines if the provided itemstack can be consumed to add experience this part.
     *
     * @param itemStack The possible leveling itemstack.
     * @return True if this part should consume the itemstack and gain experience.
     */
    public boolean isLevelingChargeItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ChargeItem) {
            ChargeItem chargeItem = (ChargeItem) itemStack.getItem();
            for (ElementInfo elementInfo : this.elements) {
                if (chargeItem.getElements().contains(elementInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines how much experience the provided charge itemstack can grant this part.
     *
     * @param itemStack The possible leveling itemstack.
     * @return The amount of experience to gain.
     */
    public int getExperienceFromChargeItem(ItemStack itemStack) {
        int experience = 0;
        if (itemStack.getItem() instanceof ChargeItem) {
            ChargeItem chargeItem = (ChargeItem) itemStack.getItem();
            for (ElementInfo elementInfo : this.elements) {
                if (chargeItem.getElements().contains(elementInfo)) {
                    experience += ChargeItem.CHARGE_EXPERIENCE;
                }
            }
        }
        return experience;
    }

    /**
     * Returns the Equipment Part Sharpness for the provided ItemStack.
     **/
    public int getSharpness(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        int sharpness = ItemEquipment.SHARPNESS_MAX;
        if (nbt.contains("equipmentSharpness")) {
            sharpness = nbt.getIntOr("equipmentSharpness", 0);
        }
        return sharpness;
    }

    /**
     * Sets the sharpness of the provided Equipment Item Stack.
     **/
    public void setSharpness(ItemStack itemStack, int sharpness) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        nbt.putInt("equipmentSharpness", Math.max(Math.min(sharpness, ItemEquipment.SHARPNESS_MAX), 0));
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
    }

    /**
     * Increases the sharpness of the provided Equipment Item Stack. This will also level up the part if the sharpness is enough.
     **/
    public boolean addSharpness(ItemStack itemStack, int sharpness) {
        int currentSharpness = this.getSharpness(itemStack);
        if (currentSharpness >= ItemEquipment.SHARPNESS_MAX) {
            return false;
        }
        this.setSharpness(itemStack, currentSharpness + sharpness);
        return true;
    }

    /**
     * Decreases the sharpness of the provided Equipment Item Stack. This will also level up the part if the sharpness is enough.
     **/
    public boolean removeSharpness(ItemStack itemStack, int sharpness) {
        int currentSharpness = this.getSharpness(itemStack);
        if (currentSharpness <= 0) {
            return false;
        }
        this.setSharpness(itemStack, currentSharpness - sharpness);
        return true;
    }

    /**
     * Returns the Equipment Part Mana for the provided ItemStack.
     **/
    public int getMana(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        int mana = ItemEquipment.MANA_MAX;
        if (nbt.contains("equipmentMana")) {
            mana = nbt.getIntOr("equipmentMana", 0);
        }
        return mana;
    }

    /**
     * Sets the mana of the provided Equipment Item Stack.
     **/
    public void setMana(ItemStack itemStack, int mana) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        nbt.putInt("equipmentMana", Math.max(Math.min(mana, ItemEquipment.MANA_MAX), 0));
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
    }

    /**
     * Increases the mana of the provided Equipment Item Stack. This will also level up the part if the mana is enough.
     **/
    public boolean addMana(ItemStack itemStack, int mana) {
        int currentMana = this.getMana(itemStack);
        if (currentMana >= ItemEquipment.MANA_MAX) {
            return false;
        }
        this.setMana(itemStack, currentMana + mana);
        return true;
    }

    /**
     * Decreases the mana of the provided Equipment Item Stack. This will also level up the part if the mana is enough.
     **/
    public boolean removeMana(ItemStack itemStack, int mana) {
        int currentMana = this.getMana(itemStack);
        if (currentMana <= 0) {
            return false;
        }
        this.setMana(itemStack, currentMana - mana);
        return true;
    }

    /**
     * Returns the dyed color for the provided ItemStack.
     **/
    public Vector3d getColor(ItemStack itemStack) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        double r = 1;
        double g = 1;
        double b = 1;
        if (nbt.contains("equipmentColorR")) {
            r = nbt.getFloatOr("equipmentColorR", 1.0F);
        }
        if (nbt.contains("equipmentColorG")) {
            g = nbt.getFloatOr("equipmentColorG", 1.0F);
        }
        if (nbt.contains("equipmentColorB")) {
            b = nbt.getFloatOr("equipmentColorB", 1.0F);
        }
        return new Vector3d(r, g, b);
    }

    /**
     * Set the dyed color for the provided ItemStack.
     **/
    public void setColor(ItemStack itemStack, float red, float green, float blue) {
        CompoundTag nbt = this.getTagCompound(itemStack);
        nbt.putFloat("equipmentColorR", red);
        nbt.putFloat("equipmentColorG", green);
        nbt.putFloat("equipmentColorB", blue);
        itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbt));
    }

   /* @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (!this.allowdedIn(tab)) {
            return;
        }

        for (int level = 1; level <= this.levelMax; level++) {
            ItemStack itemStack = new ItemStack(this, 1);
            this.setLevel(itemStack, level);
            items.add(itemStack);
        }
    }*/

    /**
     * Returns if this Part has the provided element.
     *
     * @param element The element to check for.
     * @return True if this part has the element.
     */
    public boolean hasElement(ElementInfo element) {
        return this.elements.contains(element);
    }

    /**
     * Returns a comma separated list of Elements used by this Part.
     *
     * @return The Elements used by this Part.
     */
    public Component getElementNames() {
        MutableComponent elementNames = Component.literal("");
        boolean firstElement = true;
        for (ElementInfo element : this.elements) {
            if (!firstElement) {
                elementNames.append(", ");
            }
            firstElement = false;
            elementNames.append(element.getTitle());
        }
        return elementNames;
    }

    public record FeatureStats(
            double totalDamage,
            double totalCooldown,
            int damageFeatureCount,
            double totalSweep,
            double totalKnockback,
            double totalRange,
            float pickaxeSpeed,
            float axeSpeed,
            float shovelSpeed,
            int pickaxeLevel,
            int axeLevel,
            int shovelLevel,
            Set<String> harvestTypes,
            float totalHarvestSpeed,
            double projectileScore,
            int effectCount) {
    }
}
