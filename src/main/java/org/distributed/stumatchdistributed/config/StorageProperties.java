package org.distributed.stumatchdistributed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Base directory for storing user virtual disks.
     */
    private Path baseDir = Path.of(System.getProperty("user.home"), "distributed-storage");

    /**
     * Subdirectory dedicated to user disks.
     */
    private String userDir = "users";

    public Path getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    public String getUserDir() {
        return userDir;
    }

    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    public Path userDisksPath() {
        return baseDir.resolve(userDir);
    }
}

