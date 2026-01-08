package org.distributed.stumatchdistributed.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private UUID id;
    private String fileName;
    private String objectKey;
    private long sizeBytes;
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
