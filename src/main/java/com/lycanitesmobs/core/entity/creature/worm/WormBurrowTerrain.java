package com.lycanitesmobs.core.entity.creature.worm;

import com.lycanitesmobs.core.data.tag.LycanitesBlockTags;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Which terrain a worm creature can burrow through / stealth in.
 * Upstream 0.1.0 moved this off a hardcoded material list onto the
 * lycanitesmobs:worm_burrowable block tag so packs can retune it.
 */
final class WormBurrowTerrain {
    private WormBurrowTerrain() {
    }

    static boolean isBurrowable(BlockState blockState) {
        return blockState.is(LycanitesBlockTags.WORM_BURROWABLE);
    }
}
