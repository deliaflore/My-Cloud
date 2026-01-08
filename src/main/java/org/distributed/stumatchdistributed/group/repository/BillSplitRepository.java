package org.distributed.stumatchdistributed.group.repository;

import org.distributed.stumatchdistributed.group.model.BillSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillSplitRepository extends JpaRepository<BillSplit, Long> {
    
    List<BillSplit> findByBillId(Long billId);
    
    List<BillSplit> findByUserId(UUID userId);
    
    @Query("SELECT SUM(s.amount) FROM BillSplit s WHERE s.user.id = :userId AND s.paid = false")
    BigDecimal sumUnpaidAmountByUserId(@Param("userId") UUID userId);
}
