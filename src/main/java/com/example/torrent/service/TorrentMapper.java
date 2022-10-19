package com.example.torrent.service;

import com.example.torrent.model.api.TorrentDTO;
import com.example.torrent.model.api.TorrentStatusDTO;
import org.libtorrent4j.TorrentHandle;
import org.libtorrent4j.TorrentStatus;
import org.mapstruct.Mapper;

import static java.util.Arrays.stream;
import static org.libtorrent4j.TorrentHandle.PIECE_GRANULARITY;

@Mapper(componentModel = "spring")
public interface TorrentMapper {
    default TorrentDTO toDTO(TorrentHandle torrentHandle) {
        return TorrentDTO.builder()
                .name(torrentHandle.getName())
                .progress(torrentHandle.status().progress())
                .status(toDTO(torrentHandle.status().state()))
                .seeds(torrentHandle.status().numSeeds())
                .peers(torrentHandle.status().numPeers())
                .downloadRate(torrentHandle.status().downloadRate())
                .uploadRate(torrentHandle.status().uploadRate())
                .size(stream(torrentHandle.fileProgress(PIECE_GRANULARITY)).sum())
                .build();
    }

    TorrentStatusDTO toDTO(TorrentStatus.State state);
}
