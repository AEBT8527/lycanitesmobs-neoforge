package com.lycanitesmobs.core.entity.navigation;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArenaNodeNetwork {
    protected final Level world;

    /** A list of all nodes in this network. **/
    private final List<ArenaNode> nodes = new ArrayList<ArenaNode>();

    /** The central or starting point of the arena. **/
    private ArenaNode centralNode;

    // ==================================================
    //                    Constructor
    // ==================================================
    public ArenaNodeNetwork(Level world) {
        this.world = world;
    }

    public ArenaNode getCentralNode() {
        return this.centralNode;
    }

    public void setCentralNode(ArenaNode centralNode) {
        this.centralNode = centralNode;
    }

    public List<ArenaNode> getNodes() {
        return Collections.unmodifiableList(this.nodes);
    }


    // ==================================================
    //                      Nodes
    // ==================================================
    public void addNode(ArenaNode node, ArenaNode... adjacentNodes) {
        if(node == null || this.nodes.contains(node))
            return;
        this.addNode(node);
        for(ArenaNode adjacentNode : adjacentNodes)
            node.addAdjacentNode(adjacentNode);
    }

    public void addNode(ArenaNode node) {
        if(node == null || this.nodes.contains(node))
            return;
        this.nodes.add(node);
    }


    // ==================================================
    //                   Navigation
    // ==================================================
    public ArenaNode getClosestNode(BlockPos targetPos) {
        int nodesSize = this.nodes.size();
        if(nodesSize <= 0)
            return null;
        if(nodesSize == 1)
            return this.nodes.get(0);
        double smallestDistance = 0;
        ArenaNode closestNode = null;
        for(ArenaNode node : this.nodes) {
            double distance = targetPos.distSqr(node.getPos());
            if(closestNode == null || distance < smallestDistance) {
                smallestDistance = distance;
                closestNode = node;
            }
        }
        return closestNode;
    }
}
