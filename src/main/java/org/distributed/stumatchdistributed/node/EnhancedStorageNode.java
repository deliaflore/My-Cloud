package org.distributed.stumatchdistributed.node;

import io.grpc.stub.StreamObserver;
import org.distributed.stumatchdistributed.virtualdisk.VirtualDisk;
import org.distributed.stumatchdistributed.network.NetworkInterfaceManager;
import org.distributed.stumatchdistributed.network.NetworkInterface;
import org.distributed.stumatchdistributed.process.ProcessManager;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Storage Node with:
 * - Virtual Disk (real file system)
 * - Auto IP assignment
 * - Lifecycle management (READY/RUNNING/WAITING/STOPPED/DEAD)
 * - Process management
 * - Sandboxed operation
 *
 * @author Your Name
 * @version 2.0
 */
public class EnhancedStorageNode {
    private static final Logger log = LoggerFactory.getLogger(EnhancedStorageNode.class);

    // Node identity
    private final String nodeId;
    private final int port;

    // Network interface (auto-assigned IP)
    private final NetworkInterface networkInterface;

    // Virtual disk
    private final VirtualDisk virtualDisk;

    // Lifecycle management
    private final NodeLifecycleManager lifecycleManager;

    // Process management
    private final ProcessManager processManager;

    // gRPC server
    private Server server;

    // Node specifications
    private final int storageGB;
    private final int ramGB;
    private final int cpuCores;
    private final long bandwidthBitsPerSecond;

    /**
     * Creates an enhanced storage node.
     */
    private EnhancedStorageNode(Builder builder) throws IOException {
        this.nodeId = builder.nodeId;
        this.port = builder.port;
        this.storageGB = builder.storageGB;
        this.ramGB = builder.ramGB;
        this.cpuCores = builder.cpuCores;
        this.bandwidthBitsPerSecond = builder.bandwidthMbps * 1_000_000L;

        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  ENHANCED STORAGE NODE INITIALIZATION                  ║");
        log.info("╠════════════════════════════════════════════════════════╣");
        log.info("║  Node ID: {}                                    ║", String.format("%-40s", nodeId));
        log.info("╚════════════════════════════════════════════════════════╝");

        // 1. Assign Network Interface (Auto IP)
        log.info("🌐 Step 1: Assigning network interface...");
        this.networkInterface = NetworkInterfaceManager.getInstance().assignInterface(nodeId);
        if (networkInterface == null) {
            throw new IOException("Failed to assign network interface");
        }
        log.info("   ✅ IP Address: {}", networkInterface.getIpAddress());
        log.info("   ✅ MAC Address: {}", networkInterface.getMacAddress());

        // 2. Create Virtual Disk
        log.info("💾 Step 2: Creating virtual disk...");
        Path diskDir = Paths.get(System.getProperty("user.home"), "distributed-storage", "disks");
        this.virtualDisk = new VirtualDisk(nodeId + "-disk", storageGB, diskDir);
        log.info("   ✅ Virtual Disk: {} GB", storageGB);

        // Format the disk
        if (!virtualDisk.isFormatted()) {
            log.info("   🔧 Formatting virtual disk...");
            virtualDisk.format();
        }

        // Mount the disk
        virtualDisk.mount();
        log.info("   ✅ Disk mounted");

        // 3. Initialize Lifecycle Manager
        log.info("🔧 Step 3: Initializing lifecycle manager...");
        this.lifecycleManager = new NodeLifecycleManager(nodeId);
        log.info("   ✅ Lifecycle manager ready");

        // 4. Initialize Process Manager
        log.info("⚙️  Step 4: Initializing process manager...");
        this.processManager = new ProcessManager(nodeId, cpuCores);
        log.info("   ✅ Process manager ready ({} cores)", cpuCores);

        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  ✅ NODE INITIALIZATION COMPLETE                       ║");
        log.info("╠════════════════════════════════════════════════════════╣");
        log.info("║  Node ID:     {}                            ║", String.format("%-38s", nodeId));
        log.info("║  IP Address:  {}                      ║", String.format("%-38s", networkInterface.getIpAddress()));
        log.info("║  Port:        {}                                ║", port);
        log.info("║  Storage:     {} GB (Virtual Disk)                 ║", storageGB);
        log.info("║  RAM:         {} GB                                  ║", ramGB);
        log.info("║  CPU Cores:   {}                                     ║", cpuCores);
        log.info("║  Status:      READY                                   ║");
        log.info("╚════════════════════════════════════════════════════════╝");
    }

    /**
     * Starts the node (activates lifecycle, starts gRPC server).
     */
    public void start() throws IOException, InterruptedException {
        log.info("🚀 Starting node: {}", nodeId);

        // Activate lifecycle
        if (!lifecycleManager.activate()) {
            throw new IOException("Failed to activate node lifecycle");
        }

        // Start process manager
        processManager.start();

        // Start gRPC server
        server = ServerBuilder.forPort(port)
                .addService(new EnhancedNodeServiceImpl(this))
                .build()
                .start();

        log.info("╔════════════════════════════════════════════════════════╗");
        log.info("║  ✅ NODE STARTED SUCCESSFULLY                          ║");
        log.info("╠════════════════════════════════════════════════════════╣");
        log.info("║  Node ID:     {}                            ║", String.format("%-38s", nodeId));
        log.info("║  IP:Port:     {}:{}                 ║",
                String.format("%-25s", networkInterface.getIpAddress()), port);
        log.info("║  State:       WAITING (Ready for requests)            ║");
        log.info("║  Disk:        MOUNTED                                 ║");
        log.info("╚════════════════════════════════════════════════════════╝");

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("⚠️  Shutting down node {}", nodeId);
            try {
                log.info("oohh no im shutting down node {}", nodeId);
                EnhancedStorageNode.this.stop();
            } catch (InterruptedException e) {
                log.error("Error during shutdown", e);
            }
        }));

        // Block until terminated
        server.awaitTermination();
    }

    /**
     * Stops the node gracefully.
     */
    public void stop() throws InterruptedException {
        log.info("🛑 Stopping node, im dying: {}", nodeId);

        // Stop lifecycle
        lifecycleManager.stop();

        // Stop process manager
        processManager.shutdown();

        // Unmount disk
        virtualDisk.unmount();

        // Stop gRPC server
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }

        // Release network interface
        NetworkInterfaceManager.getInstance().releaseInterface(nodeId);

        log.info("✅ Node stopped: {}", nodeId);
    }

    /**
     * Stores a chunk using the virtual disk.
     * Submits as a managed process.
     */
    public boolean storeChunk(String chunkId, byte[] data) {
        log.info("╔═══════════════════════════════════════════════════════╗");
        log.info("║  📥 INCOMING CHUNK STORAGE REQUEST                    ║");
        log.info("╠═══════════════════════════════════════════════════════╣");
        log.info("║  Node:      {}                               ║", String.format("%-40s", nodeId));
        log.info("║  Chunk ID:  {}                     ║", String.format("%-40s", chunkId));
        log.info("║  Size:      {}                              ║", formatBytes(data.length));
        log.info("╚═══════════════════════════════════════════════════════╝");

        // Submit as a process
        long pid = processManager.submitProcess(
                "StoreChunk-" + chunkId,
                () -> {
                    try {
                        // Simulate transfer time
                        long transferTimeMs = (data.length * 8 * 1000) / bandwidthBitsPerSecond;

                        log.info("⏳ Transferring... [          ]   0%");
                        Thread.sleep(transferTimeMs / 4);
                        log.info("⏳ Transferring... [██        ]  25%");
                        Thread.sleep(transferTimeMs / 4);
                        log.info("⏳ Transferring... [████      ]  50%");
                        Thread.sleep(transferTimeMs / 4);
                        log.info("⏳ Transferring... [███████   ]  75%");
                        Thread.sleep(transferTimeMs / 4);
                        log.info("⏳ Transferring... [██████████] 100%");

                        // Write to virtual disk
                        boolean success = virtualDisk.writeFile(chunkId, data);

                        if (success) {
                            log.info("╔═══════════════════════════════════════════════════════╗");
                            log.info("║  ✅ CHUNK STORED SUCCESSFULLY                         ║");
                            log.info("╠═══════════════════════════════════════════════════════╣");
                            log.info("║  Chunk ID:    {}                   ║", String.format("%-38s", chunkId));
                            log.info("║  Duration:    {} ms                         ║", transferTimeMs);
                            log.info("║  Disk Used:   {}/{} GB                  ║",
                                    virtualDisk.getUsedBytes() / (1024*1024*1024),
                                    virtualDisk.getTotalSizeBytes() / (1024*1024*1024));
                            log.info("║  Files:       {}                                  ║", virtualDisk.listFiles().size());
                            log.info("╚═══════════════════════════════════════════════════════╝");
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Transfer interrupted", e);
                    }
                },
                5  // Priority
        );

        log.info("📋 Process submitted (PID: {})", pid);

        return true;
    }

    /**
     * Retrieves a chunk from the virtual disk.
     */
    public byte[] retrieveChunk(String chunkId) {
        log.info("📤 Retrieving chunk: {}", chunkId);
        return virtualDisk.readFile(chunkId);
    }

    /**
     * Gets comprehensive node status.
     */
    public EnhancedNodeStatus getStatus() {
        return new EnhancedNodeStatus(
                nodeId,
                networkInterface.getIpAddress(),
                networkInterface.getMacAddress(),
                port,
                lifecycleManager.getLifecycleInfo(),
                virtualDisk.getStats(),
                processManager.listProcesses(),
                ramGB,
                cpuCores
        );
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public NetworkInterface getNetworkInterface() { return networkInterface; }
    public VirtualDisk getVirtualDisk() { return virtualDisk; }
    public NodeLifecycleManager getLifecycleManager() { return lifecycleManager; }
    public ProcessManager getProcessManager() { return processManager; }

    /**
     * Builder pattern.
     */
    public static class Builder {
        private String nodeId;
        private int port;
        private int storageGB = 100;
        private int ramGB = 8;
        private int cpuCores = 4;
        private int bandwidthMbps = 1000;

        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
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

        public EnhancedStorageNode build() throws IOException {
            if (nodeId == null || nodeId.isEmpty()) {
                throw new IllegalStateException("Node ID is required");
            }
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Invalid port: " + port);
            }

            return new EnhancedStorageNode(this);
        }
    }

    /**
     * Main method - runs node as separate process.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java EnhancedStorageNode <node-id> <port> [storage-gb] [ram-gb]");
            System.exit(1);
        }

        try {
            EnhancedStorageNode node = new EnhancedStorageNode.Builder()
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

// ============================================================================
// Enhanced Node Status (comprehensive info)
// ============================================================================

// ============================================================================
// Enhanced gRPC Service Implementation
// ============================================================================

class EnhancedNodeServiceImpl extends org.distributed.stumatchdistributed.grpc.NodeServiceGrpc.NodeServiceImplBase {
    private final EnhancedStorageNode node;

    EnhancedNodeServiceImpl(EnhancedStorageNode node) {
        this.node = node;
    }

    @Override
    public void storeChunk(org.distributed.stumatchdistributed.grpc.StoreChunkRequest request,
                           StreamObserver<org.distributed.stumatchdistributed.grpc.StoreChunkResponse> responseObserver) {
        String chunkId = request.getChunkId();
        byte[] data = request.getData().toByteArray();

        boolean success = node.storeChunk(chunkId, data);

        org.distributed.stumatchdistributed.grpc.StoreChunkResponse response = org.distributed.stumatchdistributed.grpc.StoreChunkResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "Stored on virtual disk" : "Storage failed")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void retrieveChunk(org.distributed.stumatchdistributed.grpc.RetrieveChunkRequest request,
                              StreamObserver<org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse> responseObserver) {
        String chunkId = request.getChunkId();
        byte[] data = node.retrieveChunk(chunkId);

        org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse.Builder responseBuilder = org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse.newBuilder();

        if (data != null) {
            responseBuilder.setData(com.google.protobuf.ByteString.copyFrom(data))
                    .setSuccess(true);
        } else {
            responseBuilder.setSuccess(false);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getStatus(org.distributed.stumatchdistributed.grpc.StatusRequest request,
                          StreamObserver<org.distributed.stumatchdistributed.grpc.StatusResponse> responseObserver) {
        EnhancedNodeStatus status = node.getStatus();

        // Convert to basic StatusResponse (maintain compatibility)
        org.distributed.stumatchdistributed.grpc.StatusResponse response = org.distributed.stumatchdistributed.grpc.StatusResponse.newBuilder()
                .setNodeId(status.getNodeId())
                .setUsedStorage(status.getDiskStats().getUsedBytes())
                .setTotalStorage(status.getDiskStats().getTotalBytes())
                .setNumChunks(status.getDiskStats().getFileCount())
                .setUtilizationPercent(status.getDiskStats().getUtilizationPercent())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
