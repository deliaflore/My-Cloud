package org.distributed.stumatchdistributed.network;

/**
 * Represents a virtual network interface.
 */
public class NetworkInterface {
    private final String nodeId;
    private final String ipAddress;
    private final String interfaceName;
    private final String macAddress;
    private final long assignedTime;

    public NetworkInterface(String nodeId, String ipAddress,
                            String interfaceName, String macAddress) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.interfaceName = interfaceName;
        this.macAddress = macAddress;
        this.assignedTime = System.currentTimeMillis();
    }

    public String getNodeId() { return nodeId; }
    public String getIpAddress() { return ipAddress; }
    public String getInterfaceName() { return interfaceName; }
    public String getMacAddress() { return macAddress; }
    public long getAssignedTime() { return assignedTime; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s [%s]",
                nodeId, ipAddress, interfaceName, macAddress);
    }
}
