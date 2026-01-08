package org.distributed.stumatchdistributed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Distributed Storage System.
 *
 * This is a Spring Boot application that:
 * 1. Starts a web server on port 8080
 * 2. Exposes REST APIs for network management
 * 3. Coordinates distributed file storage
 *
 * Architecture:
 * - Layered architecture with clear separation of concerns
 * - Dependency injection for loose coupling
 * - REST APIs for external access
 * - gRPC for inter-node communication
 *
 * @author Your Name
 * @version 1.0
 */
import org.distributed.stumatchdistributed.config.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class StumatchDistributedApplication {
    private static final Logger log = LoggerFactory.getLogger(StumatchDistributedApplication.class);

    public static void main(String[] args) {
        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║   STUMATCH DISTRIBUTED STORAGE SYSTEM                     ║");
        log.info("║   Starting Application...                                 ║");
        log.info("╚═══════════════════════════════════════════════════════════╝");

        SpringApplication.run(StumatchDistributedApplication.class, args);

        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║   Application Started Successfully!                       ║");
        log.info("║   REST API available at: http://localhost:8080/api        ║");
        log.info("║                                                           ║");
        log.info("║   Available Endpoints:                                    ║");
        log.info("║   - GET  /api/network/nodes                              ║");
        log.info("║   - GET  /api/network/status                             ║");
        log.info("║   - POST /api/network/nodes/register                     ║");
        log.info("║   - POST /api/files/upload                               ║");
        log.info("║   - GET  /api/files/test                                 ║");
        log.info("╚═══════════════════════════════════════════════════════════╝");
    }
}
