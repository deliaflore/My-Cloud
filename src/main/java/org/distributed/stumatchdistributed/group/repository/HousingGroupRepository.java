package org.distributed.stumatchdistributed.group.repository;

import org.distributed.stumatchdistributed.group.model.HousingGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HousingGroupRepository extends JpaRepository<HousingGroup, Long> {
    
    @Query("SELECT g FROM HousingGroup g JOIN g.members m WHERE m.id = :userId")
    Optional<HousingGroup> findByMemberId(@Param("userId") UUID userId);
    
    boolean existsByMembersId(UUID userId);
}
