package com.lycanitesmobs.core.item.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** 26.x: the Tier interface was removed; this stands alone with the accessors the mod uses. */
public enum CustomTiers {
    HARVEST_0(0, 59, 2.0F, 0.0F, 15),
    HARVEST_1(1, 131, 4.0F, 1.0F, 5),
    HARVEST_2(2, 250, 6.0F, 2.0F, 14),
    HARVEST_3(3, 1561, 8.0F, 3.0F, 10),
    HARVEST_4(4, 3122, 10.0F, 4.0F, 18);

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;

    CustomTiers(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
    }

    public int getUses() {
        return this.uses;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getAttackDamageBonus() {
        return this.attackDamageBonus;
    }

    public int getLevel() {
        return this.level;
    }

    public TagKey<Block> getIncorrectBlocksForDrops() {
        return switch (this.level) {
            case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        };
    }

}
