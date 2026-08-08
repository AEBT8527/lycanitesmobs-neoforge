package com.lycanitesmobs.core.worldgen.dungeon.instance;

import com.lycanitesmobs.core.block.base.BlockFireBase;
import com.lycanitesmobs.core.block.cloud.BlockFrostCloud;
import com.lycanitesmobs.core.block.cloud.BlockPoisonCloud;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.spawner.MobSpawn;
import com.lycanitesmobs.core.manager.DeferredLevelActionManager;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import com.lycanitesmobs.core.worldgen.dungeon.DeferredBossSpawner;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonSector;
import com.lycanitesmobs.core.worldgen.dungeon.definition.DungeonTheme;
import com.lycanitesmobs.core.worldgen.dungeon.definition.SectorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SectorInstance {
    private boolean bossRoomSpawnQueued = false;

    /** Sector Instances makeup an entire Dungeon Layout. **/

    /**
     * The Dungeon Layout that this instance belongs to.
     **/
    protected DungeonLayout layout;

    /**
     * The Dungeon Sector that this instance is using.
     **/
    protected DungeonSector dungeonSector;

    /**
     * The connector that this sector is connected to, cannot be null.
     **/
    protected SectorConnector parentConnector;

    /**
     * A list of connectors that this sector provides to connect to other sectors
     * with.
     **/
    protected List<SectorConnector> connectors = new ArrayList<>();

    /**
     * The room size of this Sector Instance, this includes the inside and inner
     * floor, walls and ceiling. Used for building and sector to sector collision.
     **/
    protected Vector3i roomSize;

    /**
     * The occupied size of this Sector Instance, includes the room size plus
     * additional space taken up by sector layers, structures, padding, etc.
     **/
    protected Vector3i occupiedSize;

    /**
     * The theme this Sector Instance is using.
     **/
    protected DungeonTheme theme;

    /**
     * The random light block for this sector instance to use.
     **/
    protected BlockState lightBlock;

    /**
     * The random torch block for this sector instance to use.
     **/
    protected BlockState torchBlock;

    /**
     * The random stairs block for this sector instance to use.
     **/
    protected BlockState stairBlock;

    /**
     * The random pit block for this sector instance to use.
     **/
    protected BlockState pitBlock;

    /**
     * How many chunks this sector has been built into. When this equals the total
     * chunks this sector occupies it is considered fully built.
     **/
    protected int chunksBuilt = 0;

    /**
     * Constructor
     *
     * @param layout        The Dungeon Layout to create this instance for.
     * @param dungeonSector The Dungeon Sector to create this instance from.
     * @param random        The instance of Random to use.
     */
    public SectorInstance(DungeonLayout layout, DungeonSector dungeonSector, RandomSource random) {
        this.layout = layout;
        this.dungeonSector = dungeonSector;

        // Size:
        this.roomSize = this.dungeonSector.getRandomSize(random);
        this.occupiedSize = new Vector3i(
                this.roomSize.x() + Math.max(1, this.dungeonSector.getPadding().getX()),
                this.roomSize.y() + this.dungeonSector.getPadding().getY(),
                this.roomSize.z() + Math.max(1, this.dungeonSector.getPadding().getZ()));

        // Structures:
        // TODO Structures
    }

    public BlockState getLightBlock() {
        return this.lightBlock;
    }

    public BlockState getTorchBlock() {
        return this.torchBlock;
    }

    public BlockState getStairBlock() {
        return this.stairBlock;
    }

    public BlockState getPitBlock() {
        return this.pitBlock;
    }

    public String getSectorType() {
        return this.dungeonSector.getType();
    }

    int getConnectorLevel() {
        return this.parentConnector.level;
    }

    long getBuildPlanSeed() {
        return this.layout.dungeonInstance.seed ^ this.hashCode();
    }

    int getDungeonMaxBuildHeight() {
        return this.layout.dungeonInstance.world.getMaxY();
    }

    Level getDungeonWorld() {
        return this.layout.dungeonInstance.world;
    }

    boolean isDungeonWaterlogged() {
        return this.layout.dungeonInstance.schematic.isWaterlogged();
    }

    MobSpawn getRandomDungeonMobSpawn(boolean boss, RandomSource random) {
        return this.layout.dungeonInstance.schematic.getRandomMobSpawn(this.getConnectorLevel(), boss, random);
    }

    Identifier getRandomDungeonLootTable(RandomSource random) {
        return this.layout.dungeonInstance.schematic.getRandomLootTable(this.getConnectorLevel(), random);
    }

    Map<Integer, SectorLayer> getFloorLayers() {
        return this.dungeonSector.getFloor().getLayers();
    }

    Map<Integer, SectorLayer> getWallLayers() {
        return this.dungeonSector.getWall().getLayers();
    }

    Map<Integer, SectorLayer> getCeilingLayers() {
        return this.dungeonSector.getCeiling().getLayers();
    }

    BlockState getFloorBlock(char buildChar, RandomSource random) {
        return this.theme.getFloor(this, buildChar, random);
    }

    BlockState getWallBlock(char buildChar, RandomSource random) {
        return this.theme.getWall(this, buildChar, random);
    }

    BlockState getCeilingBlock(char buildChar, RandomSource random) {
        return this.theme.getCeiling(this, buildChar, random);
    }

    /**
     * Connects this sector to the provided connector. Should be called before init.
     *
     * @param parentConnector The connector that this sector is connecting from.
     */
    public void connect(SectorConnector parentConnector) {
        this.parentConnector = parentConnector;
    }

    boolean hasParentConnector() {
        return this.parentConnector != null;
    }

    SectorConnectorAnchor getParentConnectorAnchor() {
        return new SectorConnectorAnchor(this.parentConnector.position, this.parentConnector.facing, this.parentConnector.level);
    }

    void attachToParentConnector() {
        this.parentConnector.attachChild(this);
        this.layout.closeOpenConnector(this.parentConnector);
    }

    void initializeTheme(RandomSource random) {
        SectorInstance parentSector = this.parentConnector.getParentSector();
        if (this.dungeonSector.changesTheme() || parentSector == null) {
            this.theme = this.layout.dungeonInstance.schematic.getRandomTheme(random);
        } else {
            this.theme = parentSector.theme;
        }
        this.lightBlock = this.theme.getLight('B', random);
        this.torchBlock = this.theme.getTorch('B', random);
        this.stairBlock = this.theme.getStairs('B', random);
        this.pitBlock = this.theme.getPit('B', random);
    }

    SectorConnector addChildConnector(BlockPos blockPos, int level, Direction facing) {
        SectorConnector connector = new SectorConnector(blockPos, this, level, facing);
        this.connectors.add(connector);
        return connector;
    }

    /**
     * Initialises this Sector Instance. Must be connected to a parent connector.
     *
     * @param random The instance of Random to use.
     */
    public void init(RandomSource random) {
        if (this.parentConnector == null) {
            LMHelperClass.logWarning("Dungeon", "Skipping SectorInstance.init due to null parentConnector: " + this);
            return;
        }

        this.attachToParentConnector();
        this.initializeTheme(random);
        this.createChildConnectors(random);
    }

    private void createChildConnectors(RandomSource random) {
        BlockPos boundsMin = this.getRoomBoundsMin();
        BlockPos boundsMax = this.getRoomBoundsMax();
        Vector3i size = this.getRoomSize();

        BlockPos upperConnector = this.getUpperConnectorPosition();
        if (upperConnector.getY() < 255) {
            this.addChildConnector(upperConnector, this.getConnectorLevel() + 1, Direction.UP);
        }

        if (this.hasHorizontalChildConnectors()) {
            this.createHorizontalConnectors(random, boundsMin, boundsMax, size);
        }
        if (this.hasLowerStairsChildConnector()) {
            this.createLowerStairsConnector();
        }
    }

    private void createHorizontalConnectors(RandomSource random, BlockPos boundsMin, BlockPos boundsMax, Vector3i size) {
        SectorConnectorAnchor anchor = this.getParentConnectorAnchor();
        BlockPos frontPos = anchor.position();
        Direction frontFacing = Direction.SOUTH;
        BlockPos backPos = anchor.position();
        Direction backFacing = Direction.NORTH;
        if (anchor.facing() == Direction.SOUTH || anchor.facing() == Direction.UP) {
            frontPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                    anchor.y(), boundsMax.getZ() + 1);
            frontFacing = Direction.SOUTH;
            backPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                    anchor.y(), boundsMin.getZ() - 1);
            backFacing = Direction.NORTH;
        } else if (anchor.facing() == Direction.EAST) {
            frontPos = new BlockPos(boundsMax.getX() + 1, anchor.y(),
                    this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
            frontFacing = Direction.EAST;
            backPos = new BlockPos(boundsMin.getX() - 1, anchor.y(),
                    this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
            backFacing = Direction.WEST;
        } else if (anchor.facing() == Direction.NORTH) {
            frontPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                    anchor.y(), boundsMin.getZ() - 1);
            frontFacing = Direction.NORTH;
            backPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                    anchor.y(), boundsMax.getZ() + 1);
            backFacing = Direction.SOUTH;
        } else if (anchor.facing() == Direction.WEST) {
            frontPos = new BlockPos(boundsMin.getX() - 1, anchor.y(),
                    this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
            frontFacing = Direction.WEST;
            backPos = new BlockPos(boundsMax.getX() + 1, anchor.y(),
                    this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
            backFacing = Direction.EAST;
        }
        this.addChildConnector(frontPos, anchor.level(), frontFacing);
        if (this.hasBackChildConnector()) {
            this.addChildConnector(backPos, anchor.level(), backFacing);
        }

        if (this.hasSideChildConnectors()) {
            this.createSideConnectors(random, boundsMin, boundsMax, size);
        }
    }

    private void createSideConnectors(RandomSource random, BlockPos boundsMin, BlockPos boundsMax, Vector3i size) {
        SectorConnectorAnchor anchor = this.getParentConnectorAnchor();
        BlockPos leftPos = anchor.position();
        Direction leftFacing = Direction.WEST;
        BlockPos rightPos = anchor.position();
        Direction rightFacing = Direction.EAST;
        if (anchor.facing() == Direction.SOUTH || anchor.facing() == Direction.NORTH
                || anchor.facing() == Direction.UP) {
            leftPos = new BlockPos(boundsMin.getX() - 1, anchor.y(),
                    this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
            leftFacing = Direction.WEST;
            rightPos = new BlockPos(boundsMax.getX() + 1, anchor.y(),
                    this.getConnectorOffset(random, size.z(), boundsMin.getZ()));
            rightFacing = Direction.EAST;
        } else if (anchor.facing() == Direction.EAST
                || anchor.facing() == Direction.WEST) {
            leftPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                    anchor.y(), boundsMax.getZ() + 1);
            leftFacing = Direction.SOUTH;
            rightPos = new BlockPos(this.getConnectorOffset(random, size.x(), boundsMin.getX()),
                    anchor.y(), boundsMin.getZ() - 1);
            rightFacing = Direction.NORTH;
        }
        this.addChildConnector(leftPos, anchor.level(), leftFacing);
        this.addChildConnector(rightPos, anchor.level(), rightFacing);
    }

    private void createLowerStairsConnector() {
        BlockPos blockPos = this.getLowerStairsConnectorPosition();
        if (blockPos.getY() <= 0) {
            return;
        }

        SectorConnectorAnchor anchor = this.getParentConnectorAnchor();
        this.addChildConnector(blockPos, anchor.level() - 1, anchor.facing());
    }

    /**
     * Returns a random child connector for a Sector Instance to connect to.
     *
     * @param random The instance of Random to use.
     * @return A random connector.
     */
    public SectorConnector getRandomConnector(RandomSource random, SectorInstance sectorInstance) {
        List<SectorConnector> openConnectors = this.getOpenConnectors(sectorInstance);
        if (openConnectors.isEmpty()) {
            return null;
        }
        if (openConnectors.size() == 1) {
            return openConnectors.get(0);
        }
        return openConnectors.get(random.nextInt(openConnectors.size()));
    }

    /**
     * Returns a list of open connectors where they are not set to closed and have
     * no child Sector Instance connected.
     *
     * @param sectorInstance The sector to get the open connectors for. If null,
     *                       collision checks are skipped.
     * @return A list of open connectors.
     */
    public List<SectorConnector> getOpenConnectors(SectorInstance sectorInstance) {
        List<SectorConnector> openConnectors = new ArrayList<>();
        for (SectorConnector connector : this.connectors) {
            if (connector.canConnect(this.layout, sectorInstance)) {
                openConnectors.add(connector);
            }
        }
        return openConnectors;
    }

    /**
     * Returns a list of every ChunkPos that this Sector Instance occupies.
     * Applies an offset to occupied bounds for generating a chunk position.
     *
     * @return A list of ChunkPos.
     */
    public List<ChunkPos> getChunkPositions() {
        ChunkPos minChunkPos = ChunkPos.containing(this.getOccupiedBoundsMin().offset(-1, 0, -1));
        ChunkPos maxChunkPos = ChunkPos.containing(this.getOccupiedBoundsMax().offset(1, 0, 1));
        List<ChunkPos> chunkPosList = new ArrayList<>();
        for (int x = minChunkPos.x(); x <= maxChunkPos.x(); x++) {
            for (int z = minChunkPos.z(); z <= maxChunkPos.z(); z++) {
                chunkPosList.add(new ChunkPos(x, z));
            }
        }
        return chunkPosList;
    }

    /**
     * Returns a list of other sectors near this sector instance.
     *
     * @return A list of nearby sector instances.
     */
    public List<SectorInstance> getNearbySectors() {
        return this.layout.getNearbySectors(this);
    }

    /**
     * Returns true if this sector instance collides with the provided sector
     * instance.
     *
     * @param sectorInstance The sector instance to check for collision with.
     * @return True on collision.
     */
    public boolean collidesWith(SectorInstance sectorInstance) {
        if (sectorInstance == this || sectorInstance == this.parentConnector.getParentSector()) {
            return false;
        }

        BlockPos boundsMin = this.getOccupiedBoundsMin();
        BlockPos boundsMax = this.getOccupiedBoundsMax();
        BlockPos targetMin = sectorInstance.getOccupiedBoundsMin();
        BlockPos targetMax = sectorInstance.getOccupiedBoundsMax();

        if (boundsMin.getY() != targetMin.getY() && !sectorInstance.isSectorType("stairs")) {
            return false;
        }

        boolean withinX = boundsMin.getX() >= targetMin.getX() && boundsMin.getX() <= targetMax.getX();
        if (!withinX)
            withinX = boundsMax.getX() >= targetMin.getX() && boundsMax.getX() <= targetMax.getX();
        if (!withinX)
            return false;

        // boolean withinY = boundsMin.getY() >= targetMin.getY() && boundsMin.getY() <=
        // targetMax.getY();
        // if(!withinY)
        // withinY = boundsMax.getY() >= targetMin.getY() && boundsMax.getY() <=
        // targetMax.getY();
        // if(!withinY)
        // return false;

        boolean withinZ = boundsMin.getZ() >= targetMin.getZ() && boundsMin.getZ() <= targetMax.getZ();
        if (!withinZ)
            withinZ = boundsMax.getZ() >= targetMin.getZ() && boundsMax.getZ() <= targetMax.getZ();
        if (!withinZ)
            return false;

        return true;
    }

    /**
     * Returns the room size of this sector. X and Z are swapped when facing EAST or
     * WEST.
     * This is how large the room to be built is excluding extra blocks added for
     * layers or structures, etc.
     * Used for building this sector.
     *
     * @return A vector of the room size.
     */
    public Vector3i getRoomSize() {
        if (this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
            return new Vector3i(this.roomSize.z(), this.roomSize.y(), this.roomSize.x());
        }
        return this.roomSize;
    }

    /**
     * Returns the collision size of this sector. X and Z are swapped when facing
     * EAST or WEST.
     * This is how large this sector is including extra blocks added for layers or
     * structures, etc.
     * Used for detecting what chunks this sector needs to generate in and sector
     * collision detection.
     *
     * @return A vector of the collision size.
     */
    public Vector3i getOccupiedSize() {
        if (this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
            return new Vector3i(this.occupiedSize.z(), this.occupiedSize.y(), this.occupiedSize.x());
        }
        return this.occupiedSize;
    }

    /**
     * Returns the minimum xyz position that this Sector Instance from the provided
     * bounds size.
     *
     * @param boundsSize The xyz size to use when calculating bounds.
     * @return The minimum bounds position (corner).
     */
    public BlockPos getBoundsMin(Vector3i boundsSize) {
        BlockPos bounds = new BlockPos(this.parentConnector.position);
        if (this.parentConnector.facing == Direction.UP) {
            bounds = bounds.offset(
                    -(int) Math.ceil((double) boundsSize.x() / 2),
                    0,
                    -(int) Math.ceil((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.SOUTH) {
            bounds = bounds.offset(
                    -(int) Math.ceil((double) boundsSize.x() / 2),
                    0,
                    0);
        } else if (this.parentConnector.facing == Direction.EAST) {
            bounds = bounds.offset(
                    0,
                    0,
                    -(int) Math.ceil((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.NORTH) {
            bounds = bounds.offset(
                    -(int) Math.ceil((double) boundsSize.x() / 2),
                    0,
                    -boundsSize.z());
        } else if (this.parentConnector.facing == Direction.WEST) {
            bounds = bounds.offset(
                    -boundsSize.x(),
                    0,
                    -(int) Math.ceil((double) boundsSize.z() / 2));
        }

        return bounds;
    }

    /**
     * Returns the maximum xyz position that this Sector Instance from the provided
     * bounds size.
     *
     * @param boundsSize The xyz size to use when calculating bounds.
     * @return The maximum bounds position (corner).
     */
    public BlockPos getBoundsMax(Vector3i boundsSize) {
        BlockPos bounds = new BlockPos(this.parentConnector.position);
        if (this.parentConnector.facing == Direction.UP) {
            bounds = bounds.offset(
                    (int) Math.ceil((double) boundsSize.x() / 2),
                    boundsSize.y(),
                    (int) Math.ceil((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.SOUTH) {
            bounds = bounds.offset(
                    (int) Math.floor((double) boundsSize.x() / 2),
                    boundsSize.y(),
                    boundsSize.z());
        } else if (this.parentConnector.facing == Direction.EAST) {
            bounds = bounds.offset(
                    boundsSize.x(),
                    boundsSize.y(),
                    (int) Math.floor((double) boundsSize.z() / 2));
        } else if (this.parentConnector.facing == Direction.NORTH) {
            bounds = bounds.offset(
                    (int) Math.floor((double) boundsSize.x() / 2),
                    boundsSize.y(),
                    0);
        } else if (this.parentConnector.facing == Direction.WEST) {
            bounds = bounds.offset(
                    0,
                    boundsSize.y(),
                    (int) Math.floor((double) boundsSize.z() / 2));
        }
        return bounds;
    }

    /**
     * Returns the minimum xyz position that this Sector Instance occupies.
     *
     * @return The minimum bounds position (corner).
     */
    public BlockPos getOccupiedBoundsMin() {
        BlockPos occupiedBoundsMin = this.getBoundsMin(this.getOccupiedSize());
        if (this.isSectorType("stairs")) {
            occupiedBoundsMin = occupiedBoundsMin.subtract(new Vec3i(0, this.getRoomSize().y() * 2, 0));
        }
        return occupiedBoundsMin;
    }

    /**
     * Returns the maximum xyz position that this Sector Instance occupies.
     *
     * @return The maximum bounds position (corner).
     */
    public BlockPos getOccupiedBoundsMax() {
        return this.getBoundsMax(this.getOccupiedSize());
    }

    /**
     * Returns the minimum xyz position that this Sector Instance builds from.
     *
     * @return The minimum bounds position (corner).
     */
    public BlockPos getRoomBoundsMin() {
        return this.getBoundsMin(this.getRoomSize());
    }

    /**
     * Returns the maximum xyz position that this Sector Instance builds to.
     *
     * @return The maximum bounds position (corner).
     */
    public BlockPos getRoomBoundsMax() {
        return this.getBoundsMax(this.getRoomSize());
    }

    SectorBounds getRoomArea() {
        return SectorBounds.between(this.getRoomBoundsMin(), this.getRoomBoundsMax());
    }

    SectorBounds getRoomArea(int offsetY) {
        return this.getRoomArea().offset(0, offsetY, 0);
    }

    SectorBounds getWallArea() {
        SectorBounds roomArea = this.getRoomArea();
        return roomArea.withYRange(
                Math.min(roomArea.minY() + 1, roomArea.maxY()),
                Math.max(roomArea.minY() - 1, roomArea.maxY())
        );
    }

    SectorBounds getClearArea() {
        SectorBounds roomArea = this.getRoomArea();
        if (this.isSectorType("stairs")) {
            return roomArea.withMinY(Math.max(1, roomArea.minY() - (this.getRoomSize().y() * 2)));
        }
        return roomArea;
    }

    SectorBounds getWallBuildArea() {
        SectorBounds wallArea = this.getWallArea();
        if (this.isSectorType("stairs")) {
            return wallArea.withMinY(Math.max(1, this.getRoomArea().minY() - (this.getRoomSize().y() * 2)));
        }
        return wallArea;
    }

    SectorBounds getStairArea() {
        BlockPos center = this.getCenter();
        int stairsHeight = this.parentConnector.getParentSector().getOccupiedSize().y() - 1;
        if (this.isSectorType("stairs")) {
            stairsHeight = this.getRoomSize().y() * 2;
        }

        SectorBounds roomArea = this.getRoomArea();
        return new SectorBounds(
                center.getX() - 1,
                center.getX() + 1,
                Math.max(1, roomArea.minY() - stairsHeight),
                Math.min(roomArea.minY(), roomArea.maxY()),
                center.getZ() - 1,
                center.getZ() + 1
        );
    }

    BlockState getStairBuildState(SectorBounds stairArea, int x, int y, int z, RandomSource random) {
        BlockState blockState = Blocks.CAVE_AIR.defaultBlockState();
        int centerX = stairArea.minX() + 1;
        int centerZ = stairArea.minZ() + 1;

        if (x == centerX && z == centerZ) {
            blockState = this.theme.getWall(this, 'B', random);
        }

        int step = y % 8;
        int offsetX = x - stairArea.minX();
        int offsetZ = z - stairArea.minZ();
        BlockState floorBlockState = this.theme.getFloor(this, 'B', random);

        if (step % 4 == 3) {
            if (offsetX == 0 && offsetZ == 0) {
                blockState = floorBlockState;
            } else if (offsetX == 0 && offsetZ == 1) {
                blockState = this.stairBlock;
            }
        }
        if (step % 4 == 2) {
            if (offsetX == 0 && offsetZ == 2) {
                blockState = floorBlockState;
            } else if (offsetX == 1 && offsetZ == 2) {
                blockState = this.stairBlock.setValue(StairBlock.FACING, Direction.WEST);
            }
        }
        if (step % 4 == 1) {
            if (offsetX == 2 && offsetZ == 2) {
                blockState = floorBlockState;
            } else if (offsetX == 2 && offsetZ == 1) {
                blockState = this.stairBlock.setValue(StairBlock.FACING, Direction.SOUTH);
            }
        }
        if (step % 4 == 0) {
            if (offsetX == 2 && offsetZ == 0) {
                blockState = floorBlockState;
            } else if (offsetX == 1 && offsetZ == 0) {
                blockState = this.stairBlock.setValue(StairBlock.FACING, Direction.EAST);
            }
        }

        return blockState;
    }

    boolean isSectorType(String type) {
        return type.equalsIgnoreCase(this.dungeonSector.getType());
    }

    boolean hasHorizontalChildConnectors() {
        return this.isSectorType("corridor")
                || this.isSectorType("room")
                || this.isSectorType("tower")
                || this.isSectorType("entrance")
                || this.isSectorType("bossRoom");
    }

    boolean hasBackChildConnector() {
        return this.isSectorType("tower");
    }

    boolean hasSideChildConnectors() {
        return this.isSectorType("room") || this.isSectorType("tower");
    }

    boolean hasLowerStairsChildConnector() {
        return this.isSectorType("stairs");
    }

    BlockPos getUpperConnectorPosition() {
        BlockPos center = this.getCenter();
        return new BlockPos(center.getX(), this.parentConnector.position.getY() + this.getRoomSize().y(), center.getZ());
    }

    BlockPos getLowerStairsConnectorPosition() {
        SectorBounds roomArea = this.getRoomArea();
        BlockPos center = this.getCenter();
        int y = this.parentConnector.position.getY() - (this.getRoomSize().y() * 2);

        if (this.parentConnector.facing == Direction.EAST) {
            return new BlockPos(roomArea.maxX() + 1, y, center.getZ());
        }
        if (this.parentConnector.facing == Direction.NORTH) {
            return new BlockPos(center.getX(), y, roomArea.minZ() - 1);
        }
        if (this.parentConnector.facing == Direction.WEST) {
            return new BlockPos(roomArea.minX() - 1, y, center.getZ());
        }
        return new BlockPos(center.getX(), y, roomArea.maxZ() + 1);
    }

    int getConnectorOffset(RandomSource random, int length, int start) {
        int entrancePadding = 2;
        if (!this.isSectorType("room") || length <= entrancePadding * 2) {
            return start + Math.round((float) length / 2);
        }
        return start + entrancePadding + random.nextInt(length - (entrancePadding * 2)) + 1;
    }

    /**
     * Returns the rounded center block position of this sector.
     *
     * @return The center block position.
     */
    public BlockPos getCenter() {
        BlockPos startPos = this.getRoomBoundsMin();
        BlockPos stopPos = this.getRoomBoundsMax();

        Vector3i size = this.getRoomSize();
        int centerX = startPos.getX() + Math.round((float) size.x() / 2);
        int centerZ = startPos.getZ() + Math.round((float) size.z() / 2);

        return new BlockPos(centerX, startPos.getY(), centerZ);
    }

    /**
     * Places a block state in the world from this sector.
     *
     * @param worldWriter The world to place a block in. Cannot be the actual World
     *                    during WorldGen.
     * @param chunkPos    The chunk position to build within.
     * @param blockPos    The position to place the block at.
     * @param blockState  The block state to place.
     * @param random      The instance of random, used for random mob spawns or loot
     *                    on applicable blocks, etc.
     */
    public void placeBlock(LevelAccessor worldWriter, ChunkPos chunkPos, BlockPos blockPos, BlockState blockState,
                           Direction facing, RandomSource random) {
        int chunkOffset = 0;
        if (blockPos.getX() < chunkPos.getMinBlockX() + chunkOffset
                || blockPos.getX() > chunkPos.getMaxBlockX() + chunkOffset) {
            return;
        }
        if (blockPos.getY() < worldWriter.getMinY() || blockPos.getY() >= worldWriter.getMaxY()) {
            return;
        }
        if (blockPos.getZ() < chunkPos.getMinBlockZ() + chunkOffset
                || blockPos.getZ() > chunkPos.getMaxBlockZ() + chunkOffset) {
            return;
        }

        if (DungeonInstance.isActualPlacementDisabled()) {
            return;
        }

        if (blockState.getBlock() == Blocks.AIR && worldWriter.getBlockState(blockPos).getBlock() == Blocks.CHEST) {
            return;
        }

        int flags = this.getPlacementFlags(blockState);

        if (blockState.getBlock() instanceof WallTorchBlock) {
            blockState = blockState.setValue(WallTorchBlock.FACING, facing);
            flags = 0;
        }

        if (blockState.getBlock() == Blocks.CHEST) {
            blockState = blockState.setValue(ChestBlock.FACING, facing);
        }

        if (this.isDungeonWaterlogged() && blockPos.getY() < worldWriter.getSeaLevel()) {
            if (blockState.getBlock() == Blocks.AIR || blockState.getBlock() == Blocks.CAVE_AIR) {
                blockState = Blocks.WATER.defaultBlockState();
                flags = 2;
            } else if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
                blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, true);
            }
        }

        worldWriter.setBlock(blockPos, blockState, flags);
        // Only these two carry runtime data; the old code ran the whole roll for every block
        // placed in the dungeon.
        if (blockState.getBlock() == Blocks.SPAWNER || blockState.getBlock() == Blocks.CHEST) {
            this.applyPlacedBlockRuntimeData(worldWriter, blockPos, blockState, random);
        }
    }

    private int getPlacementFlags(BlockState blockState) {
        if (blockState.getBlock() == Blocks.AIR
                || blockState.getBlock() == Blocks.CAVE_AIR
                || (blockState.liquid() && !blockState.getFluidState().isSource())
                || blockState.getBlock() instanceof FireBlock
                || blockState.getBlock() instanceof BlockFireBase
                || blockState.getBlock() instanceof BlockPoisonCloud
                || blockState.getBlock() instanceof BlockFrostCloud) {
            return 0;
        }
        return 2;
    }

    private void applyPlacedBlockRuntimeData(LevelAccessor worldWriter, BlockPos blockPos, BlockState blockState, RandomSource random) {
        if (blockState.getBlock() == Blocks.SPAWNER) {
            BlockEntity tileEntity = worldWriter.getBlockEntity(blockPos);
            if (tileEntity instanceof SpawnerBlockEntity spawner) {
                MobSpawn mobSpawn = this.getRandomDungeonMobSpawn(false, random);
                if (mobSpawn != null && mobSpawn.getEntityType() != null) {
                    spawner.setEntityId(mobSpawn.getEntityType(), random);
                }
            }
            return;
        }

        if (blockState.getBlock() == Blocks.CHEST) {
            BlockEntity tileEntity = worldWriter.getBlockEntity(blockPos);
            if (tileEntity instanceof ChestBlockEntity chest) {
                var lootTable = this.getRandomDungeonLootTable(random);
                if (lootTable != null) {
                    chest.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, lootTable), Objects.hash(blockPos.hashCode(), random));
                }
            }
        }
    }

    void buildEntrance(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        this.parentConnector.buildEntrance(worldWriter, world, chunkPos, random);
    }

    /**
     * Builds this sector. Wont build at y level 0 or below, beyond world height or
     * outside of the chunk.
     *
     * @param world    The world to build in.
     * @param chunkPos The chunk position to build within.
     * @param random   The instance of random, used for characters that are random.
     */
    public void build(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
        SectorBuildSequence.generate(this, new DirectPlacementSink(worldWriter, world, chunkPos, random), random);
        this.buildEntrance(worldWriter, world, chunkPos, random);
        this.chunksBuilt++;
        this.enqueueBossRoomSpawn(worldWriter, random);
    }

    /**
     * Spawns a mob in this sector.
     *
     * @param worldWriter The world to create blocks in.
     * @param world       The world being built in. This cannot be used for
     *                    placement during WorldGen.
     * @param chunkPos    The chunk position to spawn within.
     * @param blockPos    The position to spawn the mob at.
     * @param mobSpawn    The Mob Spawn entry to use.
     * @param random      The instance of random, used for mob vacations where
     *                    applicable.
     */
    public void spawnMob(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, BlockPos blockPos,
                         MobSpawn mobSpawn, RandomSource random) {
        int chunkOffset = 8;
        if (blockPos.getX() < chunkPos.getMinBlockX() + chunkOffset
                || blockPos.getX() > chunkPos.getMaxBlockX() + chunkOffset) {
            return;
        }
        if (blockPos.getY() < worldWriter.getMinY() || blockPos.getY() >= worldWriter.getMaxY()) {
            return;
        }
        if (blockPos.getZ() < chunkPos.getMinBlockZ() + chunkOffset
                || blockPos.getZ() > chunkPos.getMaxBlockZ() + chunkOffset) {
            return;
        }

        Level entityWorld = (world != null) ? world
                : (worldWriter instanceof ServerLevelAccessor ? ((ServerLevelAccessor) worldWriter).getLevel() : null);
        if (entityWorld == null) {
            return;
        }

        LivingEntity entityLiving = mobSpawn.createEntity(entityWorld);
        if (entityLiving == null) {
            return;
        }
        entityLiving.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());

        if (entityLiving instanceof BaseCreatureEntity entityCreature) {
            Vector3i size = this.getRoomSize();
            int radius = Math.max(3, Math.max(size.x(), size.z()));
            entityCreature.setHome(blockPos.getX(), blockPos.getY(), blockPos.getZ(), radius);
        }

        mobSpawn.onSpawned(entityLiving, null);
        if (entityWorld instanceof ServerLevel serverLevel) {
            DeferredLevelActionManager.spawnEntity(serverLevel, blockPos,
                    "dungeon_room_spawn:" + this.hashCode() + ":" + blockPos.asLong(),
                    entityLiving);
            return;
        }
        DeferredLevelActionManager.spawnEntityNow(entityWorld, entityLiving);
    }

    private void enqueueBossRoomSpawn(LevelAccessor worldWriter, RandomSource random) {
        if (!this.isSectorType("bossRoom")) {
            return;
        }

        MobSpawn mobSpawn = this.getRandomDungeonMobSpawn(true, random);
        if (mobSpawn == null || !mobSpawn.hasResolvedEntitySource()) {
            return;
        }

        BlockPos bossPos = this.getCenter().offset(0, 1, 0);

        Vector3i size = this.getRoomSize();
        int radius = Math.max(3, Math.max(size.x(), size.z()));

        ResourceKey<Level> dimKey = null;
        ServerLevel serverLevel = null;
        if (this.getDungeonWorld() != null) {
            dimKey = this.getDungeonWorld().dimension();
            serverLevel = (ServerLevel) this.getDungeonWorld();
        } else if (worldWriter instanceof ServerLevelAccessor sla) {
            dimKey = sla.getLevel().dimension();
            serverLevel = sla.getLevel();
        }

        if (dimKey != null) {
            // Chunk build can reach this sector more than once; only the first queues the boss.
            synchronized (this) {
                if (this.bossRoomSpawnQueued) {
                    return;
                }
                this.bossRoomSpawnQueued = true;
            }
            DeferredBossSpawner.enqueue(dimKey, bossPos, mobSpawn, radius, serverLevel);
        }
    }

    /**
     * Formats this object into a String.
     *
     * @return A formatted string description of this object.
     */
    @Override
    public String toString() {
        String bounds = "";
        String size = "";
        if (this.parentConnector != null) {
            bounds = " Bounds: " + this.getOccupiedBoundsMin() + " to " + this.getOccupiedBoundsMax();
            size = " Occupies: " + this.getOccupiedSize();
        }
        return "Sector Instance Type: " + (this.dungeonSector == null ? "Unset" : this.dungeonSector.getType())
                + " Parent Connector Pos: " + (this.parentConnector == null ? "Unset" : this.parentConnector.position)
                + size + bounds;
    }

    public void generateBuildPlan(DungeonBuildPlan plan) {
        RandomSource random = RandomSource.create(this.getBuildPlanSeed());
        SectorBuildSequence.generate(this, new PlanPlacementSink(plan, this.getDungeonMaxBuildHeight()), random);
    }

    private class DirectPlacementSink implements SectorBuildSequence.PlacementSink {
        private final LevelAccessor worldWriter;
        private final Level world;
        private final ChunkPos chunkPos;
        private final RandomSource random;

        private DirectPlacementSink(LevelAccessor worldWriter, Level world, ChunkPos chunkPos, RandomSource random) {
            this.worldWriter = worldWriter;
            this.world = world;
            this.chunkPos = chunkPos;
            this.random = random;
        }

        @Override
        public SectorBounds clip(SectorBounds area) {
            return area.clipToChunk(this.chunkPos);
        }

        @Override
        public int maxBuildHeight() {
            return this.world.getMaxY();
        }

        @Override
        public void place(SectorBuildStep step) {
            SectorInstance.this.placeBlock(this.worldWriter, this.chunkPos, step.pos(), step.state(), step.facing(), this.random);
        }

        @Override
        public boolean includesEmptyStairSpace() {
            return true;
        }
    }

    private static class PlanPlacementSink implements SectorBuildSequence.PlacementSink {
        private final DungeonBuildPlan plan;
        private final int maxBuildHeight;

        private PlanPlacementSink(DungeonBuildPlan plan, int maxBuildHeight) {
            this.plan = plan;
            this.maxBuildHeight = maxBuildHeight;
        }

        @Override
        public SectorBounds clip(SectorBounds area) {
            return area;
        }

        @Override
        public int maxBuildHeight() {
            return this.maxBuildHeight;
        }

        @Override
        public void place(SectorBuildStep step) {
            this.plan.add(ChunkPos.containing(step.pos()), new DungeonBuildPlan.PlannedBlock(step.pos(), step.state(), step.facing()));
        }
    }

}

record SectorConnectorAnchor(BlockPos position, Direction facing, int level) {
    int y() {
        return this.position.getY();
    }
}
