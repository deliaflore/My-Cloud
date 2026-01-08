package org.distributed.stumatchdistributed.storage.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for FileMetadata to avoid circular references in JSON serialization.
 */
public record FileMetadataDTO(
        UUID id,
        String fileName,
        String objectKey,
        long sizeBytes,
        String contentType,
        String storagePath,
        String storageNodeHint,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FileMetadataDTO from(org.distributed.stumatchdistributed.storage.entity.FileMetadata metadata) {
        return new FileMetadataDTO(
                metadata.getId(),
                metadata.getFileName(),
                metadata.getObjectKey(),
                metadata.getSizeBytes(),
                metadata.getContentType(),
                metadata.getStoragePath(),
                metadata.getStorageNodeHint(),
                metadata.isDeleted(),
                metadata.getCreatedAt(),
                metadata.getUpdatedAt()
        );
    }
}

