package com.example.torrent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "torrent")
public class TorrentConfigurationProperties {
    private Path downloadPath;
    private Path resumePath;
    private List<Path> contentPaths = Collections.emptyList();
    private Duration fileScanInterval;
    private int activeLimit;
}
