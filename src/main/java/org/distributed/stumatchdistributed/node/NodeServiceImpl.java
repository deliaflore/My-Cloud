package org.distributed.stumatchdistributed.node;


import org.distributed.stumatchdistributed.model.NodeStatus;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC service implementation for node operations.
 *
 * Design Pattern: Adapter Pattern
 * - Adapts StorageNode interface to gRPC service interface
 * - Handles protocol buffer serialization/deserialization
 *
 * Separation of Concerns:
 * - This class only handles gRPC communication
 * - Business logic stays in StorageNode
 *
 * @author Your Name
 * @version 1.0
 */
public class NodeServiceImpl extends org.distributed.stumatchdistributed.grpc.NodeServiceGrpc.NodeServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(NodeServiceImpl.class);

    private final StorageNode node;

    /**
     * Constructor injection for better testability.
     *
     * @param node The storage node this service wraps
     */
    public NodeServiceImpl(StorageNode node) {
        this.node = node;
    }

    /**
     * Handles chunk storage requests via gRPC.
     *
     * Protocol:
     * 1. Client sends StoreChunkRequest (chunk_id + data)
     * 2. Node attempts to store
     * 3. Returns StoreChunkResponse (success/failure)
     *
     * @param request Contains chunk ID and data
     * @param responseObserver Callback for sending response
     */
    @Override
    public void storeChunk(org.distributed.stumatchdistributed.grpc.StoreChunkRequest request,
                           StreamObserver<org.distributed.stumatchdistributed.grpc.StoreChunkResponse> responseObserver) {
        String chunkId = request.getChunkId();
        byte[] data = request.getData().toByteArray();

        log.debug("gRPC request received: storeChunk({})", chunkId);

        try {
            // Delegate to business logic layer
            boolean success = node.storeChunk(chunkId, data);

            // Build response
            org.distributed.stumatchdistributed.grpc.StoreChunkResponse response = org.distributed.stumatchdistributed.grpc.StoreChunkResponse.newBuilder()
                    .setSuccess(success)
                    .setMessage(success ? "Chunk stored successfully" : "Insufficient storage space")
                    .build();

            // Send response to client
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.debug("gRPC response sent: success={}", success);

        } catch (Exception e) {
            log.error("Error storing chunk via gRPC", e);

            // Send error response
            org.distributed.stumatchdistributed.grpc.StoreChunkResponse errorResponse = org.distributed.stumatchdistributed.grpc.StoreChunkResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Internal error: " + e.getMessage())
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    /**
     * Handles chunk retrieval requests via gRPC.
     *
     * @param request Contains chunk ID to retrieve
     * @param responseObserver Callback for sending response with chunk data
     */
    @Override
    public void retrieveChunk(org.distributed.stumatchdistributed.grpc.RetrieveChunkRequest request,
                              StreamObserver<org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse> responseObserver) {
        String chunkId = request.getChunkId();

        log.debug("gRPC request received: retrieveChunk({})", chunkId);

        try {
            // Delegate to business logic
            byte[] data = node.retrieveChunk(chunkId);

            // Build response
            org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse.Builder responseBuilder = org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse.newBuilder();

            if (data != null) {
                responseBuilder
                        .setData(ByteString.copyFrom(data))
                        .setSuccess(true);
            } else {
                responseBuilder.setSuccess(false);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

            log.debug("gRPC response sent: found={}", data != null);

        } catch (Exception e) {
            log.error("Error retrieving chunk via gRPC", e);

            org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse errorResponse = org.distributed.stumatchdistributed.grpc.RetrieveChunkResponse.newBuilder()
                    .setSuccess(false)
                    .build();

            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    /**
     * Handles node status requests via gRPC.
     *
     * @param request Empty request (no parameters needed)
     * @param responseObserver Callback for sending status response
     */
    @Override
    public void getStatus(org.distributed.stumatchdistributed.grpc.StatusRequest request,
                          StreamObserver<org.distributed.stumatchdistributed.grpc.StatusResponse> responseObserver) {
        log.debug("gRPC request received: getStatus()");

        try {
            // Get status from node
            NodeStatus status = node.getStatus();

            // Convert to protocol buffer format
            org.distributed.stumatchdistributed.grpc.StatusResponse response = org.distributed.stumatchdistributed.grpc.StatusResponse.newBuilder()
                    .setNodeId(status.getNodeId())
                    .setUsedStorage(status.getUsedStorageBytes())
                    .setTotalStorage(status.getTotalStorageBytes())
                    .setNumChunks(status.getNumChunks())
                    .setUtilizationPercent(status.getUtilizationPercent())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.debug("gRPC response sent: status={}", status);

        } catch (Exception e) {
            log.error("Error getting node status via gRPC", e);
            responseObserver.onError(e);
        }
    }
}
