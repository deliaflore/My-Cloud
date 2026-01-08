package org.distributed.stumatchdistributed.storage.repository;

import org.distributed.stumatchdistributed.storage.entity.StorageState;
import org.distributed.stumatchdistributed.storage.entity.UserStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserStorageRepository extends JpaRepository<UserStorage, UUID> {
    Optional<UserStorage> findByUserId(UUID userId);

    Optional<UserStorage> findByDiskId(String diskId);

    long countByState(StorageState state);
}

