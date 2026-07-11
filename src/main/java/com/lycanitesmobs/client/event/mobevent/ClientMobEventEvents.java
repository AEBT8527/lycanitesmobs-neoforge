package com.lycanitesmobs.client.event.mobevent;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.screen.base.BaseOverlayScreen;
import com.lycanitesmobs.client.manager.ClientManager;
import com.lycanitesmobs.core.event.mobevent.MobEvent;
import com.lycanitesmobs.core.manager.MobEventManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = LycanitesMobs.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientMobEventEvents {
    private static class ClientWorldState {
        final Map<String, MobEventPlayerClient> mobEventPlayers = new HashMap<>();
        MobEventPlayerClient worldEventPlayer;
    }

    private static final Map<Level, ClientWorldState> WORLD_STATES = new WeakHashMap<>();

    @SubscribeEvent
    public static void onClientUpdate(ClientTickEvent.Post event) {
        if (ClientManager.getInstance().getClientPlayer() == null)
            return;

        Level world = ClientManager.getInstance().getClientPlayer().getCommandSenderWorld();
        if (!world.isClientSide)
            return;

        update(world);
    }

    private static ClientWorldState getState(Level world) {
        return WORLD_STATES.computeIfAbsent(world, ignored -> new ClientWorldState());
    }

    public static void applyMobEvent(Level world, String mobEventName) {
        if (world == null || !world.isClientSide) {
            return;
        }

        if ("".equals(mobEventName)) {
            stopMobEvent(world, mobEventName);
            return;
        }

        MobEvent mobEvent = MobEventManager.getInstance().getMobEvent(mobEventName);
        if (mobEvent == null) {
            LMHelperClass.logWarningMessage("Tried to start a client mob event with the invalid name: '" + mobEventName + "'.");
            return;
        }

        ClientWorldState state = getState(world);
        MobEventPlayerClient mobEventPlayerClient = state.mobEventPlayers.get(mobEvent.getName());
        boolean extended = mobEventPlayerClient != null && mobEventPlayerClient.mobEvent == mobEvent;
        if (!extended) {
            mobEventPlayerClient = new MobEventPlayerClient(mobEvent, world);
            state.mobEventPlayers.put(mobEvent.getName(), mobEventPlayerClient);
        }
        mobEventPlayerClient.extended = extended;

        Player clientPlayer = ClientManager.getInstance().getClientPlayer();
        if (clientPlayer != null) {
            mobEventPlayerClient.onStart(clientPlayer);
        }
    }

    public static void applyWorldEvent(Level world, String mobEventName) {
        if (world == null || !world.isClientSide) {
            return;
        }

        if ("".equals(mobEventName)) {
            stopWorldEvent(world);
            return;
        }

        MobEvent mobEvent = MobEventManager.getInstance().getMobEvent(mobEventName);
        if (mobEvent == null) {
            LMHelperClass.logWarningMessage("Tried to start a client world event with the invalid name: '" + mobEventName + "'.");
            return;
        }

        ClientWorldState state = getState(world);
        boolean extended = state.worldEventPlayer != null && state.worldEventPlayer.mobEvent == mobEvent;
        if (!extended) {
            state.worldEventPlayer = new MobEventPlayerClient(mobEvent, world);
        }
        state.worldEventPlayer.extended = extended;

        Player clientPlayer = ClientManager.getInstance().getClientPlayer();
        if (clientPlayer != null) {
            state.worldEventPlayer.onStart(clientPlayer);
        }
    }

    private static void stopMobEvent(Level world, String mobEventName) {
        if (world == null || !world.isClientSide) {
            return;
        }

        ClientWorldState state = getState(world);
        MobEventPlayerClient mobEventPlayerClient = state.mobEventPlayers.remove(mobEventName);
        Player clientPlayer = ClientManager.getInstance().getClientPlayer();
        if (mobEventPlayerClient != null && clientPlayer != null) {
            mobEventPlayerClient.onFinish(clientPlayer);
        }
    }

    private static void stopWorldEvent(Level world) {
        if (world == null || !world.isClientSide) {
            return;
        }

        ClientWorldState state = getState(world);
        Player clientPlayer = ClientManager.getInstance().getClientPlayer();
        if (state.worldEventPlayer != null && clientPlayer != null) {
            state.worldEventPlayer.onFinish(clientPlayer);
        }
        state.worldEventPlayer = null;
    }

    private static void update(Level world) {
        if (world == null || !world.isClientSide) {
            return;
        }

        ClientWorldState state = getState(world);
        for (MobEventPlayerClient mobEventPlayerClient : state.mobEventPlayers.values()) {
            mobEventPlayerClient.onUpdate();
        }
        if (state.worldEventPlayer != null) {
            state.worldEventPlayer.onUpdate();
        }
    }

    public static void render(Level world, GuiGraphics guiGraphics, BaseOverlayScreen overlayScreen, int sWidth, int sHeight) {
        if (world == null || !world.isClientSide) {
            return;
        }

        ClientWorldState state = getState(world);
        for (MobEventPlayerClient mobEventPlayerClient : state.mobEventPlayers.values()) {
            mobEventPlayerClient.onGUIUpdate(guiGraphics, overlayScreen, sWidth, sHeight);
        }
        if (state.worldEventPlayer != null) {
            state.worldEventPlayer.onGUIUpdate(guiGraphics, overlayScreen, sWidth, sHeight);
        }
    }
}
