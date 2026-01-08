package org.distributed.stumatchdistributed.node;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the complete lifecycle of a storage node.
 *
 * Node States (like OS process states):
 * - CREATED: Node object created but not started
 * - READY: Node initialized and ready to accept requests
 * - RUNNING: Node actively processing requests
 * - WAITING: Node idle, waiting for requests
 * - STOPPED: Node gracefully stopped
 * - DEAD: Node crashed or forcefully terminated
 *
 * @author Your Name
 * @version 1.0
 */
public class NodeLifecycleManager {
    private static final Logger log = LoggerFactory.getLogger(NodeLifecycleManager.class);

    private final String nodeId;
    private volatile NodeState currentState;
    private final ExecutorService processExecutor;
    private final ScheduledExecutorService heartbeatExecutor;

    // Lifecycle timestamps
    private final AtomicLong createdTime;
    private final AtomicLong startedTime;
    private final AtomicLong stoppedTime;

    // Heartbeat tracking
    private final AtomicLong lastHeartbeat;
    private static final long HEARTBEAT_INTERVAL_MS = 5000;  // 5 seconds
    private static final long DEAD_THRESHOLD_MS = 15000;     // 15 seconds no heartbeat = dead

    public enum NodeState {
        CREATED("Node created, not yet started"),
        READY("Node ready to accept requests"),
        RUNNING("Node actively processing"),
        WAITING("Node idle, waiting for work"),
        STOPPED("Node gracefully stopped"),
        DEAD("Node crashed or unresponsive");

        private final String description;

        NodeState(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    public NodeLifecycleManager(String nodeId) {
        this.nodeId = nodeId;
        this.currentState = NodeState.CREATED;
        this.createdTime = new AtomicLong(System.currentTimeMillis());
        this.startedTime = new AtomicLong(0);
        this.stoppedTime = new AtomicLong(0);
        this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());

        // Thread pool for processing (simulates OS process)
        this.processExecutor = Executors.newFixedThreadPool(4,
                new ThreadFactory() {
                    private int threadId = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName(nodeId + "-worker-" + (threadId++));
                        t.setDaemon(true);
                        return t;
                    }
                });

        // Heartbeat monitor
        this.heartbeatExecutor = Executors.newScheduledThreadPool(1);

        log.info("🔧 Node Lifecycle Manager created for: {}", nodeId);
        log.info("   State: {}", currentState);
    }

    /**
     * Activates the node (transitions CREATED → READY).
     * Like booting an OS.
     */
    public synchronized boolean activate() {
        if (currentState != NodeState.CREATED && currentState != NodeState.STOPPED) {
            log.warn("⚠️ Cannot activate node in state: {}", currentState);
            return false;
        }

        log.info("🚀 Activating node: {}", nodeId);

        try {
            // Initialization steps
            startedTime.set(System.currentTimeMillis());

            // Start heartbeat monitoring
            startHeartbeat();

            // Transition to READY
            transitionTo(NodeState.READY);

            // After a moment, transition to WAITING (idle)
            processExecutor.submit(() -> {
                try {
                    Thread.sleep(1000);  // Simulate initialization
                    transitionTo(NodeState.WAITING);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            log.info("✅ Node activated: {}", nodeId);
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to activate node", e);
            transitionTo(NodeState.DEAD);
            return false;
        }
    }

    /**
     * Starts processing a request (transitions WAITING → RUNNING).
     */
    public synchronized boolean startProcessing(Runnable task, String taskDescription) {
        if (currentState != NodeState.WAITING && currentState != NodeState.READY) {
            log.warn("⚠️ Cannot process in state: {}", currentState);
            return false;
        }

        log.info("▶️  Starting task: {} on {}", taskDescription, nodeId);

        transitionTo(NodeState.RUNNING);

        // Execute task
        processExecutor.submit(() -> {
            try {
                updateHeartbeat();  // Node is alive
                task.run();

                // After task completes, return to WAITING
                transitionTo(NodeState.WAITING);

            } catch (Exception e) {
                log.error("❌ Task failed", e);
                transitionTo(NodeState.DEAD);
            }
        });

        return true;
    }

    /**
     * Gracefully stops the node (transitions → STOPPED).
     */
    public synchronized boolean stop() {
        log.info("🛑 Stopping node: {}", nodeId);

        try {
            stoppedTime.set(System.currentTimeMillis());

            // Stop accepting new tasks
            processExecutor.shutdown();

            // Wait for running tasks to complete
            if (!processExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                processExecutor.shutdownNow();
            }

            // Stop heartbeat
            heartbeatExecutor.shutdown();

            transitionTo(NodeState.STOPPED);

            log.info("✅ Node stopped: {}", nodeId);
            return true;

        } catch (Exception e) {
            log.error("❌ Failed to stop node", e);
            transitionTo(NodeState.DEAD);
            return false;
        }
    }

    /**
     * Force kills the node (transitions → DEAD).
     * Simulates a crash.
     */
    public synchronized void kill() {
        log.warn("💀 Killing node: {}", nodeId);

        processExecutor.shutdownNow();
        heartbeatExecutor.shutdownNow();

        transitionTo(NodeState.DEAD);
    }

    /**
     * Checks if node is alive based on heartbeat.
     */
    public boolean isAlive() {
        long timeSinceHeartbeat = System.currentTimeMillis() - lastHeartbeat.get();

        if (timeSinceHeartbeat > DEAD_THRESHOLD_MS && currentState != NodeState.STOPPED) {
            log.error("💀 Node {} is DEAD (no heartbeat for {} ms)", nodeId, timeSinceHeartbeat);
            transitionTo(NodeState.DEAD);
            return false;
        }

        return currentState != NodeState.DEAD && currentState != NodeState.STOPPED;
    }

    /**
     * Updates heartbeat timestamp (node is alive).
     */
    private void updateHeartbeat() {
        lastHeartbeat.set(System.currentTimeMillis());
    }

    /**
     * Starts heartbeat monitoring.
     */
    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            updateHeartbeat();

            // Check if we should transition to DEAD
            if (!isAlive()) {
                log.error("💀 Node {} missed heartbeat - marking as DEAD", nodeId);
            }

        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Transitions node to a new state.
     */
    private synchronized void transitionTo(NodeState newState) {
        NodeState oldState = currentState;
        currentState = newState;

        log.info("🔄 Node {} state change: {} → {}", nodeId, oldState, newState);
    }

    /**
     * Gets current lifecycle information.
     */
    public synchronized NodeLifecycleInfo getLifecycleInfo() {
        return new NodeLifecycleInfo(
                nodeId,
                currentState,
                createdTime.get(),
                startedTime.get(),
                stoppedTime.get(),
                lastHeartbeat.get(),
                isAlive(),
                getUptime()
        );
    }

    /**
     * Calculates uptime in seconds.
     */
    private long getUptime() {
        if (startedTime.get() == 0) {
            return 0;
        }

        if (stoppedTime.get() > 0) {
            return (stoppedTime.get() - startedTime.get()) / 1000;
        }

        return (System.currentTimeMillis() - startedTime.get()) / 1000;
    }

    // Getters
    public NodeState getCurrentState() { return currentState; }
}

/**
 * Snapshot of node lifecycle information.
 */
class NodeLifecycleInfo {
    private final String nodeId;
    private final NodeLifecycleManager.NodeState state;
    private final long createdTime;
    private final long startedTime;
    private final long stoppedTime;
    private final long lastHeartbeat;
    private final boolean alive;
    private final long uptimeSeconds;

    public NodeLifecycleInfo(String nodeId, NodeLifecycleManager.NodeState state,
                             long createdTime, long startedTime, long stoppedTime,
                             long lastHeartbeat, boolean alive, long uptimeSeconds) {
        this.nodeId = nodeId;
        this.state = state;
        this.createdTime = createdTime;
        this.startedTime = startedTime;
        this.stoppedTime = stoppedTime;
        this.lastHeartbeat = lastHeartbeat;
        this.alive = alive;
        this.uptimeSeconds = uptimeSeconds;
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public NodeLifecycleManager.NodeState getState() { return state; }
    public long getCreatedTime() { return createdTime; }
    public long getStartedTime() { return startedTime; }
    public long getStoppedTime() { return stoppedTime; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public boolean isAlive() { return alive; }
    public long getUptimeSeconds() { return uptimeSeconds; }
}
