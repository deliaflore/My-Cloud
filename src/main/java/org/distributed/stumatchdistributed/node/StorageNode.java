package org.distributed.stumatchdistributed.node;

import lombok.Getter;
import org.distributed.stumatchdistributed.model.NodeStatus;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.distributed.stumatchdistributed.service.StorageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Storage Node with REAL FILE STORAGE.
 
 * KEY CHANGES:
 * - Each node has its own storage directory on disk
 * - Chunks stored as actual files (not just in memory)
 * - Can see and verify files in file explorer
 * - Storage persists across restarts
 *
 * @author Your Name
 * @version 2.0 (Real Storage)
 */
public class StorageNode {
    private static final Logger log = LoggerFactory.getLogger(StorageNode.class);

    // Node configuration
    @Getter
    private final String nodeId;
    private final String ipAddress;
    @Getter
    private final int port;
    private final long totalStorageBytes;
    private final long totalRamBytes;
    private final int cpuCores;
    private final long bandwidthBitsPerSecond;

    // REAL STORAGE: Path to this node's storage directory
    @Getter
    private final Path storageDirectory;

    // Track used storage (by reading actual files)
    private long usedStorageBytes;

    // gRPC server
    private Server server;

    /**
     * Creates a storage node with REAL file storage.
     */
    private StorageNode(Builder builder) {
        this.nodeId = builder.nodeId;
        this.ipAddress = builder.ipAddress;
        this.port = builder.port;
        this.totalStorageBytes = builder.storageGB * 1024L * 1024 * 1024;
        this.totalRamBytes = builder.ramGB * 1024L * 1024 * 1024;
        this.cpuCores = builder.cpuCores;
        this.bandwidthBitsPerSecond = builder.bandwidthMbps * 1_000_000L;

        // REAL STORAGE: Create directory for this node
        this.storageDirectory = createStorageDirectory();

        // Calculate used storage from existing files
        this.usedStorageBytes = calculateUsedStorage();

        logNodeCreation();
    }

    /**
     * Creates a dedicated storage directory for this node.

     * Directory structure:
     * C:/distributed-storage/
     *   ├── node1/
     *   │   ├── chunk_001.dat
     *   │   ├── chunk_002.dat
     *   ├── node2/
     *   │   ├── chunk_001.dat
     *   └── node3/
     *       ├── chunk_001.dat
     */
    private Path createStorageDirectory() {
        try {
            // Base directory on your computer
            Path baseDir = Paths.get(System.getProperty("user.home"), "distributed-storage");

            // Node-specific directory
            Path nodeDir = baseDir.resolve(nodeId);

            // Create directory if it doesn't exist
            if (!Files.exists(nodeDir)) {
                Files.createDirectories(nodeDir);
                log.info("📁 Created storage directory: {}", nodeDir.toAbsolutePath());
            } else {
                log.info("📁 Using existing storage directory: {}", nodeDir.toAbsolutePath());
            }

            return nodeDir;

        } catch (IOException e) {
            log.error("Failed to create storage directory", e);
            throw new RuntimeException("Cannot create storage directory", e);
        }
    }

    /**
     * Calculates used storage by scanning actual files.
     * This is how you PROVE storage is real!
     */
    private long calculateUsedStorage() {
        try {
            long totalSize = 0;

            File dir = storageDirectory.toFile();
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        totalSize += file.length();
                    }
                }
            }

            log.info("📊 Calculated used storage: {} bytes from {} files",
                    totalSize, files != null ? files.length : 0);

            return totalSize;

        } catch (Exception e) {
            log.error("Failed to calculate storage", e);
            return 0;
        }
    }

    /**
     * Starts the gRPC server.
     */
    public void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .addService(new NodeServiceImpl(this))
                .build()
                .start();

        log.info("✅ Node {} started on port {}", nodeId, port);
        log.info("🌐 Ready to accept storage requests");
        log.info("💾 Storage location: {}", storageDirectory.toAbsolutePath());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("⚠️ Shutting down node {}", nodeId);
            try {
                StorageNode.this.stop();
            } catch (InterruptedException e) {
                log.error("Error during shutdown", e);
            }
        }));

        server.awaitTermination();
    }

    /**
     * Stops the gRPC server gracefully.
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            log.info("Node {} stopped", nodeId);
        }
    }

    /**
     * Stores a chunk as an ACTUAL FILE on disk.

     * CRITICAL: This is what makes storage REAL!
     *
     * @param chunkId Unique identifier
     * @param data Chunk data
     * @return true if stored successfully
     */
    public synchronized boolean storeChunk(String chunkId, byte[] data) {
        long chunkSize = data.length;

        // Check capacity
        if (usedStorageBytes + chunkSize > totalStorageBytes) {
            log.warn("❌ Insufficient storage for chunk {} (need {} bytes, available {} bytes)",
                    chunkId, chunkSize, totalStorageBytes - usedStorageBytes);
            return false;
        }

        // ENHANCED: Show start time
        String startTime = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        );

        log.info("╔═══════════════════════════════════════════════════════╗");
        log.info("║  📥 INCOMING CHUNK TRANSFER                           ║");
        log.info("╠═══════════════════════════════════════════════════════╣");
        log.info("║  Chunk ID:    {}                    ║", String.format("%-35s", chunkId));
        log.info("║  Size:        {}                               ║", String.format("%-35s", formatBytes(chunkSize)));
        log.info("║  Start Time:  {}                          ║", startTime);
        log.info("╚═══════════════════════════════════════════════════════╝");

        // Simulate transfer with progress
        long transferTimeMs = (chunkSize * 8 * 1000) / bandwidthBitsPerSecond;

        try {
            log.info("⏳ Transferring... [          ]   0%");
            Thread.sleep(transferTimeMs / 4);
            log.info("⏳ Transferring... [██        ]  25%");
            Thread.sleep(transferTimeMs / 4);
            log.info("⏳ Transferring... [████      ]  50%");
            Thread.sleep(transferTimeMs / 4);
            log.info("⏳ Transferring... [███████   ]  75%");
            Thread.sleep(transferTimeMs / 4);
            log.info("⏳ Transferring... [██████████] 100%");

            // ═══════════════════════════════════════════════════════
            // CRITICAL PART: WRITE TO ACTUAL FILE ON DISK
            // ═══════════════════════════════════════════════════════

            Path chunkFile = storageDirectory.resolve(chunkId + ".dat");
            Files.write(chunkFile, data);

            log.info("💾 Chunk written to disk: {}", chunkFile.toAbsolutePath());

            // ═══════════════════════════════════════════════════════

            // Update used storage
            usedStorageBytes += chunkSize;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Transfer interrupted", e);
            return false;
        } catch (IOException e) {
            log.error("Failed to write chunk to disk", e);
            return false;
        }

        // Show end time
        String endTime = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        );

        double utilizationPercent = (usedStorageBytes * 100.0) / totalStorageBytes;
        int numFiles = Objects.requireNonNull(storageDirectory.toFile().listFiles()).length;

        log.info("╔═══════════════════════════════════════════════════════╗");
        log.info("║  ✅ TRANSFER COMPLETE                                 ║");
        log.info("╠═══════════════════════════════════════════════════════╣");
        log.info("║  End Time:        {}                      ║", endTime);
        log.info("║  Duration:        {} ms                        ║", String.format("%-26s", transferTimeMs));
        log.info("║  Total Stored:    {}                         ║", String.format("%-26s", formatBytes(usedStorageBytes)));
        log.info("║  Utilization:     {}%%                         ║", String.format("%.2f", utilizationPercent));
        log.info("║  Files on Disk:   {}                              ║", numFiles);
        log.info("║  Location:        {}   ║", storageDirectory.toAbsolutePath());
        log.info("╚═══════════════════════════════════════════════════════╝");
        log.info("");

        return true;
    }

    /**
     * Retrieves a chunk from ACTUAL FILE on disk.
     *
     * @param chunkId Chunk identifier
     * @return Chunk data or null if not found
     */
    public synchronized byte[] retrieveChunk(String chunkId) {
        try {
            Path chunkFile = storageDirectory.resolve(chunkId + ".dat");

            if (!Files.exists(chunkFile)) {
                log.warn("❌ Chunk file not found: {}", chunkFile);
                return null;
            }

            // Read from actual file
            byte[] data = Files.readAllBytes(chunkFile);

            log.info("📤 Retrieved chunk from disk: {} ({} bytes)",
                    chunkId, data.length);

            return data;

        } catch (IOException e) {
            log.error("Failed to read chunk from disk", e);
            return null;
        }
    }

    /**
     * Gets current node status by scanning actual files.
     */
    public synchronized NodeStatus getStatus() {
        // Recalculate from actual files
        usedStorageBytes = calculateUsedStorage();

        double utilization = (usedStorageBytes * 100.0) / totalStorageBytes;
        int numChunks = Objects.requireNonNull(storageDirectory.toFile().listFiles()).length;

        return new NodeStatus(
                nodeId,
                usedStorageBytes,
                totalStorageBytes,
                numChunks,
                utilization
        );
    }

    /**
     * Lists all chunks stored on this node.
     * Shows ACTUAL files on disk!
     */
    public synchronized String[] listStoredChunks() {
        File dir = storageDirectory.toFile();
        return dir.list((dir1, name) -> name.endsWith(".dat"));
    }

    /**
     * Deletes a chunk from disk.
     * Demonstrates actual file management.
     */
    public synchronized boolean deleteChunk(String chunkId) {
        try {
            Path chunkFile = storageDirectory.resolve(chunkId + ".dat");

            if (Files.exists(chunkFile)) {
                long fileSize = Files.size(chunkFile);
                Files.delete(chunkFile);

                usedStorageBytes -= fileSize;

                log.info("🗑️  Deleted chunk from disk: {}", chunkId);
                return true;
            }

            return false;

        } catch (IOException e) {
            log.error("Failed to delete chunk", e);
            return false;
        }
    }

    private String formatBytes(long bytes) {
        return StorageMetricsService.formatBytes(bytes);
    }

    private void logNodeCreation() {
        log.info("╔════════════════════════════════════════════╗");
        log.info("║  Node Created: {}                    ║", String.format("%-20s", nodeId));
        log.info("╠════════════════════════════════════════════╣");
        log.info("║  IP Address:  {}:{}         ║", ipAddress, port);
        log.info("║  Storage:     {} GB                    ║", totalStorageBytes / (1024*1024*1024));
        log.info("║  RAM:         {} GB                     ║", totalRamBytes / (1024*1024*1024));
        log.info("║  CPU Cores:   {}                        ║", cpuCores);
        log.info("║  Bandwidth:   {} Mbps                ║", bandwidthBitsPerSecond / 1_000_000);
        log.info("║  Storage Dir: {}  ║", storageDirectory.toAbsolutePath());
        log.info("╚════════════════════════════════════════════╝");
    }

    /**
     * Builder Pattern for clean construction.
     */
    public static class Builder {
        private String nodeId;
        private String ipAddress = "127.0.0.1";
        private int port;
        private int storageGB = 100;
        private int ramGB = 8;
        private int cpuCores = 4;
        private int bandwidthMbps = 1000;

        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder storageGB(int storageGB) {
            this.storageGB = storageGB;
            return this;
        }

        public Builder ramGB(int ramGB) {
            this.ramGB = ramGB;
            return this;
        }

        public Builder cpuCores(int cpuCores) {
            this.cpuCores = cpuCores;
            return this;
        }

        public Builder bandwidthMbps(int bandwidthMbps) {
            this.bandwidthMbps = bandwidthMbps;
            return this;
        }

        public StorageNode build() {
            if (nodeId == null || nodeId.isEmpty()) {
                throw new IllegalStateException("Node ID is required");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Invalid port number: " + port);
            }

            return new StorageNode(this);
        }
    }

    /**
     * Main method to run node as separate process.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java StorageNode <node-id> <port> [storage-gb] [ram-gb]");
            System.exit(1);
        }

        try {
            StorageNode node = new StorageNode.Builder()
                    .nodeId(args[0])
                    .port(Integer.parseInt(args[1]))
                    .storageGB(args.length > 2 ? Integer.parseInt(args[2]) : 100)
                    .ramGB(args.length > 3 ? Integer.parseInt(args[3]) : 8)
                    .build();

            node.start();

        } catch (Exception e) {
            log.error("Failed to start node", e);
            System.exit(1);
        }
    }
}