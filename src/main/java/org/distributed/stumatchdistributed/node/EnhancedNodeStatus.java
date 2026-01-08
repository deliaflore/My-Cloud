package org.distributed.stumatchdistributed.node;

public class EnhancedNodeStatus {
    private final String nodeId;
    private final String ipAddress;
    private final String macAddress;
    private final int port;
    private final NodeLifecycleInfo lifecycleInfo;
    private final org.distributed.stumatchdistributed.virtualdisk.DiskStats diskStats;
    private final java.util.List<org.distributed.stumatchdistributed.process.ProcessInfo> processes;
    private final int ramGB;
    private final int cpuCores;

    public EnhancedNodeStatus(String nodeId, String ipAddress, String macAddress, int port,
                              NodeLifecycleInfo lifecycleInfo,
                              org.distributed.stumatchdistributed.virtualdisk.DiskStats diskStats,
                              java.util.List<org.distributed.stumatchdistributed.process.ProcessInfo> processes,
                              int ramGB, int cpuCores) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.port = port;
        this.lifecycleInfo = lifecycleInfo;
        this.diskStats = diskStats;
        this.processes = processes;
        this.ramGB = ramGB;
        this.cpuCores = cpuCores;
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public String getIpAddress() { return ipAddress; }
    public String getMacAddress() { return macAddress; }
    public int getPort() { return port; }
    public NodeLifecycleInfo getLifecycleInfo() { return lifecycleInfo; }
    public org.distributed.stumatchdistributed.virtualdisk.DiskStats getDiskStats() { return diskStats; }
    public java.util.List<org.distributed.stumatchdistributed.process.ProcessInfo> getProcesses() { return processes; }
    public int getRamGB() { return ramGB; }
    public int getCpuCores() { return cpuCores; }
}
