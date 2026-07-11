package com.lycanitesmobs.core.event;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockTypeGetter;
import com.lycanitesmobs.core.block.blockentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.block.blockentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.block.blockentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.block.blockentity.TileEntitySummoningPedestal;
import com.lycanitesmobs.core.command.*;
import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.data.info.item.ItemInfo;
import com.lycanitesmobs.core.data.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.entity.base.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.effect.EffectBase;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.manager.*;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.lycanitesmobs.core.util.helpers.LMHelperClass.cast;

public class RegistryEvents {
    private static RegistryEvents INSTANCE;

    public static RegistryEvents getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RegistryEvents();
        return INSTANCE;
    }

    // ==================================================
    //                  Registry Events
    // ==================================================
    // ========== Entities ==========
    @SubscribeEvent
    public void registerEntities(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends EntityType<?>>> entityType : ObjectManager.getEntityTypeEntries()) {
                        helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, entityType.getKey()), entityType.getValue().get());
                    }
                }
        );
    }

    // ========== Blocks ==========

    /**
     * Registers blocks and their corresponding block items.
     * <p>
     * Block items must be registered in the same Registry Event after blocks
     * to ensure that a non-null block is passed to the BlockItem constructor.
     * This sequence guarantees that the block references used in block items
     * are fully initialized and registered, avoiding any potential issues
     * with null references or unregistered blocks.
     * </p>
     *
     * @param event The register event for blocks and items.
     */
    @SubscribeEvent
    public void registerBlocks(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            for (Supplier<? extends Block> block : ObjectManager.getBlockSuppliers()) {
                BlockTypeGetter getter = (BlockTypeGetter) block.get();
                helper.register(getter.getRegistryName(), block.get());
            }
            for (Lazy<? extends LiquidBlock> lazy : ObjectManager.getLiquidBlockSuppliers()) {
                LiquidBlock lb = lazy.get();
                BlockTypeGetter getter = (BlockTypeGetter) lb;
                helper.register(getter.getRegistryName(), lb);
            }
        });
        event.register(Registries.ITEM,
                helper -> {
                    for (Map.Entry<String, Lazy<? extends BlockItem>> entry : ObjectManager.getBlockItemEntries()) {
                        String name = entry.getKey();
                        Supplier<? extends BlockItem> blockItem = entry.getValue();
                        LMHelperClass.logDebug("Item", "Registering item block: " + name);
                        if (name == null) {
                            LMHelperClass.logWarningMessage("Block Item: " + name + " has no Registry Name!");
                            continue;
                        }
                        helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, name), blockItem.get());
                    }
                }
        );
    }

    // ========== Items ==========
    @SubscribeEvent
    public void registerItems(RegisterEvent event) {
        event.register(Registries.ITEM,
                helper -> {
                    //General Items
                    for (ItemInfo itemInfo : ItemManager.getItemInfos()) {
                        if (ObjectManager.hasItem(itemInfo.getName())) {
                            LycanitesMobs.LOGGER.info("Tried registering item in Object Manager's item list, skipping: " + itemInfo.getName());
                            return;
                        }

                        LMHelperClass.logDebug("Item", "Registering general item: " + itemInfo.getName());
                        helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, itemInfo.getItem().itemName), itemInfo.getItem());
                    }
                    //Special Items
                    for (Map.Entry<String, Supplier<? extends Item>> entry : ObjectManager.getItemSupplierEntries()) {
                        String name = entry.getKey();
                        if (ItemManager.hasItemInfo(name)) {
                            LycanitesMobs.LOGGER.info("Tried registering item in Item Manager's item list, skipping: " + name);
                            return;
                        }
                        LMHelperClass.logDebug("Item", "Registering special item: " + name);
                        helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, name), entry.getValue().get());
                    }
                }
        );

    }

    // ========== Sounds ==========

    /**
     * Sounds are for some reason being registered elsewhere(assuming during the process of
     * adding them to the sounds map with SoundEvent.createVariableRangeEvent(resourceLocation)
     * so this is not needed. Kept for future organization.
     *
     * @param event
     */
    /*@SubscribeEvent
    public void registerSounds(RegisterEvent event) {
        event.register(Registries.SOUND_EVENT,
                helper -> {
                    for (Lazy<? extends SoundEvent> soundEvent : sounds.values()) {
                        for (Identifier registryName : soundNames.values()) {
                            if (registryName == null) {
                                LMHelperClass.logWarningMessage("Sound: " + soundEvent + " has no Registry Name!");
                            }
                            helper.register(registryName, soundEvent.get());
                        }
                    }
                }
        );
    }*/

    // ========== Potions ==========
    @SubscribeEvent
    public void registerEffects(RegisterEvent event) {
        event.register(Registries.MOB_EFFECT,
                helper -> {
                    for (EffectBase effect : ObjectManager.getEffects()) {
                        helper.register(effect.getRegistryName(), effect);
                    }
                }
        );
    }

    // ========== Fluids ==========
    @SubscribeEvent
    public void registerFluids(RegisterEvent event) {
        event.register(Registries.FLUID, helper -> {
            FluidManager.getInstance().defineFluids();
            FluidManager.getInstance().registerFluidObjects(helper::register);
        });
        event.register(NeoForgeRegistries.Keys.FLUID_TYPES, helper -> {
            FluidManager.getInstance().defineFluids();
            FluidManager.getInstance().forEachFluidType(helper::register);
        });
    }

    // ========== Tile Entities ==========
    @SubscribeEvent
    public void registerTileEntities(RegisterEvent event) {
        event.register(Registries.BLOCK_ENTITY_TYPE,
                helper -> {
                    BlockEntityType<TileEntitySummoningPedestal> summoningPedestalType = new BlockEntityType<>(TileEntitySummoningPedestal::new,
                            ObjectManager.getBlock("summoningpedestal")
                    );
                    Identifier summoningpedestalLocation = Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, "summoningpedestal");
                    helper.register(summoningpedestalLocation, summoningPedestalType);
                    ObjectManager.registerTileEntityType(TileEntitySummoningPedestal.class, summoningPedestalType);

                    BlockEntityType<TileEntityEquipmentForge> equipmentForgeType = new BlockEntityType<>(TileEntityEquipmentForge::new,
                            ObjectManager.getBlock("equipmentforge_lesser"),
                            ObjectManager.getBlock("equipmentforge_greater"),
                            ObjectManager.getBlock("equipmentforge_master")
                    );
                    Identifier equipmentforgeLocation = Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, "equipmentforge");
                    helper.register(equipmentforgeLocation, equipmentForgeType);
                    ObjectManager.registerTileEntityType(TileEntityEquipmentForge.class, equipmentForgeType);

                    BlockEntityType<EquipmentInfuserTileEntity> equipmentInfuserType = new BlockEntityType<>(EquipmentInfuserTileEntity::new,
                            ObjectManager.getBlock("equipment_infuser")
                    );
                    Identifier equipment_infuserLocation = Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, "equipment_infuser");
                    helper.register(equipment_infuserLocation, equipmentInfuserType);
                    ObjectManager.registerTileEntityType(EquipmentInfuserTileEntity.class, equipmentInfuserType);

                    BlockEntityType<EquipmentStationTileEntity> equipmentStationType = new BlockEntityType<>(EquipmentStationTileEntity::new,
                            ObjectManager.getBlock("equipment_station")
                    );
                    Identifier equipment_stationLocation = Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, "equipment_station");
                    helper.register(equipment_stationLocation, equipmentStationType);
                    ObjectManager.registerTileEntityType(EquipmentStationTileEntity.class, equipmentStationType);
                });
    }


    @SubscribeEvent
    public void registerStructureTypes(RegisterEvent event) {
        event.register(Registries.STRUCTURE_TYPE, helper -> {
            com.lycanitesmobs.core.worldgen.structure.ModStructureTypes.LM_DUNGEON =
                    (net.minecraft.world.level.levelgen.structure.StructureType<com.lycanitesmobs.core.worldgen.structure.LMDungeonStructure>)
                            () -> com.lycanitesmobs.core.worldgen.structure.LMDungeonStructure.CODEC;
            helper.register(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("lm_dungeon"),
                    com.lycanitesmobs.core.worldgen.structure.ModStructureTypes.LM_DUNGEON);
        });
        event.register(Registries.STRUCTURE_PIECE, helper -> {
            com.lycanitesmobs.core.worldgen.structure.ModStructurePieceTypes.LM_DUNGEON_PIECE =
                    (net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.ContextlessType)
                            com.lycanitesmobs.core.worldgen.structure.LMDungeonPiece::new;
            helper.register(com.lycanitesmobs.core.util.helpers.AssetHelper.modResource("lm_dungeon_piece"),
                    com.lycanitesmobs.core.worldgen.structure.ModStructurePieceTypes.LM_DUNGEON_PIECE);
        });
    }

    @SubscribeEvent
    public void registerAttributes(RegisterEvent event) {
        event.register(Registries.ATTRIBUTE,
                helper -> {
                    helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, "defense"), BaseCreatureEntity.DEFENSE);
                    helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, "ranged_speed"), BaseCreatureEntity.RANGED_SPEED);
                }
        );
    }

    // ========== Stats ==========
    @SubscribeEvent
    public void registerStats(RegisterEvent event) {
        StatManager.getInstance().createStatTypes();
        StatManager.getInstance().forEachStatType((name, statType) -> {
            event.register(Registries.STAT_TYPE,
                    helper -> {
                        helper.register(Identifier.fromNamespaceAndPath(LycanitesMobs.MODID, name), statType);
                    }
            );
        });
    }


    // ========== Commands ==========
    public void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("lm")
                        .then(CreaturesCommand.register())
                        .then(BeastiaryCommand.register())
                        .then(SpawnersCommand.register())
                        .then(SpawnerCommand.register())
                        .then(MobEventsCommand.register())
                        .then(MobEventCommand.register())
                        .then(EquipmentCommand.register())
                        .then(DungeonsCommand.register())
                        .then(DebugCommand.register())
        );
    }

    // ==================================================
    //              Entity Attribute Registration
    // ==================================================
    @SubscribeEvent
    public void registerEntityAttributes(EntityAttributeCreationEvent event) {
        CreatureManager cm = CreatureManager.getInstance();
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Identifier loc = entry.getKey().identifier();
            if (!loc.getNamespace().equals(LycanitesMobs.MODID)) continue;
            CreatureInfo creatureInfo = cm.getCreature(loc.getPath());
            if (creatureInfo == null) continue;
            event.put(cast(entry.getValue()), BaseCreatureEntity.registerCustomAttributes().build());
        }
    }

    @SubscribeEvent
    public void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        CreatureManager cm = CreatureManager.getInstance();
        int count = 0;
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            Identifier loc = entry.getKey().identifier();
            if (!loc.getNamespace().equals(LycanitesMobs.MODID)) continue;
            CreatureInfo creatureInfo = cm.getCreature(loc.getPath());
            if (creatureInfo == null) continue;

            event.register(
                    cast(entry.getValue()),
                    SpawnPlacementTypes.NO_RESTRICTIONS,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, level, spawnType, pos, random) -> true,
                    RegisterSpawnPlacementsEvent.Operation.OR
            );
            count++;
        }
        LMHelperClass.logDebug("MobSpawns", "Registered SpawnPlacements for " + count + " creatures.");
    }

    // ==================================================
    //           Debug: Scute Entity Galleries
    // ==================================================

    /**
     * Right-clicking with a Turtle Scute in the main hand spawns one of every projectile.
     * Right-clicking with a Turtle Scute in the offhand spawns one of every creature.
     */
    public void onRightClickTurtleScute(PlayerInteractEvent.RightClickItem event) {
        if (FMLEnvironment.isProduction()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (event.getItemStack().getItem() != Items.TURTLE_SCUTE) return;

        if (event.getHand() == InteractionHand.OFF_HAND) {
            spawnCreatureGallery(serverLevel, event.getEntity());
            event.setCanceled(true);
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var player = event.getEntity();
        List<ProjectileSpawn> projectileSpawns = collectProjectileSpawns(serverLevel, player);
        if (projectileSpawns.isEmpty()) {
            LMHelperClass.logInfoMessageDev("[Debug] No projectiles were available for the scute gallery.");
            return;
        }

        final int columns = Math.max(1, (int) Math.ceil(Math.sqrt(projectileSpawns.size())));
        final double spacing = 5.0D;
        Vec3 forward = horizontalForward(player.getYRot());
        Vec3 right = new Vec3(forward.z(), 0, -forward.x()).normalize();
        Vec3 origin = player.position()
                .add(forward.scale(8.0D))
                .add(right.scale(-((columns - 1) * spacing) / 2.0D))
                .add(0, 1.5D, 0);

        List<TextureCheck> textureChecks = new ArrayList<>();
        int spawned = 0;
        for (int index = 0; index < projectileSpawns.size(); index++) {
            ProjectileSpawn spawn = projectileSpawns.get(index);
            BaseProjectileEntity projectile = spawn.projectile();

            double col = (index % columns) * spacing;
            double row = (index / columns) * spacing;
            Vec3 pos = origin.add(right.scale(col)).add(forward.scale(row));
            prepareProjectileGalleryEntry(projectile, pos, spawn.name());
            textureChecks.add(TextureCheck.from(spawn.name(), spawn.kind(), projectile.getClass().getName(), projectile.getTexture()));

            DeferredLevelActionManager.spawnEntity(serverLevel, projectile.blockPosition(), null, projectile);
            spawned++;
        }

        Path reportPath = writeProjectileTextureReport(textureChecks);
        long missing = textureChecks.stream().filter(TextureCheck::missing).count();
        LMHelperClass.logInfoMessageDev("[Debug] Spawned " + spawned + " projectile gallery entries. Missing textures: " + missing + ". Report: " + reportPath);
        event.setCanceled(true);
    }

    private static void spawnCreatureGallery(ServerLevel serverLevel, Entity player) {
        List<CreatureInfo> creatureInfos = new ArrayList<>(CreatureManager.getInstance().getCreatures());
        creatureInfos.removeIf(creatureInfo -> creatureInfo.isDummy() || creatureInfo.getEntityType() == null);
        creatureInfos.sort(Comparator.comparing(CreatureInfo::getName));
        if (creatureInfos.isEmpty()) {
            LMHelperClass.logInfoMessageDev("[Debug] No creatures were available for the scute gallery.");
            return;
        }

        final int columns = Math.max(1, (int) Math.ceil(Math.sqrt(creatureInfos.size())));
        final double spacing = 6.0D;
        Vec3 forward = horizontalForward(player.getYRot());
        Vec3 right = new Vec3(forward.z(), 0, -forward.x()).normalize();
        Vec3 origin = player.position()
                .add(forward.scale(10.0D))
                .add(right.scale(-((columns - 1) * spacing) / 2.0D));

        int spawned = 0;
        for (int index = 0; index < creatureInfos.size(); index++) {
            CreatureInfo creatureInfo = creatureInfos.get(index);
            LivingEntity creature = creatureInfo.createEntity(serverLevel);
            if (creature == null) {
                continue;
            }

            double col = (index % columns) * spacing;
            double row = (index / columns) * spacing;
            Vec3 pos = origin.add(right.scale(col)).add(forward.scale(row));
            prepareCreatureGalleryEntry(creature, pos, creatureInfo.getName());

            DeferredLevelActionManager.spawnEntity(serverLevel, creature.blockPosition(), null, creature);
            spawned++;
        }

        LMHelperClass.logInfoMessageDev("[Debug] Spawned " + spawned + " creature gallery entries.");
    }

    private static List<ProjectileSpawn> collectProjectileSpawns(ServerLevel level, Entity owner) {
        ProjectileManager projectileManager = ProjectileManager.getInstance();
        List<ProjectileSpawn> projectileSpawns = new ArrayList<>();

        List<ProjectileInfo> projectileInfos = new ArrayList<>(projectileManager.getProjectiles());
        projectileInfos.sort(Comparator.comparing(ProjectileInfo::getName));
        for (ProjectileInfo projectileInfo : projectileInfos) {
            if (!projectileInfo.isEnabled()) {
                continue;
            }
            BaseProjectileEntity projectile = owner instanceof net.minecraft.world.entity.LivingEntity livingEntity
                    ? projectileInfo.createProjectile(level, livingEntity)
                    : projectileInfo.createProjectile(level, 0, 0, 0);
            if (projectile != null) {
                projectileSpawns.add(new ProjectileSpawn(projectileInfo.getName(), "json", projectile));
            }
        }

        addOldProjectileSpawns(projectileSpawns, projectileManager.getOldSpriteProjectileEntries(), projectileManager, level);
        addOldProjectileSpawns(projectileSpawns, projectileManager.getOldModelProjectileEntries(), projectileManager, level);
        projectileSpawns.sort(Comparator.comparing(ProjectileSpawn::kind).thenComparing(ProjectileSpawn::name));
        return projectileSpawns;
    }

    private static void addOldProjectileSpawns(List<ProjectileSpawn> projectileSpawns,
            Iterable<Map.Entry<String, Class<? extends Entity>>> oldProjectiles, ProjectileManager projectileManager, ServerLevel level) {
        for (Map.Entry<String, Class<? extends Entity>> entry : oldProjectiles) {
            Class<? extends Entity> entityClass = entry.getValue();
            if (!BaseProjectileEntity.class.isAssignableFrom(entityClass)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Class<? extends BaseProjectileEntity> projectileClass = (Class<? extends BaseProjectileEntity>) entityClass;
            BaseProjectileEntity projectile = projectileManager.createOldProjectile(projectileClass, level, 0, 0, 0);
            if (projectile != null) {
                projectileSpawns.add(new ProjectileSpawn(entry.getKey(), "old", projectile));
            }
        }
    }

    private static void prepareProjectileGalleryEntry(BaseProjectileEntity projectile, Vec3 pos, String name) {
        projectile.setPos(pos.x(), pos.y(), pos.z());
        projectile.setDeltaMovement(Vec3.ZERO);
        projectile.stopMovement();
        projectile.setNoGravity(true);
        projectile.setProjectileLife(Integer.MAX_VALUE);
        projectile.setCustomName(Component.literal(name));
        projectile.setCustomNameVisible(true);
    }

    private static void prepareCreatureGalleryEntry(LivingEntity creature, Vec3 pos, String name) {
        creature.setPos(pos.x(), pos.y(), pos.z());
        creature.setDeltaMovement(Vec3.ZERO);
        creature.stopRiding();
        creature.setNoGravity(true);
        creature.setCustomName(Component.literal(name));
        creature.setCustomNameVisible(true);
        if (creature instanceof BaseCreatureEntity baseCreature) {
            baseCreature.setPersistenceRequired();
            baseCreature.setNoAi(true);
        }
    }

    private static Vec3 horizontalForward(float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        return new Vec3(-Math.sin(yaw), 0, Math.cos(yaw)).normalize();
    }

    private static Path writeProjectileTextureReport(List<TextureCheck> textureChecks) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path reportDir = gameDir.resolve("lycanites_debug");
        Path reportPath = reportDir.resolve("projectile_texture_report.txt");
        List<String> lines = new ArrayList<>();
        lines.add("Lycanites projectile texture report");
        lines.add("Generated by right-clicking a Turtle Scute in the dev environment.");
        lines.add("");
        for (TextureCheck check : textureChecks) {
            if (!check.missing()) {
                continue;
            }
            lines.add("MISSING " + check.name());
            lines.add("  kind: " + check.kind());
            lines.add("  entity: " + check.entityClass());
            lines.add("  texture: " + check.texture());
            lines.add("  checked: " + check.checkedPath());
            lines.add("");
        }
        if (lines.size() == 3) {
            lines.add("No missing projectile textures were detected from the dev resource tree.");
        }

        lines.add("");
        lines.add("All checked projectiles:");
        for (TextureCheck check : textureChecks) {
            lines.add((check.missing() ? "MISSING " : "OK      ") + check.name() + " -> " + check.texture());
        }

        try {
            Files.createDirectories(reportDir);
            Files.write(reportPath, lines);
        } catch (IOException e) {
            LMHelperClass.logWarningMessage("Unable to write projectile texture report: " + e.getMessage());
        }
        return reportPath.toAbsolutePath().normalize();
    }

    private record ProjectileSpawn(String name, String kind, BaseProjectileEntity projectile) {
    }

    private record TextureCheck(String name, String kind, String entityClass, Identifier texture, Path checkedPath, boolean missing) {
        static TextureCheck from(String name, String kind, String entityClass, Identifier texture) {
            Path checkedPath = null;
            boolean missing = texture == null;
            if (texture != null) {
                checkedPath = FMLPaths.GAMEDIR.get()
                        .resolve("..")
                        .resolve("src")
                        .resolve("main")
                        .resolve("resources")
                        .resolve("assets")
                        .resolve(texture.getNamespace())
                        .resolve(texture.getPath())
                        .normalize();
                missing = !Files.exists(checkedPath);
            }
            return new TextureCheck(name, kind, entityClass, texture, checkedPath, missing);
        }
    }
}
