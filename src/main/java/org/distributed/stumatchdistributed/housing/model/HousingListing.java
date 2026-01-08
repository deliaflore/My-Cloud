package org.distributed.stumatchdistributed.housing.model;

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
@Table(name = "housing_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousingListing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String location;

    private Double latitude;
    
    private Double longitude;

    @Column(nullable = false)
    private Integer bedrooms;

    private Integer bathrooms;

    @Column(nullable = false)
    private Boolean verified = false;

    private String imageUrl;

    private Double rating;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "landlord_id", nullable = false)
    @JsonIgnoreProperties({"password", "otpEnabled", "storageQuotaBytes"})
    private UserAccount landlord;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.verified == null) {
            this.verified = false;
        }
        if (this.rating == null) {
            this.rating = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
