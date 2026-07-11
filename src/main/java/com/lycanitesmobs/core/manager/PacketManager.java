package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.network.LycanitesPayload;
import com.lycanitesmobs.core.network.NetworkDir;
import com.lycanitesmobs.core.network.PacketContext;
import com.lycanitesmobs.core.network.message.*;
import com.lycanitesmobs.core.network.packet.MessageScreenRequest;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Network dispatch. Ported from Forge's {@code SimpleChannel} to NeoForge's payload system by
 * multiplexing every Lycanites message through a single {@link LycanitesPayload} keyed by a numeric
 * id. Each message class keeps its original static {@code encode/decode/handle} methods; this class
 * owns the id table, wire (de)serialisation dispatch, and payload registration.
 */
@EventBusSubscriber(modid = LycanitesMobs.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketManager {
    private static final String PROTOCOL_VERSION = "1";

    private record MsgDef<T>(Class<T> clazz,
                             BiConsumer<T, FriendlyByteBuf> encoder,
                             Function<FriendlyByteBuf, T> decoder,
                             BiConsumer<T, Supplier<PacketContext>> handler) {
        void encode(Object msg, FriendlyByteBuf buf) {
            this.encoder.accept(this.clazz.cast(msg), buf);
        }

        Object decode(FriendlyByteBuf buf) {
            return this.decoder.apply(buf);
        }

        void handle(Object msg, Supplier<PacketContext> ctx) {
            this.handler.accept(this.clazz.cast(msg), ctx);
        }
    }

    private static final List<MsgDef<?>> DEFS = new ArrayList<>();
    private static final Map<Class<?>, Integer> ID_BY_CLASS = new HashMap<>();

    private static <T> void def(Class<T> clazz,
                                BiConsumer<T, FriendlyByteBuf> encoder,
                                Function<FriendlyByteBuf, T> decoder,
                                BiConsumer<T, Supplier<PacketContext>> handler) {
        int id = DEFS.size();
        DEFS.add(new MsgDef<>(clazz, encoder, decoder, handler));
        ID_BY_CLASS.put(clazz, id);
    }

    static {
        // Order defines the wire id and must stay stable between client and server.
        def(MessageBeastiary.class, MessageBeastiary::encode, MessageBeastiary::decode, MessageBeastiary::handle);
        def(MessageCreature.class, MessageCreature::encode, MessageCreature::decode, MessageCreature::handle);
        def(MessageCreatureKnowledge.class, MessageCreatureKnowledge::encode, MessageCreatureKnowledge::decode, MessageCreatureKnowledge::handle);
        def(MessagePlayerStats.class, MessagePlayerStats::encode, MessagePlayerStats::decode, MessagePlayerStats::handle);
        def(MessagePetEntry.class, MessagePetEntry::encode, MessagePetEntry::decode, MessagePetEntry::handle);
        def(MessagePetEntryRemove.class, MessagePetEntryRemove::encode, MessagePetEntryRemove::decode, MessagePetEntryRemove::handle);
        def(MessageSummonSet.class, MessageSummonSet::encode, MessageSummonSet::decode, MessageSummonSet::handle);
        def(MessageSummonSetSelection.class, MessageSummonSetSelection::encode, MessageSummonSetSelection::decode, MessageSummonSetSelection::handle);
        def(MessageEntityPickedUp.class, MessageEntityPickedUp::encode, MessageEntityPickedUp::decode, MessageEntityPickedUp::handle);
        def(MessageEntityPerched.class, MessageEntityPerched::encode, MessageEntityPerched::decode, MessageEntityPerched::handle);
        def(MessageWorldEvent.class, MessageWorldEvent::encode, MessageWorldEvent::decode, MessageWorldEvent::handle);
        def(MessageMobEvent.class, MessageMobEvent::encode, MessageMobEvent::decode, MessageMobEvent::handle);
        def(MessageSummoningPedestalStats.class, MessageSummoningPedestalStats::encode, MessageSummoningPedestalStats::decode, MessageSummoningPedestalStats::handle);
        def(MessageEntityVelocity.class, MessageEntityVelocity::encode, MessageEntityVelocity::decode, MessageEntityVelocity::handle);
        def(MessageEntityGUICommand.class, MessageEntityGUICommand::encode, MessageEntityGUICommand::decode, MessageEntityGUICommand::handle);
        def(MessageScreenRequest.class, MessageScreenRequest::encode, MessageScreenRequest::decode, MessageScreenRequest::handle);
        def(MessagePlayerControl.class, MessagePlayerControl::encode, MessagePlayerControl::decode, MessagePlayerControl::handle);
        def(MessagePlayerLeftClick.class, MessagePlayerLeftClick::encode, MessagePlayerLeftClick::decode, MessagePlayerLeftClick::handle);
        def(MessageSummoningPedestalSummonSet.class, MessageSummoningPedestalSummonSet::encode, MessageSummoningPedestalSummonSet::decode, MessageSummoningPedestalSummonSet::handle);
        def(MessageTileEntityButton.class, MessageTileEntityButton::encode, MessageTileEntityButton::decode, MessageTileEntityButton::handle);
        def(MessageSpawnEntity.class, MessageSpawnEntity::encode, MessageSpawnEntity::decode, MessageSpawnEntity::handle);
        def(MessageOverlayMessage.class, MessageOverlayMessage::encode, MessageOverlayMessage::decode, MessageOverlayMessage::handle);
    }

    /**
     * Kept for compatibility with the old imperative setup call; the id table is built statically
     * and the payload itself is registered from {@link #onRegisterPayloads}.
     */
    public void register() {
    }

    // ==================================================
    //                Payload registration
    // ==================================================
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playBidirectional(LycanitesPayload.TYPE, LycanitesPayload.CODEC, PacketManager::handlePayload);
    }

    private static void handlePayload(LycanitesPayload payload, IPayloadContext context) {
        NetworkDir direction = context.flow() == PacketFlow.SERVERBOUND ? NetworkDir.PLAY_TO_SERVER : NetworkDir.PLAY_TO_CLIENT;
        PacketContext ctx = new PacketContext(context, direction);
        DEFS.get(payload.messageId()).handle(payload.message(), () -> ctx);
    }

    // ==================================================
    //                 Wire (de)serialisation
    // ==================================================
    public static void encodeInner(int id, Object message, RegistryFriendlyByteBuf buf) {
        DEFS.get(id).encode(message, buf);
    }

    public static Object decodeInner(int id, RegistryFriendlyByteBuf buf) {
        return DEFS.get(id).decode(buf);
    }

    private LycanitesPayload wrap(Object message) {
        Integer id = ID_BY_CLASS.get(message.getClass());
        if (id == null) {
            throw new IllegalArgumentException("Unregistered Lycanites network message: " + message.getClass().getName());
        }
        return new LycanitesPayload(id, message);
    }

    // ==================================================
    //                   Send To Player
    // ==================================================
    public <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, wrap(message));
    }

    // ==================================================
    //                 Send To All Around
    // ==================================================
    public <MSG> void sendToAllAround(MSG message, Level world, Vec3 pos, double range) {
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer serverPlayer && player.distanceToSqr(pos) <= (range * range)) {
                this.sendToPlayer(message, serverPlayer);
            }
        }
    }

    // ==================================================
    //                 Send To Dimension
    // ==================================================
    public <MSG> void sendToWorld(MSG message, Level world) {
        for (Player player : world.players()) {
            if (player instanceof ServerPlayer serverPlayer) {
                this.sendToPlayer(message, serverPlayer);
            }
        }
    }

    // ==================================================
    //                   Send To Server
    // ==================================================
    public <MSG> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(wrap(message));
    }
}
