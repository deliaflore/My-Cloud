package org.distributed.stumatchdistributed.roommate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.roommate.model.RoommateMatch;
import org.distributed.stumatchdistributed.roommate.model.RoommateProfile;
import org.distributed.stumatchdistributed.roommate.repository.RoommateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoommateMatchingService {

    private final RoommateProfileRepository profileRepository;

    public RoommateProfile getMyProfile(UserAccount user) {
        log.info("Fetching profile for user: {}", user.getEmail());
        return profileRepository.findByUserId(user.getId())
                .orElse(null);
    }

    @Transactional
    public RoommateProfile createOrUpdateProfile(RoommateProfile profile, UserAccount user) {
        log.info("Creating/updating profile for user: {}", user.getEmail());

        RoommateProfile existing = profileRepository.findByUserId(user.getId()).orElse(null);

        if (existing != null) {
            // Update existing profile
            existing.setMinBudget(profile.getMinBudget());
            existing.setMaxBudget(profile.getMaxBudget());
            existing.setStudyHabits(profile.getStudyHabits());
            existing.setCleanliness(profile.getCleanliness());
            existing.setLifestyle(profile.getLifestyle());
            existing.setSmoker(profile.getSmoker());
            existing.setPetFriendly(profile.getPetFriendly());
            existing.setBio(profile.getBio());
            return profileRepository.save(existing);
        } else {
            // Create new profile
            profile.setUser(user);
            return profileRepository.save(profile);
        }
    }

    public List<RoommateMatch> findMatches(UserAccount user) {
        log.info("Finding matches for user: {}", user.getEmail());

        RoommateProfile userProfile = getMyProfile(user);
        if (userProfile == null) {
            log.warn("User {} has no profile, cannot find matches", user.getEmail());
            return new ArrayList<>();
        }

        List<RoommateProfile> allProfiles = profileRepository.findAllExceptUser(user.getId());

        return allProfiles.stream()
                .map(candidateProfile -> calculateCompatibility(userProfile, candidateProfile))
                .filter(match -> match.getCompatibilityScore() > 40.0) // Only show matches above 40%
                .sorted(Comparator.comparing(RoommateMatch::getCompatibilityScore).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    private RoommateMatch calculateCompatibility(RoommateProfile userProfile, RoommateProfile candidateProfile) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();

        // Budget compatibility (30 points)
        if (budgetsOverlap(userProfile, candidateProfile)) {
            score += 30.0;
            reasons.add("Compatible budget");
        }

        // Study habits (25 points)
        if (userProfile.getStudyHabits() != null &&
            userProfile.getStudyHabits().equals(candidateProfile.getStudyHabits())) {
            score += 25.0;
            reasons.add("Same study habits");
        } else if ("flexible".equals(userProfile.getStudyHabits()) ||
                   "flexible".equals(candidateProfile.getStudyHabits())) {
            score += 15.0;
            reasons.add("Flexible schedule");
        }

        // Cleanliness (25 points)
        if (userProfile.getCleanliness() != null &&
            userProfile.getCleanliness().equals(candidateProfile.getCleanliness())) {
            score += 25.0;
            reasons.add("Same cleanliness standards");
        } else if ("moderate".equals(userProfile.getCleanliness()) ||
                   "moderate".equals(candidateProfile.getCleanliness())) {
            score += 15.0;
            reasons.add("Moderate cleanliness");
        }

        // Lifestyle (20 points)
        if (userProfile.getLifestyle() != null &&
            userProfile.getLifestyle().equals(candidateProfile.getLifestyle())) {
            score += 20.0;
            reasons.add("Similar lifestyle");
        }

        // Smoking compatibility (bonus/penalty)
        if (userProfile.getSmoker() != null && candidateProfile.getSmoker() != null) {
            if (userProfile.getSmoker().equals(candidateProfile.getSmoker())) {
                score += 5.0;
            } else {
                score -= 10.0; // Penalty for mismatch
            }
        }

        // Pet compatibility (bonus)
        if (Boolean.TRUE.equals(userProfile.getPetFriendly()) &&
            Boolean.TRUE.equals(candidateProfile.getPetFriendly())) {
            score += 5.0;
            reasons.add("Both pet-friendly");
        }

        // Ensure score is between 0 and 100
        score = Math.max(0, Math.min(100, score));

        String matchReason = reasons.isEmpty() ? "Basic compatibility" : String.join(", ", reasons);

        return new RoommateMatch(
                candidateProfile.getUser(),
                candidateProfile,
                score,
                matchReason
        );
    }

    private boolean budgetsOverlap(RoommateProfile p1, RoommateProfile p2) {
        if (p1.getMinBudget() == null || p1.getMaxBudget() == null ||
            p2.getMinBudget() == null || p2.getMaxBudget() == null) {
            return false;
        }

        // Check if the budget ranges overlap
        return p1.getMaxBudget().compareTo(p2.getMinBudget()) >= 0 &&
               p2.getMaxBudget().compareTo(p1.getMinBudget()) >= 0;
    }

    public long countProfiles() {
        return profileRepository.count();
    }

    public long countMatchesForUser(UserAccount user) {
        return findMatches(user).size();
    }
}
