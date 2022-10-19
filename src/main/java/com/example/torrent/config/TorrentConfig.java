package com.example.torrent.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.libtorrent4j.*;
import org.libtorrent4j.swig.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TorrentConfig {
    private final TorrentConfigurationProperties torrentConfigurationProperties;

    @Bean(destroyMethod = "stop")
    public SessionManager sessionManager(AlertListener alertListener) throws IOException {
        SettingsPack settingsPack = new SettingsPack();
        settingsPack.setEnableDht(true);
        settingsPack.activeLimit(torrentConfigurationProperties.getActiveLimit());

        SessionParams sessionParams = new SessionParams();
        sessionParams.setSettings(settingsPack);

        SessionManager sessionManager = new SessionManager() {
            @Override
            protected void onBeforeStop() {
                log.info("Saving resume data");
                swig().get_torrents().forEach(torrent_handle::save_resume_data);
            }
        };
        sessionManager.addListener(alertListener);
        sessionManager.start(sessionParams);

        SessionHandle sessionHandle = new SessionHandle(sessionManager.swig());

        List<Path> resumeFiles = Files.find(torrentConfigurationProperties.getResumePath(), 1, (path, basicFileAttributes) -> FilenameUtils.isExtension(path.getFileName().toString(), "resume")).toList();

        for (Path resumeFilePath : resumeFiles) {
            log.info("Loading resume file {}", resumeFilePath);
            byte[] resumeData = Files.readAllBytes(resumeFilePath);
            error_code error_code = new error_code();
            add_torrent_params params = libtorrent.read_resume_data_ex(new byte_vector(resumeData), error_code);

            if (error_code.failed()) {
                throw new RuntimeException(error_code.message());
            }

            sessionHandle.swig().async_add_torrent(params);
        }

        return sessionManager;
    }
}
