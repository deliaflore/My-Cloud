package org.distributed.stumatchdistributed.model;


import java.util.*;

/**
 * Represents how file chunks are distributed across nodes.
 * Provides a mapping of node IDs to the chunks they store.
 *
 * @author Your Name
 * @version 1.0
 */
public class ChunkDistribution {
    private final Map<String, List<String>> nodeToChunks;
    private final String fileName;
    private final int totalChunks;

    public ChunkDistribution(String fileName, int totalChunks) {
        this.fileName = fileName;
        this.totalChunks = totalChunks;
        this.nodeToChunks = new HashMap<>();
    }

    /**
     * Records that a chunk has been stored on a specific node.
     */
    public void addChunkToNode(String nodeId, String chunkId) {
        nodeToChunks.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(chunkId);
    }

    /**
     * Returns an unmodifiable view of the distribution.
     */
    public Map<String, List<String>> getDistribution() {
        return Collections.unmodifiableMap(nodeToChunks);
    }

    public String getFileName() { return fileName; }
    public int getTotalChunks() { return totalChunks; }

    /**
     * Checks if all chunks have been distributed.
     */
    public boolean isComplete() {
        return nodeToChunks.values().stream()
                .mapToInt(List::size)
                .sum() == totalChunks;
    }
}
