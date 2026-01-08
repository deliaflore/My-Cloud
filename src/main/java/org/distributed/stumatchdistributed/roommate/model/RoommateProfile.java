package org.distributed.stumatchdistributed.roommate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "roommate_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoommateProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"password", "otpEnabled", "storageQuotaBytes"})
    private UserAccount user;

    @Column(precision = 10, scale = 2)
    private BigDecimal minBudget;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxBudget;

    @Column(length = 50)
    private String studyHabits; // "early_bird", "night_owl", "flexible"

    @Column(length = 50)
    private String cleanliness; // "very_organized", "moderate", "relaxed"

    @Column(length = 50)
    private String lifestyle; // "quiet", "social", "party"

    private Boolean smoker;

    private Boolean petFriendly;

    @Column(length = 1000)
    private String bio;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.smoker == null) {
            this.smoker = false;
        }
        if (this.petFriendly == null) {
            this.petFriendly = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
