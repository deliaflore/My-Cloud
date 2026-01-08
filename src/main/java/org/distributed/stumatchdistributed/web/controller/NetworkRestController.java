package org.distributed.stumatchdistributed.web.controller;

import org.distributed.stumatchdistributed.network.NetworkController;
import org.distributed.stumatchdistributed.node.EnhancedNodeStatus;
import org.distributed.stumatchdistributed.service.StorageMetricsService;
import org.distributed.stumatchdistributed.service.NodeManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for network operations.
 *
 * Design Pattern: Controller Pattern (MVC)
 * - Handles HTTP requests
 * - Delegates business logic to services
 * - Returns DTOs instead of domain models
 *
 * RESTful Design:
 * - GET for retrieving data
 * - POST for creating/triggering operations
 * - Proper HTTP status codes
 * - JSON responses
 *
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api/network")
@CrossOrigin(origins = "*") // Allow frontend access
public class NetworkRestController {
    private static final Logger log = LoggerFactory.getLogger(NetworkRestController.class);

    private final NetworkController networkController;
    private final StorageMetricsService metricsService;
    private final NodeManagementService nodeManagementService;

    /**
     * Constructor injection for better testability.
     */
    @Autowired
    public NetworkRestController(NetworkController networkController,
                                 StorageMetricsService metricsService,
                                 NodeManagementService nodeManagementService) {
        this.networkController = networkController;
        this.metricsService = metricsService;
        this.nodeManagementService = nodeManagementService;
    }

    /**
     * GET /api/network/nodes
     * Returns list of registered nodes with details including ports.
     *
     * Example response:
     * [
     *   {"nodeId": "node1", "host": "localhost", "port": 50051, "address": "localhost:50051"},
     *   {"nodeId": "node2", "host": "localhost", "port": 50052, "address": "localhost:50052"}
     * ]
     */
    @GetMapping("/nodes")
    public ResponseEntity<List<Map<String, Object>>> getNodes() {
        log.info("API request: GET /api/network/nodes");

        Map<String, Map<String, Object>> nodesWithDetails = networkController.getNodesWithDetails();
        List<Map<String, Object>> nodes = new ArrayList<>(nodesWithDetails.values());

        return ResponseEntity.ok(nodes);
    }

    /**
     * GET /api/network/status
     * Returns aggregated network statistics.
     *
     * Example response:
     * {
     *   "totalNodes": 3,
     *   "totalStorageBytes": 322122547200,
     *   "usedStorageBytes": 5242880,
     *   "utilizationPercent": 0.0016,
     *   "totalChunks": 5
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNetworkStatus() {
        log.info("API request: GET /api/network/status");

        Map<String, Object> stats = networkController.getNetworkStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * POST /api/network/nodes/register
     * Registers a new node in the network.
     *
     * Request body:
     * {
     *   "nodeId": "node1",
     *   "host": "localhost",
     *   "port": 50051
     * }
     */
    @PostMapping("/nodes/register")
    public ResponseEntity<?> registerNode(@RequestBody Map<String, Object> request) {
        try {
            String nodeId = (String) request.get("nodeId");
            String host = (String) request.get("host");
            Integer port = (Integer) request.get("port");

            log.info("API request: POST /api/network/nodes/register - nodeId={}", nodeId);

            // Validate input
            if (nodeId == null || host == null || port == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing required fields: nodeId, host, port"));
            }

            // Register node
            networkController.registerNode(nodeId, host, port);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Node registered successfully",
                    "nodeId", nodeId
            ));

        } catch (Exception e) {
            log.error("Failed to register node", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/nodes/enhanced/{nodeId}")
    public ResponseEntity<EnhancedNodeStatus> getEnhancedStatus(@PathVariable String nodeId) {
        // Return comprehensive status with lifecycle, disk, processes
        return null;
    }

    /**
     * POST /api/network/nodes/start
     * Starts a new node process.
     * 
     * Request body:
     * {
     *   "nodeId": "node6",
     *   "port": 50056,
     *   "storageGB": 100,
     *   "ramGB": 8
     * }
     */
    @PostMapping("/nodes/start")
    public ResponseEntity<?> startNode(@RequestBody Map<String, Object> request) {
        try {
            String nodeId = (String) request.get("nodeId");
            Integer port = request.get("port") != null ? 
                (request.get("port") instanceof Integer ? (Integer) request.get("port") : 
                 Integer.parseInt(request.get("port").toString())) : 50051;
            Integer storageGB = request.get("storageGB") != null ?
                (request.get("storageGB") instanceof Integer ? (Integer) request.get("storageGB") :
                 Integer.parseInt(request.get("storageGB").toString())) : 100;
            Integer ramGB = request.get("ramGB") != null ?
                (request.get("ramGB") instanceof Integer ? (Integer) request.get("ramGB") :
                 Integer.parseInt(request.get("ramGB").toString())) : 8;

            log.info("API request: POST /api/network/nodes/start - nodeId={}, port={}, storageGB={}, ramGB={}", 
                    nodeId, port, storageGB, ramGB);

            if (nodeId == null || nodeId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "nodeId is required"));
            }

            if (nodeManagementService.isNodeRunning(nodeId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Node " + nodeId + " is already running"));
            }

            boolean started = nodeManagementService.startNode(nodeId, port, storageGB, ramGB);
            
            if (started) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Node started successfully",
                        "nodeId", nodeId,
                        "port", port,
                        "storageGB", storageGB,
                        "ramGB", ramGB
                ));
            } else {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Failed to start node"));
            }

        } catch (Exception e) {
            log.error("Failed to start node", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/network/nodes/stop/{nodeId}
     * Stops a running node process.
     */
    @PostMapping("/nodes/stop/{nodeId}")
    public ResponseEntity<?> stopNode(@PathVariable String nodeId) {
        try {
            log.info("API request: POST /api/network/nodes/stop - nodeId={}", nodeId);

            if (!nodeManagementService.isNodeRunning(nodeId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Node " + nodeId + " is not running"));
            }

            boolean stopped = nodeManagementService.stopNode(nodeId);
            
            if (stopped) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Node stopped successfully",
                        "nodeId", nodeId
                ));
            } else {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Failed to stop node"));
            }

        } catch (Exception e) {
            log.error("Failed to stop node", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/network/nodes/running
     * Returns list of currently running node processes.
     */
    @GetMapping("/nodes/running")
    public ResponseEntity<Map<String, Object>> getRunningNodes() {
        log.info("API request: GET /api/network/nodes/running");
        
        Set<String> runningNodes = nodeManagementService.getRunningNodes();
        
        return ResponseEntity.ok(Map.of(
                "runningNodes", runningNodes,
                "count", runningNodes.size()
        ));
    }

    /**
     * POST /api/network/nodes/restart/{nodeId}
     * Restarts a node (stops then starts).
     */
    @PostMapping("/nodes/restart/{nodeId}")
    public ResponseEntity<?> restartNode(@PathVariable String nodeId, @RequestBody Map<String, Object> request) {
        try {
            log.info("API request: POST /api/network/nodes/restart - nodeId={}", nodeId);
            
            Integer port = request.get("port") != null ? 
                (request.get("port") instanceof Integer ? (Integer) request.get("port") : 
                 Integer.parseInt(request.get("port").toString())) : 50051;
            Integer storageGB = request.get("storageGB") != null ?
                (request.get("storageGB") instanceof Integer ? (Integer) request.get("storageGB") :
                 Integer.parseInt(request.get("storageGB").toString())) : 100;
            Integer ramGB = request.get("ramGB") != null ?
                (request.get("ramGB") instanceof Integer ? (Integer) request.get("ramGB") :
                 Integer.parseInt(request.get("ramGB").toString())) : 8;

            // Stop if running
            if (nodeManagementService.isNodeRunning(nodeId)) {
                nodeManagementService.stopNode(nodeId);
                Thread.sleep(1000); // Wait for graceful shutdown
            }

            // Start
            boolean started = nodeManagementService.startNode(nodeId, port, storageGB, ramGB);
            
            if (started) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Node restarted successfully",
                        "nodeId", nodeId
                ));
            } else {
                return ResponseEntity.internalServerError()
                        .body(Map.of("error", "Failed to restart node"));
            }

        } catch (Exception e) {
            log.error("Failed to restart node", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/network/nodes/{nodeId}
     * Deletes a single node (stops and unregisters).
     */
    @DeleteMapping("/nodes/{nodeId}")
    public ResponseEntity<?> deleteNode(@PathVariable String nodeId) {
        try {
            log.info("API request: DELETE /api/network/nodes/{}", nodeId);
            
            // Stop if running
            if (nodeManagementService.isNodeRunning(nodeId)) {
                nodeManagementService.stopNode(nodeId);
            }
            
            // Unregister from network
            networkController.unregisterNode(nodeId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Node deleted successfully",
                    "nodeId", nodeId
            ));

        } catch (Exception e) {
            log.error("Failed to delete node", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/network/nodes/delete-all
     * Stops all nodes.
     */
    @PostMapping("/nodes/delete-all")
    public ResponseEntity<?> deleteAllNodes() {
        try {
            log.info("API request: POST /api/network/nodes/delete-all");
            
            Set<String> runningNodes = nodeManagementService.getRunningNodes();
            
            // Stop all running nodes
            int stoppedCount = 0;
            for (String nodeId : runningNodes) {
                if (nodeManagementService.stopNode(nodeId)) {
                    stoppedCount++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All nodes stopped successfully",
                    "stoppedCount", stoppedCount
            ));

        } catch (Exception e) {
            log.error("Failed to stop all nodes", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/network/nodes/{nodeId}/stats
     * Gets detailed statistics for a specific node.
     */
    @GetMapping("/nodes/{nodeId}/stats")
    public ResponseEntity<?> getNodeStats(@PathVariable String nodeId) {
        try {
            log.info("API request: GET /api/network/nodes/{}/stats", nodeId);
            
            // Check if node is running and registered
            boolean isRunning = nodeManagementService.isNodeRunning(nodeId);
            boolean isRegistered = networkController.getRegisteredNodes().contains(nodeId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("nodeId", nodeId);
            stats.put("isRunning", isRunning);
            stats.put("isRegistered", isRegistered);
            stats.put("status", isRunning ? "running" : isRegistered ? "registered" : "offline");
            
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Failed to get node stats", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
