package org.distributed.stumatchdistributed.web.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * DTO for file distribution results.
 * Contains information about how chunks were distributed.
 *
 * @author Your Name
 * @version 1.0
 */
@Setter
@Getter
public class DistributionResultDTO {
    // Getters and Setters
    private String fileName;
    private int totalChunks;
    private long totalSizeBytes;
    private long distributionTimeMs;
    private Map<String, List<String>> nodeToChunks; // nodeId -> list of chunkIds

    // Constructors
    public DistributionResultDTO() {
    }

    public DistributionResultDTO(String fileName, int totalChunks, long totalSizeBytes,
                                 long distributionTimeMs, Map<String, List<String>> nodeToChunks) {
        this.fileName = fileName;
        this.totalChunks = totalChunks;
        this.totalSizeBytes = totalSizeBytes;
        this.distributionTimeMs = distributionTimeMs;
        this.nodeToChunks = nodeToChunks;
    }

}
