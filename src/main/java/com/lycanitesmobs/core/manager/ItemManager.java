package com.lycanitesmobs.core.manager;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.info.item.ItemConfig;
import com.lycanitesmobs.core.data.info.item.ItemInfo;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.block.base.BlockBase;
import com.lycanitesmobs.core.block.building.HiveBlock;
import com.lycanitesmobs.core.block.cloud.*;
import com.lycanitesmobs.core.block.fire.*;
import com.lycanitesmobs.core.block.special.BlockEquipmentForge;
import com.lycanitesmobs.core.block.special.BlockSummoningPedestal;
import com.lycanitesmobs.core.block.special.EquipmentInfuserBlock;
import com.lycanitesmobs.core.block.special.EquipmentStationBlock;
import com.lycanitesmobs.core.block.web.BlockFrostweb;
import com.lycanitesmobs.core.block.web.BlockQuickWeb;
import com.lycanitesmobs.core.item.consumable.entity.ChargeItem;
import com.lycanitesmobs.core.item.block.ItemBlockPlacer;
import com.lycanitesmobs.core.item.consumable.holiday.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGift;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGiftLarge;
import com.lycanitesmobs.core.item.consumable.utility.ItemCleansingCrystal;
import com.lycanitesmobs.core.item.consumable.utility.ItemImmunizer;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulkey;
import com.lycanitesmobs.core.item.consumable.utility.ItemSoulstone;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.special.*;
import com.lycanitesmobs.core.item.summoningstaff.*;
import com.lycanitesmobs.core.tabs.*;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.*;

public class ItemManager extends JSONLoader {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LycanitesMobs.MODID);
    // Creative Tabs:
    public static final CreativeModeTab.Builder itemsGroup =
            LMItemsGroup.getBuilder();

    public static final CreativeModeTab.Builder blocksGroup =
            LMBlocksGroup.getBuilder();

    public static final CreativeModeTab.Builder creaturesGroups =
            LMCreaturesGroup.getBuilder();

    public static final CreativeModeTab.Builder equipmentPartsGroup =
            LMEquipmentPartsGroup.getBuilder();

    public static final CreativeModeTab.Builder chargesGroup =
            LMChargesGroup.getBuilder();

    public static final CreativeModeTab.Builder bestEquipmentGroup =
            LMBestEquipmentGroup.getBuilder();

    public static final DeferredHolder<CreativeModeTab, ? extends CreativeModeTab> itemTab = TABS.register(LycanitesMobs.MODID + ".items", itemsGroup::build);
    public static final DeferredHolder<CreativeModeTab, ? extends CreativeModeTab> blockTab = TABS.register(LycanitesMobs.MODID + ".blocks", blocksGroup::build);
    public static final DeferredHolder<CreativeModeTab, ? extends CreativeModeTab> creaturesTab = TABS.register(LycanitesMobs.MODID + ".creatures", creaturesGroups::build);
    public static final DeferredHolder<CreativeModeTab, ? extends CreativeModeTab> chargesTab = TABS.register(LycanitesMobs.MODID + ".charges", chargesGroup::build);
    public static final DeferredHolder<CreativeModeTab, ? extends CreativeModeTab> equipmentPartsTab = TABS.register(LycanitesMobs.MODID + ".equipmentparts", equipmentPartsGroup::build);
    public static final DeferredHolder<CreativeModeTab, ? extends CreativeModeTab> bestEquipmentTab = TABS.register(LycanitesMobs.MODID + ".bestequipment", bestEquipmentGroup::build);
    public static final Map<String, Item.Properties> registryItems = new HashMap<>();
    protected static ItemManager INSTANCE;
    protected static final Map<String, ItemInfo> items = new HashMap<>();

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }

    /**
     * A list of blocks that need to use the cutout renderer.
     **/
    protected final List<Block> cutoutBlocks = new ArrayList<>();
    /**
     * A list of mod groups that have loaded with this manager.
     **/
    protected final List<ModInfo> loadedGroups = new ArrayList<>();
    /**
     * Handles all global item general config settings.
     **/
    protected ItemConfig config;

    /**
     * Returns the main Item Manager instance or creates it and returns it.
     **/
    public static ItemManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemManager();
        }
        return INSTANCE;
    }

    public static Collection<ItemInfo> getItemInfos() {
        return Collections.unmodifiableCollection(items.values());
    }

    public static boolean hasItemInfo(String name) {
        return items.containsKey(name);
    }

    public List<Block> getCutoutBlocks() {
        return Collections.unmodifiableList(this.cutoutBlocks);
    }

    public void registerCutoutBlock(Block block) {
        if (!this.cutoutBlocks.contains(block)) {
            this.cutoutBlocks.add(block);
        }
    }

    /**
     * Called during startup and initially loads everything in this manager.
     *
     * @param modInfo The mod loading this manager.
     */
    public void startup(ModInfo modInfo) {
        loadItems();
        loadAllFromJson(modInfo);
    }

    /**
     * Loads all JSON Items.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        this.rememberLoadedGroup(modInfo);
        this.loadAllJson(modInfo, "Items", "items", "name", true, null, FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Items", "Complete! " + this.items.size() + " JSON Items Loaded In Total.");
    }

    protected void rememberLoadedGroup(ModInfo modInfo) {
        if (!this.loadedGroups.contains(modInfo)) {
            this.loadedGroups.add(modInfo);
        }
    }

    /*private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(BuiltInRegistries.ITEM, LycanitesMobs.MODID);


    public static void registerItem(IEventBus event) {
        ITEM_DEFERRED_REGISTER.register(event);
        ITEM_DEFERRED_REGISTER.register("soulgazer", () -> new ItemSoulgazer(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(new Item.Properties(), "soulgazer")));
    }*/


    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        ItemInfo itemInfo = new ItemInfo(modInfo);
        itemInfo.loadFromJSON(json);
        if (Objects.equals(itemInfo.getName(), "unamed_item")) return;
        this.items.put(itemInfo.getName(), itemInfo);
    }

    /**
     * Called during early start up, loads all global configs into this manager.
     **/
    public void loadConfig() {
        ItemConfig.loadGlobalSettings();
    }


    /**
     * Called during early start up, loads all non-json items.
     **/
    public void loadItems() {
        Item.Properties itemProperties = new Item.Properties();
        Item.Properties itemPropertiesNoStack = new Item.Properties().stacksTo(1);

        ObjectManager.addItem("soulgazer", () -> new ItemSoulgazer(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemPropertiesNoStack, "soulgazer")));
        ObjectManager.addItem("soul_contract", () -> new ItemSoulContract(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemPropertiesNoStack, "soul_contract")));
        ObjectManager.addItem("mobtoken", () -> new ItemMobToken(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(new Item.Properties(), "mobtoken")));
        ObjectManager.addItem("soulstone", () -> new ItemSoulstone(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "soulstone"), null));
        // Equipment Pieces:
        Item.Properties equipmentProperties = new Item.Properties().stacksTo(1);
        ObjectManager.addItem("equipment", () -> new ItemEquipment(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(equipmentProperties, "equipment")));

        // Keys:
        ObjectManager.addItem("soulkey", () -> new ItemSoulkey(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "soulkey"), "soulkey", 0));
        ObjectManager.addItem("soulkeydiamond", () -> new ItemSoulkey(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "soulkeydiamond"), "soulkeydiamond", 1));
        ObjectManager.addItem("soulkeyemerald", () -> new ItemSoulkey(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "soulkeyemerald"), "soulkeyemerald", 2));


        // Buff Items:
        ObjectManager.addItem("immunizer", () -> new ItemImmunizer(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "immunizer")));
        ObjectManager.addItem("cleansingcrystal", () -> new ItemCleansingCrystal(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "cleansingcrystal")));


        // Seasonal Items:
        ObjectManager.addItem("halloweentreat", () -> new ItemHalloweenTreat(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "halloweentreat")));
        ObjectManager.addItem("wintergift", () -> new ItemWinterGift(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "wintergift")));
        ObjectManager.addItem("wintergiftlarge", () -> new ItemWinterGiftLarge(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "wintergiftlarge")));


        // Special:
        ObjectManager.addItem("frostyfur", () -> new ItemBlockPlacer(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "frostyfur"), "frostyfur", "frostcloud"));
        ObjectManager.addItem("poisongland", () -> new ItemBlockPlacer(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "poisongland"), "poisongland", "poisoncloud"));
        ObjectManager.addItem("geistliver", () -> new ItemBlockPlacer(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(itemProperties, "geistliver"), "geistliver", "shadowfire"));

        BlockManager.addDungeonBlocks("lush");
        //if (true) return;
        //.tab(this.itemsGroup)
        // Summoning Staves:
        Item.Properties summoningStaffProperties = new Item.Properties().stacksTo(1).durability(500);
        ObjectManager.addItem("summoningstaff", () -> new ItemStaffSummoning(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(summoningStaffProperties, "summoningstaff"), "summoningstaff", "summoningstaff"));
        ObjectManager.addItem("stablesummoningstaff", () -> new ItemStaffStable(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(summoningStaffProperties, "stablesummoningstaff"), "stablesummoningstaff", "staffstable"));
        ObjectManager.addItem("bloodsummoningstaff", () -> new ItemStaffBlood(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(summoningStaffProperties, "bloodsummoningstaff"), "bloodsummoningstaff", "staffblood"));
        ObjectManager.addItem("sturdysummoningstaff", () -> new ItemStaffSturdy(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(summoningStaffProperties, "sturdysummoningstaff"), "sturdysummoningstaff", "staffsturdy"));
        ObjectManager.addItem("savagesummoningstaff", () -> new ItemStaffSavage(com.lycanitesmobs.core.util.helpers.LMHelperClass.itemId(summoningStaffProperties, "savagesummoningstaff"), "savagesummoningstaff", "staffsavage"));
        // Utilities:
        ObjectManager.addBlock("summoningpedestal", () -> new BlockSummoningPedestal(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5, 10)), false);
        ObjectManager.addBlock("equipmentforge_lesser", () -> new BlockEquipmentForge(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(5, 10), 1), false);
        ObjectManager.addBlock("equipmentforge_greater", () -> new BlockEquipmentForge(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(5, 20), 2), false);
        ObjectManager.addBlock("equipmentforge_master", () -> new BlockEquipmentForge(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5, 1000), 3), false);
        ObjectManager.addBlock("equipment_infuser", () -> new EquipmentInfuserBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5, 1000)), false);
        ObjectManager.addBlock("equipment_station", () -> new EquipmentStationBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5, 1000)), false);

        // Building Blocks:

        BlockManager.addDungeonBlocks("desert");
        BlockManager.addDungeonBlocks("shadow");
        BlockManager.addDungeonBlocks("demon");
        BlockManager.addDungeonBlocks("aberrant");
        BlockManager.addDungeonBlocks("ashen");
        BlockManager.addDungeonBlocks("stream");
        ObjectManager.addBlock("soulcubedemonic", () -> new BlockBase(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubedemonic"), false);
        ObjectManager.addBlock("soulcubeundead", () -> new BlockBase(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubeundead"), false);
        ObjectManager.addBlock("soulcubeaberrant", () -> new BlockBase(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), "soulcubeaberrant"), false);
        ObjectManager.addBlock("propolis", () -> new HiveBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.CLAY).sound(SoundType.WET_GRASS).strength(0.6F).randomTicks(), "propolis"), false);
        ObjectManager.addBlock("veswax", () -> new HiveBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0.6F).randomTicks(), "veswax"), false);


        // Effect Blocks:
        net.minecraft.world.level.block.state.BlockBehaviour.Properties fireProperties = net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).randomTicks().noCollision().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        net.minecraft.world.level.block.state.BlockBehaviour.Properties brightFireProperties = net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).randomTicks().noCollision().dynamicShape().sound(SoundType.WOOL).noOcclusion().lightLevel((BlockState blockState) -> 15);
        ObjectManager.addSound("frostfire", "block.frostfire");
        ObjectManager.addBlock("frostfire", () -> new BlockFrostfire(fireProperties), false);
        ObjectManager.addSound("icefire", "block.icefire");
        ObjectManager.addBlock("icefire", () -> new BlockIcefire(fireProperties), false);
        ObjectManager.addSound("hellfire", "block.hellfire");
        ObjectManager.addBlock("hellfire", () -> new BlockHellfire(brightFireProperties), false);
        ObjectManager.addSound("doomfire", "block.doomfire");
        ObjectManager.addBlock("doomfire", () -> new BlockDoomfire(brightFireProperties), false);
        ObjectManager.addSound("primefire", "block.primefire");
        ObjectManager.addBlock("primefire", () -> new BlockPrimefire(brightFireProperties), false);
        ObjectManager.addSound("scorchfire", "block.scorchfire");
        ObjectManager.addBlock("scorchfire", () -> new BlockScorchfire(brightFireProperties), false);
        ObjectManager.addSound("shadowfire", "block.shadowfire");
        ObjectManager.addBlock("shadowfire", () -> new BlockShadowfire(fireProperties), false);
        ObjectManager.addSound("smitefire", "block.smitefire");
        ObjectManager.addBlock("smitefire", () -> new BlockSmitefire(brightFireProperties), false);

        net.minecraft.world.level.block.state.BlockBehaviour.Properties cloudProperties = net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.NONE).randomTicks().noCollision().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        ObjectManager.addSound("frostcloud", "block.frostcloud");
        ObjectManager.addBlock("frostcloud", () -> new BlockFrostCloud(cloudProperties), false);
        ObjectManager.addSound("poisoncloud", "block.poisoncloud");
        ObjectManager.addBlock("poisoncloud", () -> new BlockPoisonCloud(cloudProperties), false);
        ObjectManager.addSound("poopcloud", "block.poopcloud");
        ObjectManager.addBlock("poopcloud", () -> new BlockPoopCloud(cloudProperties), false);

        net.minecraft.world.level.block.state.BlockBehaviour.Properties webProperties = net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).randomTicks().noCollision().dynamicShape().sound(SoundType.WOOL).noOcclusion();
        ObjectManager.addBlock("quickweb", () -> new BlockQuickWeb(webProperties), false);
        ObjectManager.addBlock("frostweb", () -> new BlockFrostweb(webProperties), false);

        ObjectManager.addDamageType("ooze");
        ObjectManager.addDamageType("acid");
        ObjectManager.addDamageType("pierce");
    }

    /**
     * Determines the Equipment Sharpness repair amount of the provided itemstack. Stack size is not taken into account.
     *
     * @param itemStack The itemstack to check the item and nbt data of.
     * @return The amount of Sharpness the provided itemstack restores.
     */
    public int getEquipmentSharpnessRepair(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        Item item = itemStack.getItem();
        Object locationObj = LMHelperClass.convertObjectToDesired(item, "resourcelocation");
        if (locationObj instanceof Identifier location) {
            if (ItemConfig.getLowEquipmentSharpnessItems() != null) {
                for (String itemId : ItemConfig.getLowEquipmentSharpnessItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemConfig.getLowEquipmentRepairAmount();
                    }
                }
            }
            if (ItemConfig.getMediumEquipmentSharpnessItems() != null) {
                for (String itemId : ItemConfig.getMediumEquipmentSharpnessItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemConfig.getMediumEquipmentRepairAmount();
                    }
                }
            }

            if (ItemConfig.getHighEquipmentSharpnessItems() != null) {
                for (String itemId : ItemConfig.getHighEquipmentSharpnessItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemConfig.getHighEquipmentRepairAmount();
                    }
                }
            }
            if (ItemConfig.getMaxEquipmentSharpnessItems() != null) {
                for (String itemId : ItemConfig.getMaxEquipmentSharpnessItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemEquipment.SHARPNESS_MAX;
                    }
                }
            }

        }
        return 0;
    }

    /**
     * Determines the Equipment Mana repair amount of the provided itemstack. Stack size is not taken into account.
     *
     * @param itemStack The itemstack to check the item and nbt data of.
     * @return The amount of Mana the provided itemstack restores.
     */
    public int getEquipmentManaRepair(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }

        Item item = itemStack.getItem();
        Object locationObj = LMHelperClass.convertObjectToDesired(item, "resourcelocation");
        if (locationObj instanceof Identifier location) {
            if (item instanceof ChargeItem) {
                return ItemConfig.getHighEquipmentRepairAmount();
            }
            if (ItemConfig.getLowEquipmentManaItems() != null) {
                for (String itemId : ItemConfig.getLowEquipmentManaItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemConfig.getLowEquipmentRepairAmount();
                    }
                }
            }
            if (ItemConfig.getMediumEquipmentManaItems() != null) {
                for (String itemId : ItemConfig.getMediumEquipmentManaItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemConfig.getMediumEquipmentRepairAmount();
                    }
                }
            }
            if (ItemConfig.getHighEquipmentManaItems() != null) {
                for (String itemId : ItemConfig.getHighEquipmentManaItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemConfig.getHighEquipmentRepairAmount();
                    }
                }
            }
            if (ItemConfig.getMaxEquipmentManaItems() != null) {
                for (String itemId : ItemConfig.getMaxEquipmentManaItems()) {
                    if (Identifier.parse(itemId).equals(location)) {
                        return ItemEquipment.MANA_MAX;
                    }
                }
            }

        }
        return 0;
    }

}
