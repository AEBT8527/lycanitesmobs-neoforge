package com.lycanitesmobs.core.worldgen.dungeon.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.util.helpers.JSONHelper;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DungeonSector {
    /** Dungeon sectors can be corridors, rooms, entrances, etc that make up a dungeon. **/

    /**
     * The unique name of this sector. Required.
     **/
    protected String name = "";

    /**
     * The type of sector that this is. Can be: room (default), corridor, stairs or entrance.
     **/
    protected String type = "room";

    /**
     * The weight to use for this sector when selecting randomly.
     **/
    protected int weight = 8;

    /**
     * If true, this sector will use a new theme selected from the dungeon.
     **/
    protected boolean changeTheme = false;

    /**
     * Defines the minimum size of this sector.
     **/
    protected Vec3i sizeMin = new Vec3i(8, 8, 8);

    /**
     * Defines the maximum size of this sector.
     **/
    protected Vec3i sizeMax = new Vec3i(10, 10, 10);

    /**
     * Sets a padding around this sector to count towards block occupation. This is will be automatically increased by negative segment layers as needed and is at least one for entrance carving.
     **/
    protected Vec3i padding = new Vec3i(1, 0, 1);

    /**
     * A list of Structures used by this sector.
     **/
    protected List<String> structures = new ArrayList<>();

    /**
     * The floor segment of this sector.
     **/
    protected SectorSegment floor;

    /**
     * The wall segment of this sector.
     **/
    protected SectorSegment wall;

    /**
     * The ceiling segment of this sector.
     **/
    protected SectorSegment ceiling;

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public int getWeight() {
        return this.weight;
    }

    public boolean changesTheme() {
        return this.changeTheme;
    }

    public Vec3i getSizeMin() {
        return this.sizeMin;
    }

    public Vec3i getSizeMax() {
        return this.sizeMax;
    }

    public Vec3i getPadding() {
        return this.padding;
    }

    public List<String> getStructures() {
        return Collections.unmodifiableList(this.structures);
    }

    public SectorSegment getFloor() {
        return this.floor;
    }

    public SectorSegment getWall() {
        return this.wall;
    }

    public SectorSegment getCeiling() {
        return this.ceiling;
    }


    /**
     * Loads this Dungeon Sector from the provided JSON data.
     **/
    public void loadFromJSON(JsonObject json) {
        this.name = json.get("name").getAsString().toLowerCase();

        if (json.has("type"))
            this.type = json.get("type").getAsString().toLowerCase();

        if (json.has("changeTheme"))
            this.changeTheme = json.get("changeTheme").getAsBoolean();

        this.sizeMin = JSONHelper.getVector3i(json, "sizeMin");

        this.sizeMax = JSONHelper.getVector3i(json, "sizeMax");

        if (json.has("weight"))
            this.weight = json.get("weight").getAsInt();

        if (json.has("structures")) {
            for (JsonElement jsonElement : json.get("structures").getAsJsonArray()) {
                String structureName = jsonElement.getAsString();
                if (!this.structures.contains(structureName))
                    this.structures.add(structureName);
            }
        }

        if (json.has("floor")) {
            this.floor = new SectorSegment();
            this.floor.loadFromJSON(json.get("floor"));
        }

        if (json.has("wall")) {
            this.wall = new SectorSegment();
            this.wall.loadFromJSON(json.get("wall"));
        }

        if (json.has("ceiling")) {
            this.ceiling = new SectorSegment();
            this.ceiling.loadFromJSON(json.get("ceiling"));
        }

        this.padding = JSONHelper.getVector3i(json, "padding");
        if (this.padding.getX() <= 0) {
            this.padding = new Vec3i(this.wall.getPadding(), this.padding.getY(), this.padding.getZ());
        }
        if (this.padding.getZ() <= 0) {
            this.padding = new Vec3i(this.padding.getX(), this.padding.getY(), this.wall.getPadding());
        }
    }


    /**
     * Returns a random room size to use based on the size min max values.
     *
     * @param random The instance of Random to use.
     * @return A random size to use.
     */
    public Vector3i getRandomSize(RandomSource random) {
        int x = this.sizeMin.getX();
        if (this.sizeMax.getX() > x)
            x += random.nextInt(this.sizeMax.getX() - x + 1);
        int y = this.sizeMin.getY();
        if (this.sizeMax.getY() > y)
            y += random.nextInt(this.sizeMax.getY() - y + 1);
        int z = this.sizeMin.getZ();
        if (this.sizeMax.getZ() > z)
            z += random.nextInt(this.sizeMax.getZ() - z + 1);
        return new Vector3i(x, y, z);
    }

}
