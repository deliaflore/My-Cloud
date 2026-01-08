package org.distributed.stumatchdistributed.storage.entity;

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
 * Represents the physical/virtual storage allocation assigned to a user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_storage")
public class UserStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(nullable = false, unique = true, length = 120)
    private String diskId;

    @Column(nullable = false, length = 512)
    private String diskPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Default
    private StorageState state = StorageState.PROVISIONING;

    @Column(nullable = false)
    private long quotaBytes;

    @Column(nullable = false)
    @Default
    private long usedBytes = 0L;

    private LocalDateTime mountedAt;

    private LocalDateTime lastSyncAt;

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

