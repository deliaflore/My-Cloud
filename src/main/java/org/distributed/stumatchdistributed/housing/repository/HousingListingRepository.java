package org.distributed.stumatchdistributed.housing.repository;

import org.distributed.stumatchdistributed.housing.model.HousingListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface HousingListingRepository extends JpaRepository<HousingListing, Long> {
    
    List<HousingListing> findByVerifiedTrue();
    
    List<HousingListing> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    List<HousingListing> findByBedrooms(Integer bedrooms);
    
    List<HousingListing> findByLocationContainingIgnoreCase(String location);
    
    List<HousingListing> findByLandlordId(UUID landlordId);
    
    @Query("SELECT h FROM HousingListing h WHERE " +
           "(:minPrice IS NULL OR h.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR h.price <= :maxPrice) AND " +
           "(:bedrooms IS NULL OR h.bedrooms = :bedrooms) AND " +
           "(:location IS NULL OR LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "ORDER BY h.createdAt DESC")
    List<HousingListing> searchListings(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        @Param("bedrooms") Integer bedrooms, 
        @Param("location") String location
    );
    
    @Query("SELECT COUNT(h) FROM HousingListing h WHERE h.verified = true")
    long countVerifiedListings();
}
