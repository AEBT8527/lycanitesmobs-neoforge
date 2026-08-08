package com.lycanitesmobs.client.item;

import com.lycanitesmobs.core.data.info.creature.CreatureInfo;
import com.lycanitesmobs.core.item.consumable.entity.ItemCustomSpawnEgg;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * 26.x replacement for the old ItemColor handler: tints the spawn egg layers with the
 * creature's egg colors (layer 0 = background, layer 1 = foreground overlay).
 */
public record SpawnEggTintSource(int layer) implements ItemTintSource {

    public static final MapCodec<SpawnEggTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.INT.optionalFieldOf("layer", 0).forGetter(SpawnEggTintSource::layer))
                    .apply(i, SpawnEggTintSource::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (itemStack.getItem() instanceof ItemCustomSpawnEgg spawnEgg) {
            CreatureInfo creatureInfo = spawnEgg.getCreatureInfo(itemStack);
            if (creatureInfo != null) {
                return ARGB.opaque(this.layer == 0 ? creatureInfo.getEggBackColor() : creatureInfo.getEggForeColor());
            }
            return ARGB.opaque(this.layer == 0 ? 0x227744 : 0x11EE44);
        }
        return ARGB.opaque(0xFFFFFF);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}
