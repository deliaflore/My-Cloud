package org.distributed.stumatchdistributed.storage.service;

import org.distributed.stumatchdistributed.auth.entity.UserAccount;
import org.distributed.stumatchdistributed.auth.repository.UserAccountRepository;
import org.distributed.stumatchdistributed.config.StorageProperties;
import org.distributed.stumatchdistributed.storage.entity.StorageState;
import org.distributed.stumatchdistributed.storage.entity.UserStorage;
import org.distributed.stumatchdistributed.storage.repository.UserStorageRepository;
import org.distributed.stumatchdistributed.virtualdisk.VirtualDisk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class UserStorageService {

    private static final Logger log = LoggerFactory.getLogger(UserStorageService.class);

    private final UserStorageRepository userStorageRepository;
    private final UserAccountRepository userAccountRepository;
    private final StorageProperties storageProperties;

    public UserStorageService(UserStorageRepository userStorageRepository,
                              UserAccountRepository userAccountRepository,
                              StorageProperties storageProperties) {
        this.userStorageRepository = userStorageRepository;
        this.userAccountRepository = userAccountRepository;
        this.storageProperties = storageProperties;
    }

    /**
     * Provision a dedicated virtual disk for a user.
     */
    @Transactional
    public UserStorage provision(UserAccount user) {
        return userStorageRepository.findByUserId(user.getId())
                .orElseGet(() -> createStorage(user));
    }

    public UserStorage getStorage(UserAccount user) {
        return userStorageRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User storage not provisioned"));
    }

    public VirtualDisk attachDisk(UserStorage storage) {
        Path diskPath = Path.of(storage.getDiskPath());
        Path baseDir = diskPath.getParent();
        try {
            return new VirtualDisk(storage.getDiskId(), (int) (storage.getQuotaBytes() / (1024 * 1024 * 1024)), baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to attach virtual disk", e);
        }
    }

    private UserStorage createStorage(UserAccount user) {
        String diskId = "user-" + user.getId();
        Path usersDir = storageProperties.userDisksPath();
        Path diskFile = usersDir.resolve(diskId + ".vdisk");

        try {
            Files.createDirectories(usersDir);
            int sizeGb = Math.max(1, (int) (user.getStorageQuotaBytes() / (1024 * 1024 * 1024)));
            VirtualDisk virtualDisk = new VirtualDisk(diskId, sizeGb, usersDir);
            if (!virtualDisk.isFormatted()) {
                virtualDisk.format();
            }

            UserStorage storage = UserStorage.builder()
                    .user(user)
                    .diskId(diskId)
                    .diskPath(diskFile.toString())
                    .quotaBytes(user.getStorageQuotaBytes())
                    .usedBytes(0L)
                    .state(StorageState.READY)
                    .build();

            return userStorageRepository.save(storage);

        } catch (IOException e) {
            log.error("Failed to provision storage for {}", user.getEmail(), e);
            throw new IllegalStateException("Failed to provision storage");
        }
    }

    public void assertHasCapacity(UserAccount user, long additionalBytes) {
        long quota = user.getStorageQuotaBytes();
        long used = user.getUsedStorageBytes();
        if (used + additionalBytes > quota) {
            throw new IllegalStateException("Storage quota exceeded");
        }
    }

    @Transactional
    public void incrementUsage(UserAccount user, long deltaBytes) {
        // Update UserStorage (what the dashboard reads)
        UserStorage storage = getStorage(user);
        storage.setUsedBytes(storage.getUsedBytes() + deltaBytes);
        userStorageRepository.save(storage);
        
        // Also update UserAccount for consistency
        user.setUsedStorageBytes(user.getUsedStorageBytes() + deltaBytes);
        userAccountRepository.save(user);
    }

    @Transactional
    public void decrementUsage(UserAccount user, long deltaBytes) {
        // Update UserStorage (what the dashboard reads)
        UserStorage storage = getStorage(user);
        long newStorageValue = Math.max(0, storage.getUsedBytes() - deltaBytes);
        storage.setUsedBytes(newStorageValue);
        userStorageRepository.save(storage);
        
        // Also update UserAccount for consistency
        long newUserValue = Math.max(0, user.getUsedStorageBytes() - deltaBytes);
        user.setUsedStorageBytes(newUserValue);
        userAccountRepository.save(user);
    }
}

