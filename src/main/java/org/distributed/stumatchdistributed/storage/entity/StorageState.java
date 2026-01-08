package org.distributed.stumatchdistributed.storage.entity;

/**
 * Tracks lifecycle of a user's dedicated virtual disk.
 */
public enum StorageState {
    PROVISIONING,
    READY,
    MOUNTED,
    DEGRADED,
    ERROR
}

