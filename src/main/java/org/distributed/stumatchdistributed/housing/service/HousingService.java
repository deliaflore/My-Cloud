package org.distributed.stumatchdistributed.housing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.housing.model.HousingListing;
import org.distributed.stumatchdistributed.housing.repository.HousingListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HousingService {
    
    private final HousingListingRepository housingListingRepository;

    public List<HousingListing> getAllListings() {
        log.info("Fetching all housing listings");
        return housingListingRepository.findAll();
    }

    public List<HousingListing> getVerifiedListings() {
        log.info("Fetching verified housing listings");
        return housingListingRepository.findByVerifiedTrue();
    }

    public HousingListing getListingById(Long id) {
        log.info("Fetching listing with id: {}", id);
        return housingListingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Listing not found with id: " + id));
    }

    public List<HousingListing> getMyListings(UserAccount user) {
        log.info("Fetching listings for user: {}", user.getEmail());
        return housingListingRepository.findByLandlordId(user.getId());
    }

    @Transactional
    public HousingListing createListing(HousingListing listing, UserAccount landlord) {
        log.info("Creating new listing: {} by user: {}", listing.getTitle(), landlord.getEmail());
        listing.setLandlord(landlord);
        listing.setVerified(false); // New listings need verification
        return housingListingRepository.save(listing);
    }

    @Transactional
    public HousingListing updateListing(Long id, HousingListing updatedListing, UserAccount user) {
        log.info("Updating listing with id: {}", id);
        HousingListing existing = getListingById(id);
        
        // Check if user owns this listing
        if (!existing.getLandlord().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own listings");
        }

        existing.setTitle(updatedListing.getTitle());
        existing.setDescription(updatedListing.getDescription());
        existing.setPrice(updatedListing.getPrice());
        existing.setLocation(updatedListing.getLocation());
        existing.setLatitude(updatedListing.getLatitude());
        existing.setLongitude(updatedListing.getLongitude());
        existing.setBedrooms(updatedListing.getBedrooms());
        existing.setBathrooms(updatedListing.getBathrooms());
        existing.setImageUrl(updatedListing.getImageUrl());

        return housingListingRepository.save(existing);
    }

    @Transactional
    public void deleteListing(Long id, UserAccount user) {
        log.info("Deleting listing with id: {}", id);
        HousingListing listing = getListingById(id);
        
        // Check if user owns this listing
        if (!listing.getLandlord().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own listings");
        }

        housingListingRepository.delete(listing);
    }

    public List<HousingListing> searchListings(BigDecimal minPrice, BigDecimal maxPrice, 
                                                Integer bedrooms, String location) {
        log.info("Searching listings with filters - minPrice: {}, maxPrice: {}, bedrooms: {}, location: {}", 
                 minPrice, maxPrice, bedrooms, location);
        return housingListingRepository.searchListings(minPrice, maxPrice, bedrooms, location);
    }

    public long countAvailableListings() {
        return housingListingRepository.count();
    }

    public long countVerifiedListings() {
        return housingListingRepository.countVerifiedListings();
    }
}
