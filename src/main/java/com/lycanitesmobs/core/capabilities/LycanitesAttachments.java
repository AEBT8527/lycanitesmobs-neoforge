package com.lycanitesmobs.core.capabilities;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.capabilities.entity.ExtendedEntity;
import com.lycanitesmobs.core.capabilities.entity.ExtendedPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * NeoForge data attachments replacing the old Forge capability providers. {@link ExtendedEntity}
 * and {@link ExtendedPlayer} carry Lycanites' per-entity persistent data (beastiary knowledge,
 * pets, summon sets, stats, etc.); attaching them via {@link AttachmentType} preserves that data
 * across save/load and (for the player) across death.
 */
public class LycanitesAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LycanitesMobs.MODID);

    public static final Supplier<AttachmentType<ExtendedEntity>> EXTENDED_ENTITY =
            ATTACHMENT_TYPES.register("extended_entity", () -> AttachmentType
                    .builder(ExtendedEntity::new)
                    .serialize(ExtendedEntity.SERIALIZER)
                    .build());

    public static final Supplier<AttachmentType<ExtendedPlayer>> EXTENDED_PLAYER =
            ATTACHMENT_TYPES.register("extended_player", () -> AttachmentType
                    .builder(ExtendedPlayer::new)
                    .serialize(ExtendedPlayer.SERIALIZER)
                    .copyOnDeath()
                    .build());
}
