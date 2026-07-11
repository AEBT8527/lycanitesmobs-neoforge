package com.lycanitesmobs.core.event.mobevent;

import com.lycanitesmobs.core.capabilities.level.ExtendedWorld;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MobEventPlayerServer {
    private static boolean testOnCreative = false;

    public static boolean shouldTestOnCreative() {
        return testOnCreative;
    }

    public static void setTestOnCreative(boolean testOnCreative) {
        MobEventPlayerServer.testOnCreative = testOnCreative;
    }

    // Properties:
    /**
     * The MobEvent to play from.
     **/
    private final MobEvent mobEvent;

    /**
     * Increases every tick that this event is active.
     **/
    private int ticks = 0;

    /**
     * The world that this event is active in.
     **/
    private final Level world;

    /**
     * The world time that this event started at.
     **/
    private long startedWorldTime = 0;

    /**
     * The player that triggered this event. Can be null as not all events are player specific.
     **/
    private Player player;

    /**
     * The origin position of this event. This is not always relevant.
     **/
    private BlockPos origin = new BlockPos(0, 0, 0);

    /**
     * The level of the mob event, higher levels are more difficult, will spawn more subspecies, have higher mob levels, etc.
     **/
    private int level = 1;

    /**
     * The specific variant to spawn mobs as, if below zero a random variant is chosen as normal.
     **/
    private int variant = -1;

    /**
     * True if the started event was already running and show display as 'Event Extended' in chat.
     **/
    private boolean extended = false;


    // ==================================================
    //                     Constructor
    // ==================================================
    public MobEventPlayerServer(MobEvent mobEvent, Level world) {
        this.mobEvent = mobEvent;
        this.world = world;
        if (world.isClientSide())
            LMHelperClass.logWarningMessage("Created a MobEventServer with a client side world, this shouldn't happen, things are going to get weird!");
    }

    public MobEvent getMobEvent() {
        return this.mobEvent;
    }

    public String getMobEventName() {
        return this.mobEvent != null ? this.mobEvent.getName() : "";
    }

    public int getTicks() {
        return this.ticks;
    }

    public Level getWorld() {
        return this.world;
    }

    public Player getPlayer() {
        return this.player;
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public int getLevel() {
        return this.level;
    }

    public int getVariant() {
        return this.variant;
    }

    public boolean isExtended() {
        return this.extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public void configure(Player player, BlockPos origin, int level, int variant) {
        this.player = player;
        this.origin = origin;
        this.level = level;
        this.variant = variant;
    }

    public boolean matchesEvent(String eventName) {
        return "".equals(eventName) || eventName.equals(this.getMobEventName());
    }

    public int getTriggerTick(double eventTime) {
        if (this.mobEvent == null) {
            return -1;
        }
        return Math.round((float)this.mobEvent.getDuration() * (float)eventTime);
    }


    // ==================================================
    //                       Start
    // ==================================================
    public void onStart() {
        this.startedWorldTime = world.getGameTime();
        this.ticks = 0;

        LMHelperClass.logInfoMessage("Mob Event " + (this.extended ? "Extended" : "Started") + ": " + this.mobEvent.getTitle().getString() + " In Dimension: " + this.world.dimension().identifier() + " Duration: " + (this.mobEvent.getDuration() / 20) + "secs");
    }

    public void changeStartedWorldTime(long newStartedTime) {
        this.startedWorldTime = newStartedTime;
        LMHelperClass.logInfoMessage("Mob Event Start Time Changed: " + this.mobEvent.getTitle().getString() + " In Dimension: " + this.world.dimension().identifier().toString() + " Duration: " + (this.mobEvent.getDuration() / 20) + "secs" + " Time Remaining: " + ((this.mobEvent.getDuration() - (this.world.getGameTime() - this.startedWorldTime)) / 20) + "secs");
    }


    // ==================================================
    //                      Finish
    // ==================================================
    public void onFinish() {
        LMHelperClass.logInfoMessage("Mob Event Finished: " + this.mobEvent.getTitle().getString());
    }


    // ==================================================
    //                      Update
    // ==================================================
    public void onUpdate() {
        if (this.world == null) {
            LMHelperClass.logWarningMessage("MobEventBase was trying to update without a world object, stopped!");
            return;
        } else if (this.world.isClientSide()) {
            LMHelperClass.logWarningMessage("MobEventBase was trying to update with a client side world, stopped!");
            return;
        }

        this.mobEvent.onUpdate(this.world, this.player, this.origin, this.level, this.ticks, this.variant);
        this.ticks++;

        // Stop Event When Time Runs Out:
        if (this.ticks >= this.mobEvent.getDuration()) {
            ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
            if ("world".equalsIgnoreCase(this.mobEvent.getChannel())) {
                worldExt.stopWorldEvent();
            } else {
                worldExt.stopMobEvent(this.mobEvent.getName());
            }
        }
    }
}
