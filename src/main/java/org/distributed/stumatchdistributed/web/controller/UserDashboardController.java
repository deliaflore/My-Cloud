package org.distributed.stumatchdistributed.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.service.UserContextService;
import org.distributed.stumatchdistributed.storage.dto.ChunkDistributionDTO;
import org.distributed.stumatchdistributed.storage.dto.FileDTO;
import org.distributed.stumatchdistributed.storage.entity.FileMetadata;
import org.distributed.stumatchdistributed.storage.entity.UserStorage;
import org.distributed.stumatchdistributed.storage.repository.FileMetadataRepository;
import org.distributed.stumatchdistributed.storage.service.UserStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class UserDashboardController {

    private final UserContextService userContextService;
    private final UserStorageService userStorageService;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * GET /api/user/dashboard
     * Returns user's storage info, quota, usage, file count
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserDashboard(Authentication authentication) {
        try {
            log.info("API request: GET /api/user/dashboard");
            
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            UserStorage storage = userStorageService.getStorage(user);
            List<FileMetadata> files = fileMetadataRepository.findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

            Map<String, Object> dashboard = new HashMap<>();
            
            // User info
            dashboard.put("userName", user.getFullName() != null ? user.getFullName() : user.getEmail());
            dashboard.put("email", user.getEmail());
            
            // Storage info
            dashboard.put("quotaBytes", storage.getQuotaBytes());
            dashboard.put("usedBytes", storage.getUsedBytes());
            dashboard.put("availableBytes", storage.getQuotaBytes() - storage.getUsedBytes());
            dashboard.put("usagePercentage", (double) storage.getUsedBytes() / storage.getQuotaBytes() * 100);
            
            // File info
            dashboard.put("totalFiles", files.size());
            dashboard.put("diskId", storage.getDiskId());
            dashboard.put("storageState", storage.getState().toString());
            
            // Format sizes
            dashboard.put("quotaGB", formatBytes(storage.getQuotaBytes()));
            dashboard.put("usedGB", formatBytes(storage.getUsedBytes()));
            dashboard.put("availableGB", formatBytes(storage.getQuotaBytes() - storage.getUsedBytes()));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Failed to get user dashboard", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/user/dashboard/files
     * Returns list of user's files with details
     */
    @GetMapping("/files")
    public ResponseEntity<?> getUserFiles(Authentication authentication) {
        try {
            log.info("API request: GET /api/user/dashboard/files");
            
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            List<FileMetadata> files = fileMetadataRepository.findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

            // Convert to DTO to avoid circular reference issues
            List<FileDTO> fileDTOs = files.stream()
                    .map(f -> FileDTO.builder()
                            .id(f.getId())
                            .fileName(f.getFileName())
                            .objectKey(f.getObjectKey())
                            .sizeBytes(f.getSizeBytes())
                            .contentType(f.getContentType())
                            .createdAt(f.getCreatedAt())
                            .updatedAt(f.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(fileDTOs);
        } catch (Exception e) {
            log.error("Failed to get user files", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/user/dashboard/files/{fileId}/distribution
     * Returns chunk distribution info for a specific file
     */
    @GetMapping("/files/{fileId}/distribution")
    public ResponseEntity<?> getFileDistribution(
            @PathVariable UUID fileId,
            Authentication authentication) {
        try {
            log.info("API request: GET /api/user/dashboard/files/{}/distribution", fileId);
            
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            Optional<FileMetadata> fileOpt = fileMetadataRepository.findById(fileId);
            
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            FileMetadata file = fileOpt.get();
            
            // Verify user owns this file
            if (!file.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }
            
            // Parse distribution info from storageNodeHint
            String distributionInfo = file.getStorageNodeHint();
            ChunkDistributionDTO distribution = parseDistributionInfo(
                fileId.toString(), 
                file.getFileName(), 
                distributionInfo
            );
            
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            log.error("Failed to get file distribution", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private ChunkDistributionDTO parseDistributionInfo(String fileId, String fileName, String distributionInfo) {
        List<ChunkDistributionDTO.ChunkInfo> chunks = new ArrayList<>();
        
        if (distributionInfo != null && !distributionInfo.isEmpty()) {
            // Parse format: "node1(chunks:1,2,3), node2(chunks:4,5,6)"
            String[] nodeParts = distributionInfo.split(",\\s*(?=[^)]*\\()");
            int chunkIndex = 0;
            
            for (String nodePart : nodeParts) {
                if (nodePart.contains("(chunks:")) {
                    String nodeId = nodePart.substring(0, nodePart.indexOf("("));
                    String chunksStr = nodePart.substring(nodePart.indexOf(":") + 1, nodePart.indexOf(")"));
                    String[] chunkIds = chunksStr.split(",");
                    
                    for (String chunkId : chunkIds) {
                        chunks.add(ChunkDistributionDTO.ChunkInfo.builder()
                                .chunkIndex(chunkIndex++)
                                .nodeId(nodeId)
                                .sizeBytes(1024 * 1024) // 1MB default chunk size
                                .build());
                    }
                }
            }
        }
        
        // If no distribution info, assume single chunk on local
        if (chunks.isEmpty()) {
            chunks.add(ChunkDistributionDTO.ChunkInfo.builder()
                    .chunkIndex(0)
                    .nodeId("local")
                    .sizeBytes(1024 * 1024)
                    .build());
        }
        
        return ChunkDistributionDTO.builder()
                .fileId(fileId)
                .fileName(fileName)
                .totalChunks(chunks.size())
                .distribution(chunks)
                .build();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
