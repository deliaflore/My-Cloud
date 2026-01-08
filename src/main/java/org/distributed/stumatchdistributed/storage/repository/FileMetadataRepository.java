package org.distributed.stumatchdistributed.storage.repository;

import org.distributed.stumatchdistributed.storage.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(UUID ownerId);

    Optional<FileMetadata> findByOwnerIdAndObjectKey(UUID ownerId, String objectKey);

    long countByOwnerId(UUID ownerId);

    @Query("""
            select coalesce(sum(f.sizeBytes), 0)
            from FileMetadata f
            where f.owner.id = :ownerId
              and f.deleted = false
            """)
    long sumActiveSizeBytesByOwner(@Param("ownerId") UUID ownerId);
}

