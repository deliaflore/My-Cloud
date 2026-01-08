package org.distributed.stumatchdistributed.virtualdisk;

public class DiskStats {
    private final String diskId;
    private final long totalBytes;
    private final long usedBytes;
    private final double utilizationPercent;
    private final int fileCount;
    private final VirtualDisk.DiskState state;
    private final boolean formatted;
    private final boolean mounted;

    public DiskStats(String diskId, long totalBytes, long usedBytes,
                     double utilizationPercent, int fileCount,
                     VirtualDisk.DiskState state, boolean formatted, boolean mounted) {
        this.diskId = diskId;
        this.totalBytes = totalBytes;
        this.usedBytes = usedBytes;
        this.utilizationPercent = utilizationPercent;
        this.fileCount = fileCount;
        this.state = state;
        this.formatted = formatted;
        this.mounted = mounted;
    }

    // Getters
    public String getDiskId() { return diskId; }
    public long getTotalBytes() { return totalBytes; }
    public long getUsedBytes() { return usedBytes; }
    public double getUtilizationPercent() { return utilizationPercent; }
    public int getFileCount() { return fileCount; }
    public VirtualDisk.DiskState getState() { return state; }
    public boolean isFormatted() { return formatted; }
    public boolean isMounted() { return mounted; }
}
