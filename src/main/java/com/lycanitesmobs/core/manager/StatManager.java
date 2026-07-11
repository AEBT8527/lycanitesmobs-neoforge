package com.lycanitesmobs.core.manager;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.util.helpers.AssetHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class StatManager {
    private static StatManager INSTANCE;

    public static StatManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new StatManager();
        }
        return INSTANCE;
    }

    private final Map<String, StatType<Identifier>> statTypes = new HashMap<>();

    /**
     * Creates all base Stat Types.
     */
    public void createStatTypes() {
        this.addStatType("learn");
        this.addStatType("summon");
    }

    /**
     * Adds a new Stat Type. TODO Implement stats.
     *
     * @param name The unique name of the stat type.
     */
    public void addStatType(String name) {
//		Registry<Identifier> statRegistry = new SimpleRegistry<>();
//		StatType<Identifier> statType = new StatType<>(statRegistry);
//		statType.setRegistryName(LycanitesMobs.modInfo.modid, name);
//		this.statTypes.put(name, statType);
    }

    public void forEachStatType(BiConsumer<String, StatType<Identifier>> action) {
        this.statTypes.forEach(action);
    }

    /**
     * Gets a from a stat type.
     *
     * @param typeName The stat type name.
     * @param name     The stat name.
     * @return The stat instance or null.
     */
    @Nullable
    public Stat getStat(String typeName, String name) {
        if (!this.statTypes.containsKey(typeName)) {
            return null;
        }
        return this.statTypes.get(typeName).get(AssetHelper.resource(LycanitesMobs.modInfo.modid, name), StatFormatter.DEFAULT);
    }
}
