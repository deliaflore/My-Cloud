package org.distributed.stumatchdistributed.service;


import org.distributed.stumatchdistributed.model.FileChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for decomposing files into chunks.
 * Single Responsibility Principle: Only handles file decomposition.
 *
 * @author Your Name
 * @version 1.0
 */
@Service
public class FileDecompositionService {
    private static final Logger log = LoggerFactory.getLogger(FileDecompositionService.class);
    private static final int DEFAULT_CHUNK_SIZE_MB = 2;

    /**
     * Decomposes a file into chunks for distributed storage.
     *
     * @param filePath Path to the file to decompose
     * @param chunkSizeMB Size of each chunk in megabytes
     * @return List of file chunks
     * @throws IOException if file cannot be read
     */
    public List<FileChunk> decomposeFile(Path filePath, int chunkSizeMB) throws IOException {
        log.info("Decomposing file: {} with chunk size: {} MB", filePath, chunkSizeMB);

        byte[] fileData = Files.readAllBytes(filePath);
        int chunkSizeBytes = chunkSizeMB * 1024 * 1024;

        List<FileChunk> chunks = new ArrayList<>();
        String fileName = filePath.getFileName().toString();

        int offset = 0;
        int chunkIndex = 0;

        while (offset < fileData.length) {
            int currentChunkSize = Math.min(chunkSizeBytes, fileData.length - offset);
            byte[] chunkData = Arrays.copyOfRange(fileData, offset, offset + currentChunkSize);

            String chunkId = String.format("%s_chunk_%d", fileName, chunkIndex);
            FileChunk chunk = new FileChunk(chunkId, chunkData);
            chunks.add(chunk);

            log.debug("Created chunk: {} ({} bytes)", chunkId, currentChunkSize);

            offset += currentChunkSize;
            chunkIndex++;
        }

        log.info("File decomposed into {} chunks", chunks.size());
        return chunks;
    }

    /**
     * Decomposes file using default chunk size.
     */
    public List<FileChunk> decomposeFile(Path filePath) throws IOException {
        return decomposeFile(filePath, DEFAULT_CHUNK_SIZE_MB);
    }
}
