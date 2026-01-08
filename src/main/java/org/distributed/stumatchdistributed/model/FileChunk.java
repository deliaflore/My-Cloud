package org.distributed.stumatchdistributed.model;


import java.util.Objects;

/**
 * Represents a chunk of a file in the distributed storage system.
 * Files are decomposed into chunks for distributed storage.
 * Immutable to prevent accidental modification.
 *
 * @author Your Name
 * @version 1.0
 */
public class FileChunk {
    private final String chunkId;
    private final byte[] data;
    private final long sizeBytes;

    /**
     * Creates a file chunk.
     *
     * @param chunkId Unique identifier for this chunk
     * @param data The actual chunk data
     * @throws IllegalArgumentException if chunkId is null or data is null
     */
    public FileChunk(String chunkId, byte[] data) {
        Objects.requireNonNull(chunkId, "Chunk ID cannot be null");
        Objects.requireNonNull(data, "Chunk data cannot be null");

        this.chunkId = chunkId;
        this.data = data.clone(); // Defensive copy
        this.sizeBytes = data.length;
    }

    public String getChunkId() { return chunkId; }
    public long getSizeBytes() { return sizeBytes; }

    /**
     * Returns a defensive copy of the data to maintain immutability.
     */
    public byte[] getData() {
        return data.clone();
    }

    @Override
    public String toString() {
        return String.format("FileChunk{id='%s', size=%d bytes}", chunkId, sizeBytes);
    }
}

