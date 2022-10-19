package com.example.torrent.service;

import com.example.torrent.AbstractIntegrationTest;
import com.example.torrent.model.TorrentFileResult;
import com.example.torrent.model.entity.File;
import com.example.torrent.model.entity.Torrent;
import com.example.torrent.model.entity.TorrentFile;
import org.junit.jupiter.api.Test;
import org.libtorrent4j.TorrentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Path;

import static com.example.torrent.service.DirectoryService.getPiecesRoot;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class MetadataServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private MetadataService metadataService;
    @Value("classpath:data.txt")
    private Resource torrentData;
    @Value("classpath:data.txt.v2.torrent")
    private Resource v2TorrentMetadata;
    @Value("classpath:data.txt.v1.torrent")
    private Resource v1TorrentMetadata;
    @Autowired
    private EntityManager entityManager;

    @Test
    void findTorrentByFile_v2() throws IOException {
        TorrentInfo torrentInfo = new TorrentInfo(v2TorrentMetadata.getFile());
        Torrent torrent = metadataService.addTorrent(torrentInfo);
        entityManager.refresh(torrent);
        TorrentFile torrentFile = torrent.getFiles().stream().findFirst().orElseThrow();

        Path path = Path.of(torrentData.getFile().getPath());
        File file = File.builder()
                .path(path.toString())
                .piecesRoot(getPiecesRoot(path))
                .matchingTorrentFiles(emptySet())
                .nonMatchingTorrentFiles(emptySet())
                .build();
        TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);
        assertThat(torrentFileResult.getMatchingTorrentFiles()).containsExactly(torrentFile);
        assertThat(torrentFileResult.getNonMatchingTorrentFiles()).isEmpty();
    }

    @Test
    void findTorrentByFile_v1() throws IOException {
        TorrentInfo torrentInfo = new TorrentInfo(v1TorrentMetadata.getFile());
        Torrent torrent = metadataService.addTorrent(torrentInfo);
        entityManager.refresh(torrent);
        TorrentFile torrentFile = torrent.getFiles().stream().findFirst().orElseThrow();

        Path path = Path.of(torrentData.getFile().getPath());
        File file = File.builder()
                .path(path.toString())
                .piecesRoot(getPiecesRoot(path))
                .matchingTorrentFiles(emptySet())
                .nonMatchingTorrentFiles(emptySet())
                .build();

        TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);
        assertThat(torrentFileResult.getMatchingTorrentFiles()).containsExactly(torrentFile);
        assertThat(torrentFileResult.getNonMatchingTorrentFiles()).isEmpty();
    }

    @Test
    void findTorrentByFile_existing() throws IOException {
        TorrentInfo torrentInfo = new TorrentInfo(v1TorrentMetadata.getFile());
        Torrent torrent = metadataService.addTorrent(torrentInfo);
        entityManager.refresh(torrent);
        TorrentFile torrentFile = torrent.getFiles().stream().findFirst().orElseThrow();

        Path path = Path.of(torrentData.getFile().getPath());
        File file = File.builder()
                .path(path.toString())
                .piecesRoot(getPiecesRoot(path))
                .matchingTorrentFiles(singletonList(torrentFile))
                .nonMatchingTorrentFiles(emptySet())
                .build();

        TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);
        assertThat(torrentFileResult.getMatchingTorrentFiles()).containsExactly(torrentFile);
        assertThat(torrentFileResult.getNonMatchingTorrentFiles()).isEmpty();
    }
}