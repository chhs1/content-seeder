package com.example.torrent.service;

import com.example.torrent.config.TorrentConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.libtorrent4j.AddTorrentParams;
import org.libtorrent4j.TorrentInfo;
import org.libtorrent4j.alerts.*;
import org.libtorrent4j.swig.info_hash_t;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertListener implements org.libtorrent4j.AlertListener {
    private final TorrentConfigurationProperties torrentConfigurationProperties;

    @Override
    public int[] types() {
        return null;
    }

    @SneakyThrows
    @Override
    public void alert(Alert<?> alert) {
        AlertType alertType = alert.type();

        switch (alertType) {
            case ADD_TORRENT -> {
                AddTorrentAlert addTorrentAlert = (AddTorrentAlert) alert;
                addTorrentAlert.handle().resume();
                log.info("Torrent added {}", addTorrentAlert.torrentName());
            }
            case TORRENT_FINISHED -> {
                TorrentFinishedAlert torrentFinishedAlert = (TorrentFinishedAlert) alert;
                log.info("Torrent finished {}", torrentFinishedAlert.torrentName());
            }
            case SAVE_RESUME_DATA -> {
                SaveResumeDataAlert saveResumeDataAlert = (SaveResumeDataAlert) alert;
                log.info("Saving resume data {}", saveResumeDataAlert.torrentName());
                Files.write(getResumePath(saveResumeDataAlert.handle().torrentFile()), AddTorrentParams.writeResumeDataBuf(saveResumeDataAlert.params()));
            }
        }
    }

    private Path getResumePath(TorrentInfo torrentInfo) {
        info_hash_t infoHash = torrentInfo.swig().info_hashes();

        if (infoHash.has_v2()) {
            return torrentConfigurationProperties.getResumePath().resolve(infoHash.getV2().to_hex() + ".resume");
        } else if (infoHash.has_v1()) {
            return torrentConfigurationProperties.getResumePath().resolve(infoHash.getV1().to_hex() + ".resume");
        } else {
            throw new IllegalArgumentException("Unknown info hash");
        }
    }
}
