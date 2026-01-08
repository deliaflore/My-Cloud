package org.distributed.stumatchdistributed.roommate.repository;

import org.distributed.stumatchdistributed.roommate.model.RoommateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface RoommateProfileRepository extends JpaRepository<RoommateProfile, Long> {
    
    Optional<RoommateProfile> findByUserId(UUID userId);
    
    @Query("SELECT p FROM RoommateProfile p WHERE p.user.id != :userId")
    List<RoommateProfile> findAllExceptUser(UUID userId);
    
    boolean existsByUserId(UUID userId);
}
