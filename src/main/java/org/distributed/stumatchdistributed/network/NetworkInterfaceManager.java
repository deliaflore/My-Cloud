package org.distributed.stumatchdistributed.network;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages network interfaces and IP address assignment.
 *
 * Automatically assigns IP addresses to nodes in a virtual subnet.
 * Simulates a DHCP server.
 *
 * @author Your Name
 * @version 1.0
 */
public class NetworkInterfaceManager {
    private static final Logger log = LoggerFactory.getLogger(NetworkInterfaceManager.class);

    // Singleton instance
    private static NetworkInterfaceManager instance;

    // Virtual subnet: 192.168.100.0/24
    private static final String SUBNET_PREFIX = "192.168.100.";
    private static final int SUBNET_START = 10;  // Start from .10
    private static final int SUBNET_END = 254;   // End at .254

    private final AtomicInteger nextIpSuffix;
    private final ConcurrentHashMap<String, NetworkInterface> nodeInterfaces;

    private NetworkInterfaceManager() {
        this.nextIpSuffix = new AtomicInteger(SUBNET_START);
        this.nodeInterfaces = new ConcurrentHashMap<>();
    }

    public static synchronized NetworkInterfaceManager getInstance() {
        if (instance == null) {
            instance = new NetworkInterfaceManager();
        }
        return instance;
    }

    /**
     * Assigns a network interface to a node.
     * Automatically allocates an IP address from the pool.
     *
     * @param nodeId Node identifier
     * @return Assigned network interface
     */
    public synchronized NetworkInterface assignInterface(String nodeId) {
        // Check if already assigned
        if (nodeInterfaces.containsKey(nodeId)) {
            log.warn("Node {} already has an interface assigned", nodeId);
            return nodeInterfaces.get(nodeId);
        }

        // Get next available IP
        int ipSuffix = nextIpSuffix.getAndIncrement();

        if (ipSuffix > SUBNET_END) {
            log.error("❌ IP address pool exhausted!");
            return null;
        }

        String ipAddress = SUBNET_PREFIX + ipSuffix;

        // Create network interface
        NetworkInterface netInterface = new NetworkInterface(
                nodeId,
                ipAddress,
                "virtual-eth0",
                "02:00:00:00:00:" + String.format("%02X", ipSuffix)  // MAC address
        );

        nodeInterfaces.put(nodeId, netInterface);

        log.info("🌐 Network interface assigned to {}", nodeId);
        log.info("   IP Address: {}", ipAddress);
        log.info("   MAC Address: {}", netInterface.getMacAddress());

        return netInterface;
    }

    /**
     * Releases a network interface (IP address freed).
     */
    public synchronized boolean releaseInterface(String nodeId) {
        NetworkInterface netInterface = nodeInterfaces.remove(nodeId);

        if (netInterface != null) {
            log.info("🔌 Network interface released from {}", nodeId);
            return true;
        }

        return false;
    }

    /**
     * Gets interface for a specific node.
     */
    public NetworkInterface getInterface(String nodeId) {
        return nodeInterfaces.get(nodeId);
    }

    /**
     * Lists all active interfaces.
     */
    public java.util.Collection<NetworkInterface> getAllInterfaces() {
        return nodeInterfaces.values();
    }
}

