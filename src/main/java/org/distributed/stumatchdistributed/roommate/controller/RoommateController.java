package org.distributed.stumatchdistributed.roommate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.service.UserContextService;
import org.distributed.stumatchdistributed.roommate.model.RoommateMatch;
import org.distributed.stumatchdistributed.roommate.model.RoommateProfile;
import org.distributed.stumatchdistributed.roommate.service.RoommateMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roommates")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RoommateController {

    private final RoommateMatchingService matchingService;
    private final UserContextService userContextService;

    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            log.info("API request: GET /api/roommates/profile");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            RoommateProfile profile = matchingService.getMyProfile(user);
            
            if (profile == null) {
                return ResponseEntity.ok(Map.of("exists", false));
            }
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Failed to fetch profile", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> createOrUpdateProfile(
            @RequestBody RoommateProfile profile,
            Authentication authentication) {
        try {
            log.info("API request: POST /api/roommates/profile");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            RoommateProfile saved = matchingService.createOrUpdateProfile(profile, user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Failed to save profile", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/matches")
    public ResponseEntity<?> findMatches(Authentication authentication) {
        try {
            log.info("API request: GET /api/roommates/matches");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            List<RoommateMatch> matches = matchingService.findMatches(user);
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            log.error("Failed to find matches", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        try {
            log.info("API request: GET /api/roommates/stats");
            UserAccount user = userContextService.requireUserByEmail(authentication.getName());
            long totalProfiles = matchingService.countProfiles();
            long myMatches = matchingService.countMatchesForUser(user);
            
            return ResponseEntity.ok(Map.of(
                    "totalProfiles", totalProfiles,
                    "myMatches", myMatches
            ));
        } catch (Exception e) {
            log.error("Failed to get stats", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
