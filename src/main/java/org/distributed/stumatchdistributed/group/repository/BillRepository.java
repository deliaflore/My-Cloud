package org.distributed.stumatchdistributed.group.repository;

import org.distributed.stumatchdistributed.group.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    List<Bill> findByGroupIdOrderByDueDateDesc(Long groupId);
}
