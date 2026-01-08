package org.distributed.stumatchdistributed.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.distributed.stumatchdistributed.storage.entity.UserStorage;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an end user of the cloud storage platform.
 * Each user owns a dedicated virtual disk and quota allocation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, length = 120)
    private String passwordHash;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false)
    @Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Default
    private long storageQuotaBytes = 1_073_741_824L; // 1 GB default quota

    @Column(nullable = false)
    @Default
    private long usedStorageBytes = 0L;

    @Column(length = 120)
    private String lastKnownIp;

    private LocalDateTime lastLoginAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private UserStorage storage;

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

