package org.distributed.stumatchdistributed.housing.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.service.UserContextService;
import org.distributed.stumatchdistributed.housing.model.HousingListing;
import org.distributed.stumatchdistributed.housing.service.HousingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/housing")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HousingController {

    private final HousingService housingService;
    private final UserContextService userContextService;

    @GetMapping("/listings")
    public ResponseEntity<List<HousingListing>> getAllListings() {
        log.info("API request: GET /api/housing/listings");
        List<HousingListing> listings = housingService.getAllListings();
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/listings/verified")
    public ResponseEntity<List<HousingListing>> getVerifiedListings() {
        log.info("API request: GET /api/housing/listings/verified");
        List<HousingListing> listings = housingService.getVerifiedListings();
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/listings/my")
    public ResponseEntity<?> getMyListings(Authentication authentication) {
        try {
            log.info("API request: GET /api/housing/listings/my");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            List<HousingListing> listings = housingService.getMyListings(user);
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            log.error("Failed to fetch user listings", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/listings/{id}")
    public ResponseEntity<?> getListingById(@PathVariable Long id) {
        try {
            log.info("API request: GET /api/housing/listings/{}", id);
            HousingListing listing = housingService.getListingById(id);
            return ResponseEntity.ok(listing);
        } catch (Exception e) {
            log.error("Failed to fetch listing", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/listings")
    public ResponseEntity<?> createListing(
            @RequestBody HousingListing listing,
            Authentication authentication) {
        try {
            log.info("API request: POST /api/housing/listings");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            HousingListing created = housingService.createListing(listing, user);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create listing", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/listings/{id}")
    public ResponseEntity<?> updateListing(
            @PathVariable Long id,
            @RequestBody HousingListing listing,
            Authentication authentication) {
        try {
            log.info("API request: PUT /api/housing/listings/{}", id);
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            HousingListing updated = housingService.updateListing(id, listing, user);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            log.error("Failed to update listing", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<?> deleteListing(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            log.info("API request: DELETE /api/housing/listings/{}", id);
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            housingService.deleteListing(id, user);
            return ResponseEntity.ok(Map.of("message", "Listing deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete listing", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<HousingListing>> searchListings(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) String location) {
        log.info("API request: GET /api/housing/search - minPrice: {}, maxPrice: {}, bedrooms: {}, location: {}", 
                 minPrice, maxPrice, bedrooms, location);
        List<HousingListing> listings = housingService.searchListings(
                minPrice, maxPrice, bedrooms, location);
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("API request: GET /api/housing/stats");
        long total = housingService.countAvailableListings();
        long verified = housingService.countVerifiedListings();
        return ResponseEntity.ok(Map.of(
                "total", total,
                "verified", verified,
                "unverified", total - verified
        ));
    }
}
