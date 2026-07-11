package com.lycanitesmobs.core.manager;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.item.equipment.ItemEquipmentPart;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lycanitesmobs.core.tabs.LMEquipmentPartsGroup.equipmentNames;

public class EquipmentPartManager extends JSONLoader {

    protected static EquipmentPartManager INSTANCE;

    protected final Map<String, Lazy<ItemEquipmentPart>> equipmentParts = new HashMap<>();

    /**
     * A list of mod groups that have loaded with this Equipment Part Manager.
     **/
    protected final List<ModInfo> loadedGroups = new ArrayList<>();

    /**
     * Returns the main EquipmentPartManager INSTANCE or creates it and returns it.
     **/
    public static EquipmentPartManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EquipmentPartManager();
        }
        return INSTANCE;
    }

    public Set<String> getEquipmentPartNames() {
        return Collections.unmodifiableSet(this.equipmentParts.keySet());
    }

    public Set<Map.Entry<String, Lazy<ItemEquipmentPart>>> getEquipmentPartEntries() {
        return Collections.unmodifiableMap(this.equipmentParts).entrySet();
    }

    /**
     * Loads all JSON Equipment Parts.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        this.rememberLoadedGroup(modInfo);
        loadAllJson(modInfo, "Equipment", "equipment", "itemName", false, null, FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Equipment", "Complete! " + equipmentParts.size() + " JSON Equipment Parts Loaded In Total.");
    }

    protected void rememberLoadedGroup(ModInfo modInfo) {
        if (!this.loadedGroups.contains(modInfo)) {
            this.loadedGroups.add(modInfo);
        }
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        Item.Properties properties = new Item.Properties().stacksTo(1).setNoRepair();
        Lazy<ItemEquipmentPart> equipmentPart = Lazy.of(() -> new ItemEquipmentPart(properties, json));
        String name = ItemEquipmentPart.getPartName(json);
        if (this.equipmentParts.containsKey(name)) {
            LMHelperClass.logWarningMessage("[Equipment] Tried to add a Equipment Part with a name that is already in use: " + name);
            throw new RuntimeException("[Equipment] Tried to add a Equipment Part with a name that is already in use: " + name);
        }
        this.equipmentParts.put(name, equipmentPart);
        equipmentNames.add(name);
        ObjectManager.addItem(name, equipmentPart);
    }

    /**
     * Reloads all Equipment part JSON.
     */
    public void reload() {
        this.equipmentParts.clear();
        for (ModInfo group : this.loadedGroups) {
            this.loadAllFromJson(group);
        }
    }
}
