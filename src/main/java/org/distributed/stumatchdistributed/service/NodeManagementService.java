package org.distributed.stumatchdistributed.service;

import org.distributed.stumatchdistributed.network.NetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage node processes (start/stop nodes from web interface).
 * 
 * This service spawns new Java processes for each node using ProcessBuilder.
 */
@Service
public class NodeManagementService {
    private static final Logger log = LoggerFactory.getLogger(NodeManagementService.class);
    
    private final Map<String, Process> runningNodeProcesses = new ConcurrentHashMap<>();
    private final NetworkController networkController;
    
    public NodeManagementService(NetworkController networkController) {
        this.networkController = networkController;
    }
    
    /**
     * Starts a new node as a separate Java process.
     * 
     * @param nodeId Node identifier
     * @param port gRPC port
     * @param storageGB Storage size in GB
     * @param ramGB RAM in GB
     * @return true if started successfully
     */
    public boolean startNode(String nodeId, int port, int storageGB, int ramGB) {
        if (runningNodeProcesses.containsKey(nodeId)) {
            log.warn("Node {} is already running", nodeId);
            return false;
        }
        
        try {
            // Find the Java executable
            String javaHome = System.getProperty("java.home");
            String javaExec = javaHome + File.separator + "bin" + File.separator + "java";
            
            // Get the classpath (current JAR or classes directory)
            String classpath = System.getProperty("java.class.path");
            
            // Get the project root directory
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path targetClasses = projectRoot.resolve("target").resolve("classes");
            
            // Build classpath - prefer target/classes if it exists
            String fullClasspath;
            if (targetClasses.toFile().exists()) {
                fullClasspath = targetClasses.toString() + File.pathSeparator + classpath;
            } else {
                fullClasspath = classpath;
            }
            
            // Build the command
            ProcessBuilder processBuilder = new ProcessBuilder(
                javaExec,
                "-cp", fullClasspath,
                "org.distributed.stumatchdistributed.node.EnhancedStorageNode",
                nodeId,
                String.valueOf(port),
                String.valueOf(storageGB),
                String.valueOf(ramGB)
            );
            
            // Set working directory
            processBuilder.directory(projectRoot.toFile());
            
            // Redirect output to log files
            Path logDir = projectRoot.resolve("logs");
            logDir.toFile().mkdirs();
            
            File logFile = logDir.resolve(nodeId + ".log").toFile();
            File errorFile = logDir.resolve(nodeId + ".err").toFile();
            
            processBuilder.redirectOutput(logFile);
            processBuilder.redirectError(errorFile);
            
            // Start the process
            Process process = processBuilder.start();
            runningNodeProcesses.put(nodeId, process);
            
            log.info("✅ Started node process: {} (PID: {})", nodeId, process.pid());
            log.info("   Port: {}, Storage: {}GB, RAM: {}GB", port, storageGB, ramGB);
            log.info("   Logs: {}", logFile.getAbsolutePath());
            
            // Wait a bit for node to start gRPC server, then auto-register
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Wait 3 seconds for node to start
                    networkController.registerNode(nodeId, "localhost", port);
                    log.info("✅ Auto-registered node {} with NetworkController", nodeId);
                } catch (Exception e) {
                    log.warn("Failed to auto-register node {} (you can register manually via API): {}", nodeId, e.getMessage());
                }
            }, "node-register-" + nodeId).start();
            
            // Monitor process in background
            monitorProcess(nodeId, process);
            
            return true;
            
        } catch (IOException e) {
            log.error("❌ Failed to start node: {}", nodeId, e);
            return false;
        }
    }
    
    /**
     * Stops a running node process.
     */
    public boolean stopNode(String nodeId) {
        // Note: We don't unregister from NetworkController here because
        // the node process will handle its own cleanup when it shuts down
        
        Process process = runningNodeProcesses.remove(nodeId);
        if (process == null) {
            log.warn("Node {} is not running", nodeId);
            return false;
        }
        
        try {
            if (process.isAlive()) {
                process.destroy();
                
                // Wait for graceful shutdown
                boolean terminated = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (!terminated) {
                    log.warn("Force killing node: {}", nodeId);
                    process.destroyForcibly();
                }
            }
            
            log.info("✅ Stopped node: {}", nodeId);
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupted while stopping node: {}", nodeId, e);
            return false;
        }
    }
    
    /**
     * Checks if a node is running.
     */
    public boolean isNodeRunning(String nodeId) {
        Process process = runningNodeProcesses.get(nodeId);
        return process != null && process.isAlive();
    }
    
    /**
     * Gets all running node IDs.
     */
    public java.util.Set<String> getRunningNodes() {
        // Clean up dead processes
        runningNodeProcesses.entrySet().removeIf(entry -> !entry.getValue().isAlive());
        return runningNodeProcesses.keySet();
    }
    
    /**
     * Monitors a process and removes it from the map when it dies.
     */
    private void monitorProcess(String nodeId, Process process) {
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                runningNodeProcesses.remove(nodeId);
                log.info("Node {} process exited with code: {}", nodeId, exitCode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Process monitor interrupted for node: {}", nodeId);
            }
        }, "node-monitor-" + nodeId).start();
    }
}

