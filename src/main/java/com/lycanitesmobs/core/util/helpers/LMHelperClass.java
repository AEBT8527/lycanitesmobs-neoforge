package com.lycanitesmobs.core.util.helpers;

import net.minecraft.core.registries.BuiltInRegistries;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.config.ConfigDebug;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.lycanitesmobs.LycanitesMobs.configReady;
import static com.lycanitesmobs.LycanitesMobs.earlyDebug;

public class LMHelperClass {
    // ==================================================
    //          26.1 world-API compatibility helpers
    // ==================================================
    /** isInvulnerableTo now needs a ServerLevel (26.x); false on client where it cannot be checked. */
    public static boolean isInvulnerableTo(net.minecraft.world.entity.Entity entity, net.minecraft.world.damagesource.DamageSource source) {
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel
                && entity instanceof net.minecraft.world.entity.LivingEntity lycLiving) {
            return lycLiving.isInvulnerableTo(serverLevel, source);
        }
        return entity.isInvulnerable();
    }

    /** 26.x: Block/Item Properties must carry their registry id before construction. */
    public static net.minecraft.world.level.block.state.BlockBehaviour.Properties blockId(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties, String name) {
        return properties.setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK,
                Identifier.fromNamespaceAndPath("lycanitesmobs", name.toLowerCase())));
    }

    public static net.minecraft.world.item.Item.Properties itemId(net.minecraft.world.item.Item.Properties properties, String name) {
        return properties.setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM,
                Identifier.fromNamespaceAndPath("lycanitesmobs", name.toLowerCase())));
    }

    /** getCurrentDifficultyAt moved to ServerLevel in 26.x. */
    public static net.minecraft.world.DifficultyInstance getCurrentDifficultyAt(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.getCurrentDifficultyAt(pos);
        }
        return new net.minecraft.world.DifficultyInstance(level.getDifficulty(), level.getGameTime(), 0L, 0.0F);
    }

    /** ContainerHelper now speaks ValueInput/ValueOutput; bridge CompoundTag-based storage. */
    public static void saveAllItems(net.minecraft.nbt.CompoundTag nbt, net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> itemStacks) {
        net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registryAccess());
        net.minecraft.world.ContainerHelper.saveAllItems(out, itemStacks);
        nbt.merge(out.buildResult());
    }

    public static void loadAllItems(net.minecraft.nbt.CompoundTag nbt, net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> itemStacks) {
        net.minecraft.world.level.storage.ValueInput in = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registryAccess(), nbt);
        net.minecraft.world.ContainerHelper.loadAllItems(in, itemStacks);
    }

    /** GameRules moved to ServerLevel in 26.x; client fallback returns the given default. */
    public static boolean getGameRuleBool(net.minecraft.world.level.Level level, net.minecraft.world.level.gamerules.GameRule<Boolean> rule, boolean fallback) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.getGameRules().get(rule);
        }
        return fallback;
    }

    public static int getGameRuleInt(net.minecraft.world.level.Level level, net.minecraft.world.level.gamerules.GameRule<Integer> rule, int fallback) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.getGameRules().get(rule);
        }
        return fallback;
    }

    /** Day time was removed from Level in 26.x; read from ServerLevelData, approximate on client. */
    public static long getDayTime(net.minecraft.world.level.Level level) {
        if (level.getLevelData() instanceof net.minecraft.world.level.storage.ServerLevelData serverLevelData) {
            return (long) (serverLevelData.getDayTimeFraction() * 24000.0F);
        }
        return level.getGameTime() % 24000L;
    }

    public static final Set<String> errorMessagesLogged = new HashSet<>();
    public static final Set<String> warningMessagesLogged = new HashSet<>();
    public static final Set<String> infoMessagesLogged = new HashSet<>();
    public static Logger LOGGER = LycanitesMobs.LOGGER;

    /**
     * Prints an info message into the console.
     *
     * @param key     The debug config key to use. Use {@link #logInfoMessage(String)} for unconditional output.
     * @param message The message to print.
     */
    public static void logInfo(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.info("[LycanitesMobs] [Info] [" + key + "] " + message);
        }
    }

    /**
     * Prints an info debug into the console.
     *
     * @param key     The debug config key to use. Operational notices should use {@link #logInfoMessage(String)}.
     * @param message The message to print.
     */
    public static void logDebug(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            // Explicitly enabled debug channels log at INFO so they show on production servers too.
            LOGGER.info("[LycanitesMobs] [Debug] [" + key + "] " + message);
        }
    }

    /**
     * Prints an info warning into the console.
     *
     * @param key     The debug config key to use. Use {@link #logWarningMessage(String)} for unconditional output.
     * @param message The message to print.
     */
    public static void logWarning(String key, String message) {
        if ("".equals(key) || (!configReady && earlyDebug) || ConfigDebug.INSTANCE.isEnabled(key.toLowerCase())) {
            LOGGER.warn("[LycanitesMobs] [WARNING] [" + key + "] " + message);
        }
    }

    /**
     * Prints an error message into the console.
     *
     * @param message The error message to print.
     */
    public static void logError(String message) {
        LOGGER.error("[LycanitesMobs] " + message);
    }

    public static void logErrorMessageOnce(String errorMessage) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            LOGGER.error("[Lycanites]: " + errorMessage);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logErrorMessage(String errorMessage) {
        LOGGER.error("[Lycanites]: " + errorMessage);
    }

    public static void logWarningMessageOnce(String errorMessage) {
        if (!warningMessagesLogged.contains(errorMessage)) {
            LOGGER.warn("[Lycanites]: " + errorMessage);
            warningMessagesLogged.add(errorMessage);
        }
    }

    public static void logWarningMessageOnceDev(String errorMessage) {
        if (!FMLEnvironment.isProduction() && !warningMessagesLogged.contains(errorMessage)) {
            LOGGER.warn("[Lycanites]: " + errorMessage);
            warningMessagesLogged.add(errorMessage);
        }
    }

    public static void logWarningMessage(String errorMessage) {
        LOGGER.warn("[Lycanites]: " + errorMessage);
    }

    public static void logErrorMessageCatchable(String errorMessage, Throwable e) {
        LOGGER.error("[Lycanites]: " + errorMessage, e);
    }

    public static void logErrorMessageOnceCatchable(String errorMessage, Throwable e) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            LOGGER.error("[Lycanites]: " + errorMessage, e);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logInfoMessageOnce(String info) {
        if (!infoMessagesLogged.contains(info)) {
            LOGGER.info("[Lycanites]: " + info);
            infoMessagesLogged.add(info);
        }
    }

    public static void logInfoMessageOnceDev(String info) {
        if (!FMLEnvironment.isProduction() && !infoMessagesLogged.contains(info)) {
            LOGGER.info("[Lycanites]: " + info);
            infoMessagesLogged.add(info);
        }
    }

    public static void logInfoMessage(String info) {
        LOGGER.info("[Lycanites]: " + info);
    }

    public static void logInfoMessageDev(String info) {
        if (!FMLEnvironment.isProduction()) {
            LOGGER.info("[Lycanites]: " + info);
        }
    }

    public static Object convertObjectToDesired(Object input, String outputType) {
        return switch (outputType.toLowerCase()) {
            case "integer" -> convertToInteger(input);
            case "double" -> convertToDouble(input);
            case "float" -> convertToFloat(input);
            case "boolean" -> convertToBoolean(input);
            case "resourcelocation" -> convertToResourceLocation(input);
            case "vec3i" -> convertToVec3i(input);
            default -> input;
        };
    }

    public static Vector3d convertToVec3d(Object input) {
        if (input instanceof Vec3 v) {

            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3f v) {
            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3d v) {
            return new Vector3d(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }
        return null;
    }

    public static Vec3 convertToVec3(Object input) {
        if (input instanceof Vector3i v) {
            return new Vec3(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3f v) {
            return new Vec3(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        } else if (input instanceof Vector3d v) {
            return new Vec3(convertToDouble(v.x), convertToDouble(v.y), convertToDouble(v.z));
        }
        return null;
    }

    public static Vec3i convertToVec3i(Object input) {
        if (input instanceof Vec3 v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        } else if (input instanceof Vector3f v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        } else if (input instanceof Vector3d v) {
            return new Vec3i(convertToInteger(v.x), convertToInteger(v.y), convertToInteger(v.z));
        }
        return null;
    }

    /**
     * Best-effort registry access for contexts without a Level (e.g. pure ItemStack methods).
     * Server-side uses the running server; client-side falls back to the loaded client level.
     */
    public static net.minecraft.core.RegistryAccess registryAccess() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess();
        }
        Level proxyLevel = com.lycanitesmobs.LycanitesMobs.PROXY.getWorld();
        return proxyLevel != null ? proxyLevel.registryAccess() : null;
    }

    public static Identifier convertToResourceLocation(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof Identifier) {
            return (Identifier) input;
        } else if (input instanceof String) {
            return Identifier.parse((String) input);
        } else if (input instanceof Item item) {
            return BuiltInRegistries.ITEM.getKey(item);
        } else if (input instanceof Block block) {
            return BuiltInRegistries.BLOCK.getKey(block);
        } else if (input instanceof EntityType<?> entityType) {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        } else if (input instanceof Biome) {
            throw new IllegalArgumentException("Biome cannot be gotten from ForgeRegistries anymore - please use LMHelperClass.convertBiomeToResourceLocation(level, biome) to get from Level");
        } else if (input instanceof Fluid fluid) {
            return BuiltInRegistries.FLUID.getKey(fluid);
        } else if (input instanceof MobEffect mobEffect) {
            return BuiltInRegistries.MOB_EFFECT.getKey(mobEffect);
        } else if (input instanceof SoundEvent soundEvent) {
            return BuiltInRegistries.SOUND_EVENT.getKey(soundEvent);
        } else if (input instanceof Potion potion) {
            return BuiltInRegistries.POTION.getKey(potion);
        } else if (input instanceof Enchantment enchantment) {
            // Enchantments are a datapack (dynamic) registry in 1.21 and can't be resolved without a
            // RegistryAccess. Callers that need enchantment ids must use the (Object, RegistryAccess) overload.
            throw new IllegalArgumentException("Enchantment id cannot be resolved without a RegistryAccess - use convertToResourceLocation(input, access).");
        } else if (input instanceof BlockEntityType<?> blockEntityType) {
            return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
        } else if (input instanceof ParticleType<?> particleType) {
            return BuiltInRegistries.PARTICLE_TYPE.getKey(particleType);
        } else if (input instanceof MenuType<?> menuType) {
            return BuiltInRegistries.MENU.getKey(menuType);
        } else if (input instanceof PaintingVariant paintingVariant) {
            // 1.21: painting variants are a datapack registry; resolve via current registry access.
            var access = registryAccess();
            return access == null ? null : access.lookupOrThrow(Registries.PAINTING_VARIANT).getKey(paintingVariant);
        } else if (input instanceof RecipeType<?> recipeType) {
            return BuiltInRegistries.RECIPE_TYPE.getKey(recipeType);
        } else if (input instanceof RecipeSerializer<?> recipeSerializer) {
            return BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipeSerializer);
        } else if (input instanceof Attribute attribute) {
            return BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        } else if (input instanceof StatType<?> statType) {
            return BuiltInRegistries.STAT_TYPE.getKey(statType);
        } else if (input instanceof ArgumentTypeInfo<?, ?> argumentTypeInfo) {
            return BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(argumentTypeInfo);
        } else if (input instanceof VillagerProfession villagerProfession) {
            return BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerProfession);
        } else if (input instanceof PoiType poiType) {
            return BuiltInRegistries.POINT_OF_INTEREST_TYPE.getKey(poiType);
        } else if (input instanceof MemoryModuleType<?> memoryModuleType) {
            return BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryModuleType);
        } else if (input instanceof SensorType<?> sensorType) {
            return BuiltInRegistries.SENSOR_TYPE.getKey(sensorType);
                } else if (input instanceof Activity activity) {
            return BuiltInRegistries.ACTIVITY.getKey(activity);
        } else if (input instanceof WorldCarver<?> worldCarver) {
            return BuiltInRegistries.CARVER.getKey(worldCarver);
        } else if (input instanceof Feature<?> feature) {
            return BuiltInRegistries.FEATURE.getKey(feature);
        } else if (input instanceof ChunkStatus chunkStatus) {
            return BuiltInRegistries.CHUNK_STATUS.getKey(chunkStatus);
        } else if (input instanceof BlockStateProviderType<?> blockStateProviderType) {
            return BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE.getKey(blockStateProviderType);
        } else if (input instanceof FoliagePlacerType<?> foliagePlacerType) {
            return BuiltInRegistries.FOLIAGE_PLACER_TYPE.getKey(foliagePlacerType);
        } else if (input instanceof TreeDecoratorType<?> treeDecoratorType) {
            return BuiltInRegistries.TREE_DECORATOR_TYPE.getKey(treeDecoratorType);
        }

        return null;
    }

    public static Identifier convertToResourceLocation(Object input, RegistryAccess access) {
        if (input == null) {
            return null;
        }
        if (input instanceof Identifier) {
            return (Identifier) input;
        } else if (input instanceof String) {
            return Identifier.parse((String) input);
        } else if (input instanceof Item item) {
            return access.lookupOrThrow(Registries.ITEM).getKey(item);
        } else if (input instanceof Block block) {
            return access.lookupOrThrow(Registries.BLOCK).getKey(block);
        } else if (input instanceof EntityType<?> entityType) {
            return access.lookupOrThrow(Registries.ENTITY_TYPE).getKey(entityType);
        } else if (input instanceof Biome biome) {
            return access.lookupOrThrow(Registries.BIOME).getKey(biome);
        } else if (input instanceof Fluid fluid) {
            return access.lookupOrThrow(Registries.FLUID).getKey(fluid);
        } else if (input instanceof MobEffect mobEffect) {
            return access.lookupOrThrow(Registries.MOB_EFFECT).getKey(mobEffect);
        } else if (input instanceof SoundEvent soundEvent) {
            return access.lookupOrThrow(Registries.SOUND_EVENT).getKey(soundEvent);
        } else if (input instanceof Potion potion) {
            return access.lookupOrThrow(Registries.POTION).getKey(potion);
        } else if (input instanceof Enchantment enchantment) {
            return access.lookupOrThrow(Registries.ENCHANTMENT).getKey(enchantment);
        } else if (input instanceof BlockEntityType<?> blockEntityType) {
            return access.lookupOrThrow(Registries.BLOCK_ENTITY_TYPE).getKey(blockEntityType);
        } else if (input instanceof ParticleType<?> particleType) {
            return access.lookupOrThrow(Registries.PARTICLE_TYPE).getKey(particleType);
        } else if (input instanceof MenuType<?> menuType) {
            return access.lookupOrThrow(Registries.MENU).getKey(menuType);
        } else if (input instanceof PaintingVariant paintingVariant) {
            return access.lookupOrThrow(Registries.PAINTING_VARIANT).getKey(paintingVariant);
        } else if (input instanceof RecipeType<?> recipeType) {
            return access.lookupOrThrow(Registries.RECIPE_TYPE).getKey(recipeType);
        } else if (input instanceof RecipeSerializer<?> recipeSerializer) {
            return access.lookupOrThrow(Registries.RECIPE_SERIALIZER).getKey(recipeSerializer);
        } else if (input instanceof Attribute attribute) {
            return access.lookupOrThrow(Registries.ATTRIBUTE).getKey(attribute);
        } else if (input instanceof StatType<?> statType) {
            return access.lookupOrThrow(Registries.STAT_TYPE).getKey(statType);
        } else if (input instanceof ArgumentTypeInfo<?, ?> argumentTypeInfo) {
            return access.lookupOrThrow(Registries.COMMAND_ARGUMENT_TYPE).getKey(argumentTypeInfo);
        } else if (input instanceof VillagerProfession villagerProfession) {
            return access.lookupOrThrow(Registries.VILLAGER_PROFESSION).getKey(villagerProfession);
        } else if (input instanceof PoiType poiType) {
            return access.lookupOrThrow(Registries.POINT_OF_INTEREST_TYPE).getKey(poiType);
        } else if (input instanceof MemoryModuleType<?> memoryModuleType) {
            return access.lookupOrThrow(Registries.MEMORY_MODULE_TYPE).getKey(memoryModuleType);
        } else if (input instanceof SensorType<?> sensorType) {
            return access.lookupOrThrow(Registries.SENSOR_TYPE).getKey(sensorType);
                } else if (input instanceof Activity activity) {
            return access.lookupOrThrow(Registries.ACTIVITY).getKey(activity);
        } else if (input instanceof WorldCarver<?> worldCarver) {
            return access.lookupOrThrow(Registries.CARVER).getKey(worldCarver);
        } else if (input instanceof Feature<?> feature) {
            return access.lookupOrThrow(Registries.FEATURE).getKey(feature);
        } else if (input instanceof ChunkStatus chunkStatus) {
            return access.lookupOrThrow(Registries.CHUNK_STATUS).getKey(chunkStatus);
        } else if (input instanceof BlockStateProviderType<?> blockStateProviderType) {
            return access.lookupOrThrow(Registries.BLOCK_STATE_PROVIDER_TYPE).getKey(blockStateProviderType);
        } else if (input instanceof FoliagePlacerType<?> foliagePlacerType) {
            return access.lookupOrThrow(Registries.FOLIAGE_PLACER_TYPE).getKey(foliagePlacerType);
        } else if (input instanceof TreeDecoratorType<?> treeDecoratorType) {
            return access.lookupOrThrow(Registries.TREE_DECORATOR_TYPE).getKey(treeDecoratorType);
        }

        return null;
    }

    public static Boolean convertToBoolean(Object input) {
        if (input instanceof Boolean) {
            return (Boolean) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toLowerCase();
            if ("true".equals(stringValue)) {
                return true;
            } else if ("false".equals(stringValue)) {
                return false;
            }
        }
        return null;
    }


    public static Integer convertToInteger(Object input) {
        if (input instanceof Number) {
            return ((Number) input).intValue();
        } else {
            return null;
        }
    }

    public static Double convertToDouble(Object input) {
        if (input instanceof Number) {
            return ((Number) input).doubleValue();
        } else {
            return null;
        }
    }

    public static Float convertToFloat(Object input) {
        if (input instanceof Number) {
            return ((Number) input).floatValue();
        } else {
            return null;
        }
    }

    public static DamageSource damageSourceFromResourceKey(Level level, ResourceKey<DamageType> damageTypeResourceKey) {
        ResourceKey<Registry<DamageType>> damageTypeRegistryKey = ResourceKey.createRegistryKey(Identifier.parse("damage_type"));
        Registry<DamageType> damageTypeRegistry = level.registryAccess().lookupOrThrow(damageTypeRegistryKey);
        Holder<DamageType> holder = damageTypeRegistry.getOrThrow(damageTypeResourceKey);
        return new DamageSource(holder);
    }

    public static double wrapDegrees(double p_76138_0_) {
        double lvt_2_1_ = p_76138_0_ % 360.0;
        if (lvt_2_1_ >= 180.0) {
            lvt_2_1_ -= 360.0;
        }
        if (lvt_2_1_ < -180.0) {
            lvt_2_1_ += 360.0;
        }
        return lvt_2_1_;
    }

    public static double clamp(double p_151237_0_, double p_151237_2_, double p_151237_4_) {
        if (p_151237_0_ < p_151237_2_) {
            return p_151237_2_;
        } else {
            return Math.min(p_151237_0_, p_151237_4_);
        }
    }

    public static boolean hasTag(Object obj, Object tag) {
        if (obj instanceof BlockState block) {
            if (tag instanceof TagKey<?> tagKey) {
                return net.minecraft.core.registries.BuiltInRegistries.BLOCK.wrapAsHolder(block.getBlock()).tags().anyMatch(blockTagKey -> blockTagKey == tagKey);
            } else if (tag instanceof String string) {
                return net.minecraft.core.registries.BuiltInRegistries.BLOCK.wrapAsHolder(block.getBlock()).tags().anyMatch(blockTagKey -> blockTagKey.location().equals(Identifier.parse(string)));
            }
        }
        return false;
    }

    public static float getBrightness(Entity entity) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(entity.getX(), 0.0, entity.getZ());
        if (entity.level().hasChunkAt(blockpos$mutable)) {
            blockpos$mutable.setY((int) Math.floor(entity.getEyeY()));
            return entity.level().getBrightness(LightLayer.BLOCK, blockpos$mutable);
        } else {
            return 0.0F;
        }
    }

    public static <T> T cast(Object o) {
        return (T) o;
    }

    /**
     * Runs the provided Runnable if the current environment matches the specified environment.
     *
     * @param prodEnvironment If true, run the code in a production environment. If false, run the code in a development environment.
     * @param runnable        The code to run in the specified environment.
     */
    public static void environmentRunnable(boolean prodEnvironment, Runnable runnable) {
        if (prodEnvironment && FMLEnvironment.isProduction()) {
            runnable.run();
        } else if (!FMLEnvironment.isProduction()) {
            runnable.run();
        }
    }

    public static class Materials {
        public static boolean isLeaves(Block block) {
            return hasTag(block, BlockTags.LEAVES);
        }

        public static boolean isDirt(Block block) {
            return hasTag(block, BlockTags.DIRT);
        }

        public static boolean isCoral(Block block) {
            return hasTag(block, BlockTags.CORAL_PLANTS);
        }

        public static boolean isCrop(Block block) {
            return hasTag(block, BlockTags.CROPS);
        }

        public static boolean isPlant(Block block) {
            return block instanceof net.minecraft.world.level.block.BushBlock;
        }

        public static boolean isReplaceablePlant(Block block) {
            return hasTag(block, BlockTags.REPLACEABLE) && isPlant(block);
        }

        public static boolean isUnderwaterPlant(Block block) {
            return hasTag(block, BlockTags.UNDERWATER_BONEMEALS) && isPlant(block);
        }
    }

    /**
     * Fixes the max health value (why would it be set so low?!) by scanning for matching fields and correcting them.
     * This is a workaround for remapName not working.
     */
    public static void fixMaxHealth() {
        try {
            Field[] classFields = RangedAttribute.class.getDeclaredFields();
            for (Field field : classFields) {
                if (field.getType().equals(double.class)) {
                    field.setAccessible(true);
                    if ((double) field.get(Attributes.MAX_HEALTH.value()) == 1024.0D) {
                        field.setDouble(Attributes.MAX_HEALTH.value(), Double.MAX_VALUE);
                    }
                }
            }
        } catch (Exception e) {
            logError("Unable to fix the Maximum Mob health limit, all bosses will be capped at 1024 health, if you are using the latest version of Lycanites Mobs please report this as a bug with the following stack trace:");
            e.printStackTrace();
        }
    }

    // ==================================================
    //                      Raytrace
    // ==================================================
    // ========== Raytrace All ==========
    public static HitResult raytrace(Level world, double x, double y, double z, double tx, double ty, double tz, float borderSize, Entity entity, HashSet<Entity> excluded) {
        Vec3 startVec = new Vec3(x, y, z);
        Vec3 lookVec = new Vec3(tx - x, ty - y, tz - z);
        Vec3 endVec = new Vec3(tx, ty, tz);
        float minX = (float) (x < tx ? x : tx);
        float minY = (float) (y < ty ? y : ty);
        float minZ = (float) (z < tz ? z : tz);
        float maxX = (float) (x > tx ? x : tx);
        float maxY = (float) (y > ty ? y : ty);
        float maxZ = (float) (z > tz ? z : tz);

        // Get Block Collision:
        HitResult collision = world.clip(new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        startVec = new Vec3(x, y, z);

        // Get Entity Collision:
        if (excluded != null) {
            AABB bb = new AABB(minX, minY, minZ, maxX, maxY, maxZ).expandTowards(borderSize, borderSize, borderSize);
            List<Entity> allHitEntities = world.getEntities(null, bb);
            Entity closestHitEntity = null;
            double closestEntDistance = Float.POSITIVE_INFINITY;
            for (Entity hitEntity : allHitEntities) {
                if (hitEntity.isPickable() && !excluded.contains(hitEntity)) {
                    double entDistance = startVec.distanceTo(hitEntity.position());
                    if (entDistance < closestEntDistance) {
                        closestEntDistance = entDistance;
                        closestHitEntity = hitEntity;
                    }
                }
            }
            if (closestHitEntity != null) {
                collision = new EntityHitResult(closestHitEntity);
            }
        }

        return collision;
    }


    // ==================================================
    //                      Seasonal
    // ==================================================
    public static boolean isValentines() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.FEBRUARY && calendar.get(Calendar.DAY_OF_MONTH) >= 7 && calendar.get(Calendar.DAY_OF_MONTH) <= 14;
    }

    protected static Calendar easterCalendar;

    public static boolean isEaster() {
        Calendar calendar = Calendar.getInstance();

        if (easterCalendar == null) {
            int Y = calendar.get(Calendar.YEAR);
            int a = Y % 19;
            int b = Y / 100;
            int c = Y % 100;
            int d = b / 4;
            int e = b % 4;
            int f = (b + 8) / 25;
            int g = (b - f + 1) / 3;
            int h = (19 * a + b - d - g + 15) % 30;
            int i = c / 4;
            int k = c % 4;
            int L = (32 + 2 * e + 2 * i - h - k) % 7;
            int m = (a + 11 * h + 22 * L) / 451;
            int easterMonth = (h + L - 7 * m + 114) / 31;
            int easterDay = ((h + L - 7 * m + 114) % 31) + 1;
            easterCalendar = new GregorianCalendar(Y, easterMonth, easterDay);
        }

        long daysUntilEaster = ChronoUnit.DAYS.between(calendar.toInstant(), easterCalendar.toInstant());
        return daysUntilEaster <= 7 && daysUntilEaster >= 0;
    }

    public static boolean isMidsummer() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.JULY && calendar.get(Calendar.DAY_OF_MONTH) >= 10 && calendar.get(Calendar.DAY_OF_MONTH) <= 20;
    }

    public static boolean isHalloween() {
        Calendar calendar = Calendar.getInstance();
        if ((calendar.get(Calendar.DAY_OF_MONTH) >= 25 && calendar.get(Calendar.MONTH) == calendar.OCTOBER)
                || (calendar.get(Calendar.DAY_OF_MONTH) == 1 && calendar.get(Calendar.MONTH) == calendar.NOVEMBER)
        )
            return true;
        return false;
    }

    public static boolean isYuletide() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) >= 10 && calendar.get(Calendar.DAY_OF_MONTH) <= 25;
    }

    public static boolean isYuletidePeak() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.DECEMBER && calendar.get(Calendar.DAY_OF_MONTH) == 25;
    }

    public static boolean isNewYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
}
