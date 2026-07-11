package com.lycanitesmobs.core.event;

import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.lycanitesmobs.core.entity.spawner.SpawnerTriggerDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Forge event adapter for the spawner runtime dispatcher.
 */
public class SpawnerEventListener {
    private static SpawnerEventListener INSTANCE;
    private static boolean testOnCreative = false;

    public static SpawnerEventListener getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpawnerEventListener();
        }
        return INSTANCE;
    }

    public static boolean shouldTestCreative() {
        return testOnCreative;
    }

    public static void setTestOnCreative(boolean testOnCreative) {
        SpawnerEventListener.testOnCreative = testOnCreative;
    }

    @SubscribeEvent
    public void onWorldUpdate(LevelTickEvent.Post event) {
        SpawnerTriggerDispatcher.getInstance().onWorldTick(event.getLevel());
    }

    @SubscribeEvent
    public void onEntityUpdate(EntityTickEvent.Pre event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Player player)
                || player.getCommandSenderWorld() == null || player.getCommandSenderWorld().isClientSide) {
            return;
        }
        SpawnerTriggerDispatcher.getInstance().onPlayerTick(player);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SpawnerTriggerDispatcher.getInstance().onPlayerLoggedOut(event.getEntity());
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        LivingEntity killedEntity = event.getEntity();
        if (killedEntity == null || killedEntity.getCommandSenderWorld().isClientSide || event.isCanceled()) {
            return;
        }
        SpawnerTriggerDispatcher.getInstance().onEntityDeath(killedEntity, event.getSource().getEntity());
    }

    @SubscribeEvent
    public void onEntitySpawn(net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent event) {
        LivingEntity spawnedEntity = event.getEntity();
        if (spawnedEntity == null || spawnedEntity.getCommandSenderWorld().isClientSide) {
            return;
        }
        SpawnerTriggerDispatcher.getInstance().onEntitySpawned(spawnedEntity);
    }

    public void onChunkGenerate(String dimensionId, ChunkPos chunkPos) {
        SpawnerTriggerDispatcher.getInstance().onChunkGenerate(dimensionId, chunkPos);
    }

    @SubscribeEvent
    public void onHarvestDrops(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (event.getState() == null || !(event.getLevel() instanceof Level world) || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }
        SpawnerTriggerDispatcher.getInstance().onHarvestDrops(world, player, event.getPos(), event.getState());
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState() == null || !(event.getLevel() instanceof Level world) || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }
        this.onBlockBreak(world, event.getPos(), event.getState(), event.getPlayer(), 0);
    }

    public void onBlockBreak(Level world, BlockPos blockPos, BlockState blockState, Player player, int chain) {
        SpawnerTriggerDispatcher.getInstance().onBlockBreak(world, blockPos, blockState, player, chain);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent.EntityPlaceEvent event) {
        if (event.getState() == null || !(event.getLevel() instanceof Level world) || event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }
        if (event.getEntity() instanceof Player player) {
            this.onBlockPlace(world, event.getPos(), event.getState(), player, 0);
        }
    }

    public void onBlockPlace(Level world, BlockPos blockPos, BlockState blockState, Player player, int chain) {
        SpawnerTriggerDispatcher.getInstance().onBlockPlace(world, blockPos, blockState, player, chain);
    }

    @SubscribeEvent
    public void onSleep(CanPlayerSleepEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getCommandSenderWorld().isClientSide) {
            return;
        }

        Level world = player.getCommandSenderWorld();
        if (world.isClientSide || world.isDay()) {
            return;
        }

        BlockPos spawnPos = player.blockPosition().offset(0, 0, 1);
        if (SpawnerTriggerDispatcher.getInstance().onSleep(world, player, spawnPos)) {
            event.setProblem(Player.BedSleepingProblem.NOT_SAFE);
        }
    }

    @SubscribeEvent
    public void onFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player == null || player.getCommandSenderWorld().isClientSide || event.isCanceled()) {
            return;
        }
        SpawnerTriggerDispatcher.getInstance().onFished(player.getCommandSenderWorld(), player, event.getHookEntity());
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        if (explosion == null) {
            return;
        }

        Player player = null;
        if (explosion.getDirectSourceEntity() instanceof Player owner) {
            player = owner;
        }

        SpawnerTriggerDispatcher.getInstance().onExplosion(event.getLevel(), player, explosion);
    }

    @SubscribeEvent
    public void onLavaMix(BlockEvent.FluidPlaceBlockEvent event) {
        if (!(event.getLevel() instanceof Level world)) {
            return;
        }
        if (event.getOriginalState().getBlock() == net.minecraft.world.level.block.Blocks.LAVA
                && event.getNewState().getBlock() == net.minecraft.world.level.block.Blocks.OBSIDIAN) {
            SpawnerTriggerDispatcher.getInstance().onLavaMix(world, event.getState(), event.getLiquidPos());
        }
    }
}
