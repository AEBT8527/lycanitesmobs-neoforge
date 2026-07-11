package com.lycanitesmobs.core.entity.goals.actions;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.base.BaseCreatureEntity;
import com.lycanitesmobs.core.util.helpers.LMHelperClass;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

public class FindNearbyPlayersGoal extends Goal {
    BaseCreatureEntity host;

    // Properties:
    private double searchRange = 64D;

    private int searchTime = 0;
    private int searchRate = 20;


    /**
     * Constrcutor
     *
     * @param setHost The creature using this goal.
     */
    public FindNearbyPlayersGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

    /**
     * Sets the player search range (in blocks).
     *
     * @param searchRange The range to find blocks.
     * @return This goal for chaining.
     */
    public FindNearbyPlayersGoal setSearchRange(double searchRange) {
        this.searchRange = searchRange;
        return this;
    }

    @Override
    public boolean canUse() {
        return this.host.isAlive();
    }

    @Override
    public void tick() {
        if (this.searchTime++ % this.searchRate != 0) {
            return;
        }

        try {
            this.host.clearPlayerTargets();
            for (Player player : this.host.level().players()) {
                if (this.host.distanceTo(player) <= this.searchRange) {
                    this.host.addPlayerTarget(player);
                }
            }

        } catch (Exception e) {
            LMHelperClass.logWarningMessage("An exception occurred when player target selecting, this has been skipped to prevent a crash.");
            e.printStackTrace();
        }
    }
}
