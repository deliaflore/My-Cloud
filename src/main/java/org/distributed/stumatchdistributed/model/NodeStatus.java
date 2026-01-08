package org.distributed.stumatchdistributed.model;


import lombok.Getter;

/**
 * Represents the current status of a storage node.
 * Immutable value object following Domain-Driven Design principles.
 *
 * @author Your Name
 * @version 1.0
 */
@Getter
public class NodeStatus {
    // Getters only (immutable)
    private final String nodeId;
    private final long usedStorageBytes;
    private final long totalStorageBytes;
    private final int numChunks;
    private final double utilizationPercent;

    /**
     * Constructs a NodeStatus snapshot.
     *
     * @param nodeId Unique identifier for the node
     * @param usedStorageBytes Current storage used in bytes
     * @param totalStorageBytes Total storage capacity in bytes
     * @param numChunks Number of chunks currently stored
     * @param utilizationPercent Storage utilization percentage (0-100)
     */
    public NodeStatus(String nodeId, long usedStorageBytes, long totalStorageBytes,
                      int numChunks, double utilizationPercent) {
        this.nodeId = nodeId;
        this.usedStorageBytes = usedStorageBytes;
        this.totalStorageBytes = totalStorageBytes;
        this.numChunks = numChunks;
        this.utilizationPercent = utilizationPercent;
    }

    @Override
    public String toString() {
        return String.format("NodeStatus{id='%s', utilization=%.2f%%, chunks=%d}",
                nodeId, utilizationPercent, numChunks);
    }
}
