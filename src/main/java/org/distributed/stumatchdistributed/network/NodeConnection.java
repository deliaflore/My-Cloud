package org.distributed.stumatchdistributed.network;


import io.grpc.ManagedChannel;
import org.distributed.stumatchdistributed.grpc.NodeServiceGrpc;

/**
 * Represents a connection to a remote storage node.
 *
 * Design Pattern: Facade Pattern
 * - Provides simple interface to complex gRPC channel management
 * - Encapsulates connection details
 *
 * @author Your Name
 * @version 1.0
 */
public class NodeConnection {
    private final String nodeId;
    private final String host;
    private final int port;
    private final ManagedChannel channel;
    private final NodeServiceGrpc.NodeServiceBlockingStub stub;

    /**
     * Creates a connection to a remote node.
     *
     * @param nodeId Unique identifier for the node
     * @param host Node hostname/IP
     * @param port Node gRPC port
     * @param channel gRPC managed channel
     * @param stub Blocking stub for synchronous calls
     */
    public NodeConnection(String nodeId, String host, int port,
                          ManagedChannel channel,
                          NodeServiceGrpc.NodeServiceBlockingStub stub) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.channel = channel;
        this.stub = stub;
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public ManagedChannel getChannel() { return channel; }
    public org.distributed.stumatchdistributed.grpc.NodeServiceGrpc.NodeServiceBlockingStub getStub() { return stub; }

    /**
     * Returns connection details as string.
     */
    public String getAddress() {
        return host + ":" + port;
    }

    @Override
    public String toString() {
        return String.format("NodeConnection{id='%s', address='%s'}", nodeId, getAddress());
    }
}
