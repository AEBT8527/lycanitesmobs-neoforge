package com.lycanitesmobs;

import net.minecraft.core.registries.Registries;
import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.client.gui.screen.block.EquipmentForgeScreen;
import com.lycanitesmobs.client.gui.screen.block.EquipmentInfuserScreen;
import com.lycanitesmobs.client.gui.screen.block.EquipmentStationScreen;
import com.lycanitesmobs.client.gui.screen.block.SummoningPedestalScreen;
import com.lycanitesmobs.client.network.proxy.ClientProxy;
import com.lycanitesmobs.client.gui.screen.creature.CreatureInventoryScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.data.info.ObjectLists;
import com.lycanitesmobs.core.manager.EffectManager;
import com.lycanitesmobs.core.event.GameEventListener;
import com.lycanitesmobs.core.data.info.altar.AltarInfo;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.event.RegistryEvents;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.client.manager.ModelManager;
import com.lycanitesmobs.client.manager.TextureManager;
import com.lycanitesmobs.core.manager.CommandManager;
import com.lycanitesmobs.client.compatibility.OculusCompat;
import com.lycanitesmobs.core.data.config.CoreConfig;
import com.lycanitesmobs.core.container.block.EquipmentForgeContainer;
import com.lycanitesmobs.core.container.block.EquipmentInfuserContainer;
import com.lycanitesmobs.core.container.block.EquipmentStationContainer;
import com.lycanitesmobs.core.container.block.SummoningPedestalContainer;
import com.lycanitesmobs.core.container.creature.CreatureContainer;
import com.lycanitesmobs.core.manager.DungeonManager;
import com.lycanitesmobs.core.block.Material;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.manager.ProjectileManager;
import com.lycanitesmobs.core.item.consumable.holiday.ItemHalloweenTreat;
import com.lycanitesmobs.core.item.consumable.holiday.ItemWinterGift;
import com.lycanitesmobs.core.manager.EquipmentPartManager;
import com.lycanitesmobs.core.event.MobEventListener;
import com.lycanitesmobs.core.network.proxy.IProxy;
import com.lycanitesmobs.core.network.proxy.ServerProxy;
import com.lycanitesmobs.core.entity.spawner.StructureSpawnInjector;
import com.lycanitesmobs.core.event.SpawnerEventListener;
import com.lycanitesmobs.core.worldgen.data.WorldgenJsonDumper;
import com.lycanitesmobs.core.worldgen.structure.DungeonVirtualPack;
import com.lycanitesmobs.core.worldgen.structure.ModStructureTypes;
import com.lycanitesmobs.core.worldgen.structure.ModStructurePieceTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.lycanitesmobs.core.util.helpers.LMHelperClass.fixMaxHealth;

@Mod(LycanitesMobs.MODID)
public class LycanitesMobs {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "lycanitesmobs";
    public static final String name = "Lycanites Mobs";
    public static final String versionNumber = "2.3.3.4";
    public static final String versionMC = "1.20.1";
    public static final String version = versionNumber + " - MC " + versionMC;
    public static final String website = "https://lycanitesmobs.com";
    public static final String serviceAPI = "https://service.lycanitesmobs.com/api/v1";
    public static final String twitter = "https://twitter.com/Lycanite05";
    public static final String patreon = "https://www.patreon.com/lycanite";
    public static final String guilded = "https://www.guilded.gg/i/jpLvd6J2";
    public static final String discord = "https://discord.gg/bFpV3z4";

    public static final PacketManager PACKET_MANAGER = new PacketManager();

    /** The mod event bus, captured from the @Mod constructor for client-side listener registration. */
    public static IEventBus MOD_EVENT_BUS;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    // TODO: move projectile registries to RegistryEvents
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
            .create(Registries.ENTITY_TYPE, MODID);

    public static EffectManager effectManager;

    public static ModInfo modInfo;
    public static boolean configReady = false;
    public static boolean earlyDebug = false;
    // Proxy:
    public static IProxy PROXY = FMLEnvironment.dist == Dist.CLIENT ? new ClientProxy() : new ServerProxy();

    /**
     * Constructor
     **/

    public LycanitesMobs(IEventBus modEventBus, ModContainer modContainer) {
        MOD_EVENT_BUS = modEventBus;
        modInfo = new ModInfo(this, name, 1000);

        CoreConfig.buildSpec();
        modContainer.registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);

        FileLoader.initAll(modInfo.modid);
        StreamLoader.initAll(modInfo.modid);

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        com.lycanitesmobs.core.capabilities.LycanitesAttachments.ATTACHMENT_TYPES.register(modEventBus);
        WorldGenManager.register(modEventBus);
        ObjectManager.registerContainers(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);
        modEventBus.register(RegistryEvents.getInstance());

        PROXY.registerEvents();
        ItemManager.register(modEventBus);

        NeoForge.EVENT_BUS.register(SpawnerEventListener.getInstance());
        // Game-bus handlers living in the (mod-bus registered) RegistryEvents singleton.
        NeoForge.EVENT_BUS.addListener(RegistryEvents.getInstance()::registerCommands);
        NeoForge.EVENT_BUS.addListener(RegistryEvents.getInstance()::onRightClickTurtleScute);
        NeoForge.EVENT_BUS.register(new GameEventListener());
        NeoForge.EVENT_BUS.register(MobEventManager.getInstance());
        NeoForge.EVENT_BUS.register(MobEventListener.getInstance());
        NeoForge.EVENT_BUS.register(StructureSpawnInjector.getInstance());

        PACKET_MANAGER.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            try {
                Class<?> lib = Class.forName("net.liopyu.dyntopolib.DyntopoLib");
                lib.getMethod("register", IEventBus.class).invoke(null, modEventBus);
                Class<?> shapes = Class.forName("net.liopyu.dyntopolib.LycanitesShapes");
                shapes.getMethod("register").invoke(null);
            } catch (ClassNotFoundException ignored) {
                // dyntopolib not present — editor unavailable, mod runs fine
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("DyntopoLib found but failed to initialize", e);
            }
        }

        this.loadContent();
    }

    public static void registryObjects() {
        // Blocks and Items:
        ItemManager.getInstance().startup(modInfo);
        // Equipment Parts:
        EquipmentPartManager.getInstance().loadAllFromJson(modInfo);
        // Creatures:
        CreatureManager.getInstance().startup(modInfo);
        // Projectiles:
        ProjectileManager.getInstance().startup(modInfo);
    }

    /**
     * Some initialization must be done after registries have been initialized so we
     * do them on common setup
     */
    public static void loadValues() {
        CreatureManager.getInstance().bindRegisteredValues();
        ProjectileManager.getInstance().bindRegisteredTypes();
    }

    // Content Loading:
    public void loadContent() {
        // Elements:
        ElementManager.getInstance().loadAllFromJson(modInfo);

        // Fluids must exist before Forge asks for fluid types, blocks, and buckets.
        FluidManager.getInstance().defineFluids();

        // Vanilla Item Lists:
        ObjectLists.createVanillaLists();

        // Mob Effects:
        effectManager = EffectManager.getInstance();

        // Registry Objects (creatures must load before spawners so MobSpawn can resolve creature IDs):
        LycanitesMobs.registryObjects();

        // Spawners:
        SpawnerManager.getInstance().loadAllFromJson(modInfo);

        // Structure Spawn Injection (replaces tick-based StructureSpawnLocation):
        StructureSpawnInjector.getInstance().loadAllFromJson(modInfo);

        // Altars:
        AltarInfo.createAltars();

        // Mob Events:
        MobEventManager.getInstance().loadAllFromJson(modInfo);

        // Dungeons:
        DungeonManager.getInstance().loadAllFromJson(modInfo);

        // Treat Lists:
        ItemHalloweenTreat.createObjectLists();
        ItemWinterGift.createObjectLists();

        // World Gen:
        WorldGenManager.getInstance().setupFluidFeatures();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        configReady = true;
        ObjectManager.setCurrentModInfo(modInfo);
        this.loadConfigs();
        // Initialize Material block lists
        Material.init();
        // Fix Health Limit:
        fixMaxHealth();
        // Mod Support:
        OculusCompat.init();
        loadValues();

        event.enqueueWork(() -> {
            // Structure (piece) types now register via RegistryEvents.registerStructureTypes.

            WorldGenManager.getInstance().setupFluidFeatures();
            WorldgenJsonDumper.dumpIfDev();
        });
    }

    /**
     * Registers a virtual datapack that dynamically generates worldgen structure,
     * structure_set, and biome tag JSONs from dungeon schematic configs.
     * This replaces the hard-coded JSON files and allows full customization
     * of dungeon biome placement through the schematic condition configs.
     */
    private void addPackFinders(final AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            event.addRepositorySource(consumer -> {
                var locationInfo = new net.minecraft.server.packs.PackLocationInfo(
                        "lycanitesmobs_dynamic_dungeons",
                        Component.literal("Lycanites Mobs Dynamic Dungeons"),
                        PackSource.BUILT_IN,
                        java.util.Optional.empty());
                Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
                    @Override
                    public net.minecraft.server.packs.PackResources openPrimary(net.minecraft.server.packs.PackLocationInfo info) {
                        return new DungeonVirtualPack(info);
                    }

                    @Override
                    public net.minecraft.server.packs.PackResources openFull(net.minecraft.server.packs.PackLocationInfo info, Pack.Metadata metadata) {
                        return new DungeonVirtualPack(info);
                    }
                };
                Pack pack = Pack.readMetaAndCreate(locationInfo, supplier, PackType.SERVER_DATA,
                        new net.minecraft.server.packs.PackSelectionConfig(true, Pack.Position.TOP, false));

                if (pack != null) {
                    consumer.accept(pack);
                }
            });
        }
    }

    public void clientSetup(final FMLClientSetupEvent event) {
        ClientManager.getInstance().initLanguageManager();
        ClientManager.getInstance().registerEvents();
        TextureManager.getInstance().createTextures(modInfo);
        ModelManager.getInstance().createModels();
        ClientManager.getInstance().initBlockRenderTypes();
    }

    public void loadConfigs() {
        ItemManager.getInstance().loadConfig();
        CreatureManager.getInstance().loadConfig();
        MobEventManager.getInstance().loadConfig();
        AltarInfo.loadGlobalSettings();
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // Example code to dispatch IMC to another mod
        // InterModComms.sendTo("modif", "methodname", () -> { LOGGER.info("Hello world
        // from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        // Example code to receive and process InterModComms from other mods
        // LOGGER.info("Got IMC {}",
        // event.getIMCStream().map(m->m.getMessageSupplier().get()).collect(Collectors.toList()));
    }
}
