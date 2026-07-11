package com.lycanitesmobs.core.worldgen.dungeon.definition;

import net.minecraft.core.registries.BuiltInRegistries;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ThemeBlock {
    /** Dungeon Theme Blocks define a block to be used in the theme along with other information. **/

    /**
     * The block to use.
     **/
    protected Block block = null;

    /**
     * The block to use.
     **/
    protected String blockId;

    /**
     * The weight for randomly using this block.
     **/
    protected int weight = 8;

    public void setBlock(Block block) {
        this.block = block;
    }

    public String getBlockId() {
        return this.blockId;
    }

    public int getWeight() {
        return this.weight;
    }


    /**
     * Loads this Dungeon Theme from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        if (json.has("blockId")) {
            this.blockId = json.get("blockId").getAsString().toLowerCase();
        } else {
            LMHelperClass.logWarningMessage("Error adding Dungeon Theme Block: JSON value 'blockId' has not been set.");
        }

        if (json.has("weight")) {
            this.weight = json.get("weight").getAsInt();
        }
    }


    public Block getBlock() {
        if (this.block == null) {
            this.block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(this.blockId));
        }
        if (this.block == null) {
            return Blocks.CAVE_AIR;
        }
        return this.block;
    }


    /**
     * Returns a block state for this theme block entry.
     *
     * @return A new block state.
     */
    public BlockState getBlockState() {
        return this.getBlock().defaultBlockState();
    }
}
