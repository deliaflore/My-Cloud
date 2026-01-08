package org.distributed.stumatchdistributed.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkDistributionDTO {
    private String fileId;
    private String fileName;
    private int totalChunks;
    private List<ChunkInfo> distribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkInfo {
        private int chunkIndex;
        private String nodeId;
        private long sizeBytes;
    }
}
