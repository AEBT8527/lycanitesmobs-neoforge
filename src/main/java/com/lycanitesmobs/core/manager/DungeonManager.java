package com.lycanitesmobs.core.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSchematic;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSector;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonStructure;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonTheme;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DungeonManager extends JSONLoader {
    /**
     * This manages all Dungeons, it can load them and can also destroy them.
     **/

    private static DungeonManager INSTANCE;

    protected Map<String, DungeonTheme> themes = new HashMap<>();
    protected Map<String, DungeonStructure> structures = new HashMap<>();
    protected Map<String, DungeonSector> sectors = new HashMap<>();
    protected Map<String, DungeonSchematic> schematics = new HashMap<>();

    /**
     * Returns the main DungeonManager INSTANCE or creates it and returns it.
     **/
    public static DungeonManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DungeonManager();
        }
        return INSTANCE;
    }

    /**
     * Loads all JSON Dungeons.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        // Themes:
        this.loadAllJson(modInfo, "Dungeon Theme", "dungeons/themes", "name", true, null, FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Dungeon", "Complete! " + this.themes.size() + " Dungeon Themes Loaded In Total.");

        // Structures:
        this.loadAllJson(modInfo, "Dungeon Structure", "dungeons/structures", "name", true, null, FileLoader.common(), StreamLoader.common());

        // Sectors:
        this.loadAllJson(modInfo, "Dungeon Sector", "dungeons/sectors", "name", true, null, FileLoader.common(), StreamLoader.common());

        // Schematics:
        this.loadAllJson(modInfo, "Dungeon Schematic", "dungeons/schematics", "name", true, null, FileLoader.common(), StreamLoader.common());
    }

    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        // Themes:
        if ("Dungeon Theme".equals(loadGroup)) {
            DungeonTheme theme = new DungeonTheme();
            theme.loadFromJSON(json);
            this.addTheme(theme);
            return;
        }

        // Structures:
        if ("Dungeon Structure".equals(loadGroup)) {
            DungeonStructure structure = new DungeonStructure();
            structure.loadFromJSON(json);
            this.addStructure(structure);
            return;
        }

        // Sectors:
        if ("Dungeon Sector".equals(loadGroup)) {
            DungeonSector sector = new DungeonSector();
            sector.loadFromJSON(json);
            this.addSector(sector);
            return;
        }

        // Schematics:
        if ("Dungeon Schematic".equals(loadGroup)) {
            DungeonSchematic schematic = new DungeonSchematic();
            schematic.loadFromJSON(json);
            this.addSchematic(schematic);
            return;
        }
    }

    /**
     * Loads all JSON Dungeons.
     **/
    public Map<String, JsonObject> loadDungeonsFromJSON(String type, ModInfo modInfo) {
        LMHelperClass.logDebug("Dungeon", "Loading JSON Dungeons " + type + "!");
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, JsonObject> spawnerJSONs = new HashMap<>();

        // Load Defaults:
        Path path = FileLoader.common().getPath("dungeons/" + type);
        Map<String, JsonObject> defaultJSONs = new HashMap<>();
        this.loadJsonObjects(gson, path, defaultJSONs, "name", null);

        // Load Custom:
        String configPath = new File(".") + "/config/" + LycanitesMobs.MODID + "/";
        File customDir = new File(configPath + "dungeons/" + type);
        customDir.mkdirs();
        path = customDir.toPath();
        Map<String, JsonObject> customJSONs = new HashMap<>();
        this.loadJsonObjects(gson, path, customJSONs, "name", null);


        // Write Defaults:
        this.writeDefaultJSONObjects(gson, defaultJSONs, customJSONs, spawnerJSONs, true, "dungeons/" + type);
        return spawnerJSONs;
    }

    /**
     * Reloads all JSON Dungeons.
     **/
    public void reload() {
        LMHelperClass.logDebug("Dungeon", "Destroying Dungeons for reload!");
        this.themes.clear();
        this.structures.clear();
        this.sectors.clear();
        this.schematics.clear();

        this.loadAllFromJson(LycanitesMobs.modInfo);
    }

    /**
     * Adds a new Dungeon Theme to this Manager.
     **/
    public void addTheme(DungeonTheme theme) {
        if (this.themes.containsKey(theme.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Theme with a name that is already in use: " + theme.getName());
            return;
        }
        if (this.themes.values().contains(theme)) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Theme that is already added: " + theme.getName());
            return;
        }
        this.themes.put(theme.getName(), theme);
    }

    /**
     * Removes a Dungeon Theme from this Manager.
     **/
    public void removeTheme(DungeonTheme theme) {
        if (!this.themes.containsKey(theme.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to remove a Dungeon Theme that hasn't been added: " + theme.getName());
            return;
        }
        this.themes.remove(theme.getName());
    }

    /**
     * Gets a Theme by name or null if none can be found.
     **/
    public DungeonTheme getTheme(String name) {
        if (!this.themes.containsKey(name)) {
            LMHelperClass.logWarning("Dungeon", "Unable to find a theme called " + name);
            return null;
        }
        return this.themes.get(name);
    }

    /**
     * Adds a new Dungeon Structure to this Manager.
     **/
    public void addStructure(DungeonStructure structure) {
        if (this.structures.containsKey(structure.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Structure with a name that is already in use: " + structure.getName());
            return;
        }
        if (this.structures.values().contains(structure)) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Structure that is already added: " + structure.getName());
            return;
        }
        this.structures.put(structure.getName(), structure);
    }

    /**
     * Removes a Dungeon Structure from this Manager.
     **/
    public void removeStructure(DungeonStructure structure) {
        if (!this.structures.containsKey(structure.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to remove a Dungeon Structure that hasn't been added: " + structure.getName());
            return;
        }
        this.structures.remove(structure.getName());
    }

    /**
     * Gets a Structure by name or null if none can be found.
     **/
    public DungeonStructure getStructure(String name) {
        if (!this.structures.containsKey(name)) {
            return null;
        }
        return this.structures.get(name);
    }

    /**
     * Adds a new Dungeon Sector to this Manager.
     **/
    public void addSector(DungeonSector sector) {
        if (this.sectors.containsKey(sector.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Sector with a name that is already in use: " + sector.getName());
            return;
        }
        if (this.sectors.values().contains(sector)) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Sector that is already added: " + sector.getName());
            return;
        }
        this.sectors.put(sector.getName(), sector);
    }

    /**
     * Removes a Dungeon Sector from this Manager.
     **/
    public void removeSector(DungeonSector sector) {
        if (!this.sectors.containsKey(sector.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to remove a Dungeon Sector that hasn't been added: " + sector.getName());
            return;
        }
        this.sectors.remove(sector.getName());
    }

    /**
     * Gets a Sector by name or null if none can be found.
     **/
    public DungeonSector getSector(String name) {
        if (!this.sectors.containsKey(name)) {
            LMHelperClass.logWarning("Dungeon", "Unable to find a Dungeon Sector by the name: " + name);
            return null;
        }
        return this.sectors.get(name);
    }

    /**
     * Adds a new Dungeon Schematic to this Manager.
     **/
    public void addSchematic(DungeonSchematic schematic) {
        if (this.schematics.containsKey(schematic.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Schematic with a name that is already in use: " + schematic.getName());
            return;
        }
        if (this.schematics.values().contains(schematic)) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to add a Dungeon Schematic that is already added: " + schematic.getName());
            return;
        }
        this.schematics.put(schematic.getName(), schematic);
    }

    /**
     * Removes a Dungeon Schematic from this Manager.
     **/
    public void removeSchematic(DungeonSchematic schematic) {
        if (!this.schematics.containsKey(schematic.getName())) {
            LMHelperClass.logWarningMessage("[Dungeon Manager] Tried to remove a Dungeon Schematic that hasn't been added: " + schematic.getName());
            return;
        }
        this.schematics.remove(schematic.getName());
    }

    /**
     * Gets a Schematic by name or null if none can be found.
     **/
    public DungeonSchematic getSchematic(String name) {
        if (!this.schematics.containsKey(name)) {
            return null;
        }
        return this.schematics.get(name);
    }

    public Collection<DungeonSchematic> getSchematics() {
        return Collections.unmodifiableCollection(this.schematics.values());
    }

}
