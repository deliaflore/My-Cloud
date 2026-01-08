package org.distributed.stumatchdistributed.storage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata describing a file stored in the distributed system.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "otpEnabled", "storageQuotaBytes", "files", "storage"})
    private UserAccount owner;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, unique = true, length = 120)
    private String objectKey;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(length = 120)
    private String contentType;

    @Column(length = 64)
    private String checksum;

    @Column(length = 120)
    private String storageNodeHint;

    @Column(nullable = false, length = 512)
    private String storagePath;

    @Column(nullable = false)
    @Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

