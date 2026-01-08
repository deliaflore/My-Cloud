package org.distributed.stumatchdistributed.storage.service;

import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.config.StorageProperties;
import org.distributed.stumatchdistributed.model.ChunkDistribution;
import org.distributed.stumatchdistributed.network.NetworkController;
import org.distributed.stumatchdistributed.storage.entity.FileMetadata;
import org.distributed.stumatchdistributed.storage.entity.UserStorage;
import org.distributed.stumatchdistributed.storage.repository.FileMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final int DEFAULT_CHUNK_SIZE_MB = 2; // 2MB chunks for distribution

    private final FileMetadataRepository fileMetadataRepository;
    private final UserStorageService userStorageService;
    private final StorageProperties storageProperties;
    private final NetworkController networkController;

    public FileService(FileMetadataRepository fileMetadataRepository,
                       UserStorageService userStorageService,
                       StorageProperties storageProperties,
                       NetworkController networkController) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.userStorageService = userStorageService;
        this.storageProperties = storageProperties;
        this.networkController = networkController;
    }

    public List<FileMetadata> listFiles(UserAccount user) {
        return fileMetadataRepository.findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public FileMetadata upload(UserAccount user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        long size = file.getSize();
        userStorageService.assertHasCapacity(user, size);

        UserStorage storage = userStorageService.getStorage(user);
        Path fileDir = resolveUserFileDirectory(storage);

        try {
            Files.createDirectories(fileDir);
            String objectKey = UUID.randomUUID().toString();
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin";
            
            // Step 1: Save to user's virtual disk (for quota tracking and local backup)
            Path targetPath = fileDir.resolve(objectKey);
            Files.copy(file.getInputStream(), targetPath);
            
            // Step 2: Distribute file chunks across distributed storage nodes via gRPC
            String distributionInfo = null;
            try {
                if (networkController.getRegisteredNodes().isEmpty()) {
                    log.warn("No distributed nodes available. File stored locally only.");
                } else {
                    log.info("Distributing file '{}' across {} nodes", fileName, networkController.getRegisteredNodes().size());
                    ChunkDistribution distribution = networkController.distributeFile(targetPath, DEFAULT_CHUNK_SIZE_MB);
                    distributionInfo = formatDistributionInfo(distribution);
                    log.info("✅ File distributed: {} chunks across {} nodes", 
                            distribution.getTotalChunks(), distribution.getDistribution().size());
                }
            } catch (Exception e) {
                log.error("Failed to distribute file across nodes (stored locally only)", e);
                // Continue - file is still saved locally
            }

            FileMetadata metadata = FileMetadata.builder()
                    .owner(user)
                    .fileName(fileName)
                    .objectKey(objectKey)
                    .sizeBytes(size)
                    .contentType(file.getContentType())
                    .storagePath(targetPath.toString())
                    .storageNodeHint(distributionInfo) // Store distribution info
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            FileMetadata saved = fileMetadataRepository.save(metadata);
            userStorageService.incrementUsage(user, size);
            return saved;

        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }
    
    private String formatDistributionInfo(ChunkDistribution distribution) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chunks: ").append(distribution.getTotalChunks()).append(" | ");
        distribution.getDistribution().forEach((nodeId, chunks) -> {
            sb.append(nodeId).append(":").append(chunks.size()).append(" ");
        });
        return sb.toString().trim();
    }

    public ResponseEntity<byte[]> download(UserAccount user, UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .filter(file -> file.getOwner().getId().equals(user.getId()) && !file.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        try {
            Path path = Path.of(metadata.getStoragePath());
            byte[] bytes = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(
                            metadata.getContentType() != null ? metadata.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .body(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file", e);
        }
    }

    @Transactional
    public void delete(UserAccount user, UUID fileId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .filter(file -> file.getOwner().getId().equals(user.getId()) && !file.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        metadata.setDeleted(true);
        metadata.setDeletedAt(LocalDateTime.now());
        fileMetadataRepository.save(metadata);

        try {
            Files.deleteIfExists(Path.of(metadata.getStoragePath()));
        } catch (IOException ignored) {}

        userStorageService.decrementUsage(user, metadata.getSizeBytes());
    }

    private Path resolveUserFileDirectory(UserStorage storage) {
        Path usersDir = storageProperties.userDisksPath();
        return usersDir.resolve(storage.getDiskId() + "_files");
    }
}

