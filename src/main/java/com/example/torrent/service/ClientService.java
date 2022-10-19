package com.example.torrent.service;

import lombok.RequiredArgsConstructor;
import org.libtorrent4j.SessionManager;
import org.libtorrent4j.TorrentHandle;
import org.libtorrent4j.TorrentInfo;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

import static org.libtorrent4j.TorrentFlags.NEED_SAVE_RESUME;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final SessionManager sessionManager;

    public List<TorrentHandle> getTorrents() {
        return sessionManager.swig()
                .get_torrents()
                .stream()
                .map(TorrentHandle::new)
                .toList();
    }

    public void downloadTorrent(TorrentInfo torrentInfo) {
        sessionManager.download(torrentInfo, new File("downloads"), null, null, null, NEED_SAVE_RESUME);
    }
}
