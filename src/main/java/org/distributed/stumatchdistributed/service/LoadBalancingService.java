package org.distributed.stumatchdistributed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsible for load balancing decisions.
 * Strategy Pattern: Can be extended to support different load balancing algorithms.
 *
 * Current implementation: Round-robin distribution
 *
 * @author Your Name
 * @version 1.0
 */
@Service
public class LoadBalancingService {
    private static final Logger log = LoggerFactory.getLogger(LoadBalancingService.class);
    private final AtomicInteger nextNodeIndex = new AtomicInteger(0);

    /**
     * Selects the next node to store a chunk using round-robin algorithm.
     * Thread-safe implementation using AtomicInteger.
     *
     * @param availableNodes List of available node IDs
     * @return Selected node ID
     * @throws IllegalArgumentException if node list is empty
     */
    public String selectNodeForChunk(List<String> availableNodes) {
        if (availableNodes.isEmpty()) {
            throw new IllegalArgumentException("No nodes available for chunk storage");
        }

        int index = nextNodeIndex.getAndIncrement() % availableNodes.size();
        String selectedNode = availableNodes.get(index);

        log.debug("Selected node {} for chunk storage (round-robin)", selectedNode);
        return selectedNode;
    }

    /**
     * Resets the load balancer state.
     * Useful for testing or when restarting distribution.
     */
    public void reset() {
        nextNodeIndex.set(0);
        log.info("Load balancer reset");
    }
}
