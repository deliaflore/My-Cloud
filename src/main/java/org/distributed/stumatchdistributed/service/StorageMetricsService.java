package org.distributed.stumatchdistributed.service;


import org.distributed.stumatchdistributed.model.NodeStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for tracking and aggregating storage metrics across the network.
 * Observer Pattern: Collects and monitors node statistics.
 *
 * @author Your Name
 * @version 1.0
 */
@Service
public class StorageMetricsService {
    private final Map<String, NodeStatus> nodeStatuses = new ConcurrentHashMap<>();

    /**
     * Updates the cached status for a node.
     */
    public void updateNodeStatus(NodeStatus status) {
        nodeStatuses.put(status.getNodeId(), status);
    }

    /**
     * Calculates aggregate network statistics.
     *
     * @return Map of metric names to values
     */
    public Map<String, Object> getNetworkMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long totalStorage = 0;
        long usedStorage = 0;
        int totalChunks = 0;

        for (NodeStatus status : nodeStatuses.values()) {
            totalStorage += status.getTotalStorageBytes();
            usedStorage += status.getUsedStorageBytes();
            totalChunks += status.getNumChunks();
        }

        double utilizationPercent = totalStorage > 0
                ? (usedStorage * 100.0 / totalStorage)
                : 0;

        metrics.put("totalNodes", nodeStatuses.size());
        metrics.put("totalStorageBytes", totalStorage);
        metrics.put("usedStorageBytes", usedStorage);
        metrics.put("utilizationPercent", utilizationPercent);
        metrics.put("totalChunks", totalChunks);

        return metrics;
    }

    /**
     * Gets status for a specific node.
     */
    public NodeStatus getNodeStatus(String nodeId) {
        return nodeStatuses.get(nodeId);
    }

    /**
     * Formats bytes into human-readable format.
     * Utility method for displaying metrics.
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
