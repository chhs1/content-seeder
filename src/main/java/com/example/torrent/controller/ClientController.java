package com.example.torrent.controller;

import com.example.torrent.model.api.TorrentDTO;
import com.example.torrent.service.ClientService;
import com.example.torrent.service.TorrentMapper;
import lombok.RequiredArgsConstructor;
import org.libtorrent4j.TorrentInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.torrent.controller.MetadataController.APPLICATION_X_BITTORRENT;

@RestController
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;
    private final TorrentMapper torrentMapper;

    @GetMapping(path = "/api/client/torrents")
    public List<TorrentDTO> getTorrents() {
        return clientService.getTorrents()
                .stream()
                .map(torrentMapper::toDTO)
                .toList();
    }

    @PostMapping(path = "/api/client/torrent/download", consumes = APPLICATION_X_BITTORRENT)
    public void downloadTorrent(@RequestBody byte[] metadata) {
        TorrentInfo torrentInfo = new TorrentInfo(metadata);
        clientService.downloadTorrent(torrentInfo);
    }
}
