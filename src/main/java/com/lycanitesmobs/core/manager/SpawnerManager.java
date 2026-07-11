package com.lycanitesmobs.core.manager;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.data.loaders.FileLoader;
import com.lycanitesmobs.core.data.loaders.JSONLoader;
import com.lycanitesmobs.core.data.loaders.StreamLoader;
import com.lycanitesmobs.core.data.info.ModInfo;
import com.lycanitesmobs.core.entity.spawner.Spawner;
import com.lycanitesmobs.core.entity.spawner.condition.SpawnCondition;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class SpawnerManager extends JSONLoader {
    /**
     * This manages all Spawners, it load them and can also destroy them. Spawners are then ran by Spawn Triggers which are called from the SpawnerEventListener.
     **/

    private static SpawnerManager INSTANCE;

    protected Map<String, Spawner> spawners = new HashMap<>();
    protected List<SpawnCondition> globalSpawnConditions = new ArrayList<>();
    private String loadingDefinitionPath = "spawners";


    /**
     * Returns the main SpawnerManager INSTANCE or creates it and returns it.
     **/
    public static SpawnerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpawnerManager();
        }
        return INSTANCE;
    }

    public Collection<Spawner> getSpawners() {
        return Collections.unmodifiableCollection(this.spawners.values());
    }

    public Spawner getSpawner(String name) {
        return this.spawners.get(name);
    }

    public boolean hasSpawner(String name) {
        return this.spawners.containsKey(name);
    }

    public List<SpawnCondition> getGlobalSpawnConditions() {
        return Collections.unmodifiableList(this.globalSpawnConditions);
    }


    /**
     * Loads all JSON Spawners.
     **/
    public void loadAllFromJson(ModInfo modInfo) {
        // Spawners:
        this.loadingDefinitionPath = "spawners";
        this.loadAllJson(modInfo, "Spawner", "spawners", "name", true, "spawner", FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Spawner", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");

        // Mob Event Spawners:
        this.loadingDefinitionPath = "mobevents";
        this.loadAllJson(modInfo, "Spawner", "mobevents", "name", true, "spawner", FileLoader.common(), StreamLoader.common());
        LMHelperClass.logDebug("Spawner", "Complete! " + this.spawners.size() + " JSON Spawners Loaded In Total.");

        // Load Global Spawn Conditions:
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
        String configPath = new File(".") + "/config/" + LycanitesMobs.MODID + "/";
        this.globalSpawnConditions.clear();

        JsonObject defaultGlobalJson;
        if (FileLoader.common().isReady()) {
            Path defaultGlobalPath = FileLoader.common().getPath("globalspawner.json");
            defaultGlobalJson = this.loadJsonObject(gson, defaultGlobalPath);
        } else {
            defaultGlobalJson = this.loadJsonObject(gson, StreamLoader.common().getStream("globalspawner.json"));
        }
        if (defaultGlobalJson == null) {
            LMHelperClass.logWarningMessage("Could not find Global Spawning JSON.");
        }

        File customGlobalFile = new File(configPath + "globalspawner.json");
        JsonObject customGlobalJson = null;
        if (customGlobalFile.exists()) {
            customGlobalJson = this.loadJsonObject(gson, customGlobalFile.toPath());
        }

        JsonObject globalJson = this.writeDefaultJSONObject(gson, "globalspawner", defaultGlobalJson, customGlobalJson);
        if (globalJson.has("conditions")) {
            JsonArray jsonArray = globalJson.get("conditions").getAsJsonArray();
            Iterator<JsonElement> jsonIterator = jsonArray.iterator();
            while (jsonIterator.hasNext()) {
                JsonObject spawnConditionJson = jsonIterator.next().getAsJsonObject();
                SpawnCondition spawnCondition = SpawnCondition.createFromJSON(spawnConditionJson);
                if (spawnCondition != null) this.globalSpawnConditions.add(spawnCondition);
            }
        }
    }


    @Override
    public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
        Spawner spawner = new Spawner();
        String name = json.get("name").getAsString();
        String definitionPath = "config/" + LycanitesMobs.MODID + "/" + this.loadingDefinitionPath + "/" + name + ".json";
        spawner.loadFromJSON(json, definitionPath);
        this.addSpawner(spawner);
    }


    /**
     * Reloads all JSON Spawners.
     **/
    public void reload() {
        for (Spawner spawner : this.spawners.values().toArray(new Spawner[this.spawners.size()])) {
            spawner.destroy();
        }

        this.loadAllFromJson(LycanitesMobs.modInfo);
    }


    /**
     * Adds a new Spawner to this Manager.
     **/
    public void addSpawner(Spawner spawner) {
        if (this.spawners.containsKey(spawner.getName())) {
            LMHelperClass.logWarningMessage("[Spawner Manager] Tried to add a Spawner with a name that is already in use: " + spawner.getName());
            return;
        }
        if (this.spawners.values().contains(spawner)) {
            LMHelperClass.logWarningMessage("[Spawner Manager] Tried to add a Spawner that is already added: " + spawner.getName());
            return;
        }
        this.spawners.put(spawner.getName(), spawner);
    }


    /**
     * Removes a Spawner from this Manager.
     **/
    public void removeSpawner(Spawner spawner) {
        if (!this.spawners.containsKey(spawner.getName())) {
            LMHelperClass.logWarningMessage("[Spawner Manager] Tried to remove a Spawner that hasn't been added: " + spawner.getName());
            return;
        }
        this.spawners.remove(spawner.getName());
    }
}
