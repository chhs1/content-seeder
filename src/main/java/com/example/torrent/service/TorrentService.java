package com.example.torrent.service;

import com.example.torrent.config.TorrentConfigurationProperties;
import com.example.torrent.model.Hash;
import com.example.torrent.model.entity.Torrent;
import com.example.torrent.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.libtorrent4j.Entry;
import org.libtorrent4j.Priority;
import org.libtorrent4j.SessionManager;
import org.libtorrent4j.TorrentInfo;
import org.libtorrent4j.swig.entry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.torrent.service.MetadataService.getBestHash;
import static java.util.stream.Collectors.*;
import static org.libtorrent4j.Priority.DEFAULT;
import static org.libtorrent4j.Priority.IGNORE;
import static org.libtorrent4j.TorrentFlags.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TorrentService {
    private final SessionManager sessionManager;
    private final TorrentConfigurationProperties torrentConfigurationProperties;
    private final FileRepository fileRepository;

    public TorrentInfo getTorrentInfo(Torrent torrent) {
        Entry entry = new Entry(new entry());
        entry.dictionary().put("info", Entry.bdecode(torrent.getInfo()));
        return new TorrentInfo(entry.bencode());
    }

    @EventListener
    @Transactional
    public void onStart(ApplicationReadyEvent ignored) {
        try {
            Set<Hash> activeTorrents = getActiveTorrentInfoHashes();

            log.info("Starting torrents with matching files");
            Map<Torrent, List<Pair<Integer, Path>>> torrentPairMap = fileRepository.findAll()
                    .stream()
                    .filter(file -> !file.getMatchingTorrentFiles().isEmpty())
                    .flatMap(file -> file.getMatchingTorrentFiles().stream().map(torrentFile -> Pair.of(file, torrentFile)))
                    .collect(groupingBy(
                            pair -> pair.getValue().getTorrent(),
                            mapping(fileTorrentFilePair -> Pair.of(fileTorrentFilePair.getValue().getFileIndex(), Path.of(fileTorrentFilePair.getKey().getPath())), toList())
                    ));

            log.info("{} torrents with matching files found", torrentPairMap.size());

            for (Torrent torrent : torrentPairMap.keySet()) {
                TorrentInfo torrentInfo = getTorrentInfo(torrent);
                List<Pair<Integer, Path>> matchingFiles = torrentPairMap.get(torrent);

                if (getV1Hash(torrentInfo).map(activeTorrents::contains).orElse(false)) {
                    log.info("Torrent {} is already active, skipping", getBestHash(torrent));
                    continue;
                }

                if (getV2Hash(torrentInfo).map(activeTorrents::contains).orElse(false)) {
                    log.info("Torrent {} is already active, skipping", getBestHash(torrent));
                    continue;
                }

                if (matchingFiles.isEmpty()) {
                    continue;
                }

                Priority[] priorities = new Priority[torrentInfo.numFiles()];
                Arrays.fill(priorities, IGNORE);

                matchingFiles.forEach(pair -> {
                    Integer fileIndex = pair.getKey();
                    Path path = pair.getValue();
                    torrentInfo.renameFile(fileIndex, path.toAbsolutePath().toString());
                    priorities[fileIndex] = DEFAULT;
                });

                log.info("Starting torrent {} for {} files", getBestHash(torrent), matchingFiles.size());
                sessionManager.download(torrentInfo, torrentConfigurationProperties.getDownloadPath().toFile(), null, priorities, Collections.emptyList(), SEED_MODE.or_(AUTO_MANAGED).or_(PAUSED));
            }

            log.info("Finished starting torrents");
        } catch (Exception e) {
            log.error("Exception while starting torrents", e);
        }
    }

    private Set<Hash> getActiveTorrentInfoHashes() {
        return sessionManager.swig()
                .get_torrents()
                .stream()
                .flatMap(torrentHandle -> {
                    boolean hasV1 = torrentHandle.torrent_file_ptr().info_hashes().has_v1();
                    boolean hasV2 = torrentHandle.torrent_file_ptr().info_hashes().has_v2();

                    Optional<Hash> v1 = hasV1 ? Optional.of(Hash.of(torrentHandle.torrent_file_ptr().info_hashes().getV1())) : Optional.empty();
                    Optional<Hash> v2 = hasV2 ? Optional.of(Hash.of(torrentHandle.torrent_file_ptr().info_hashes().getV2())) : Optional.empty();

                    return Stream.concat(v1.stream(), v2.stream());
                })
                .collect(Collectors.toSet());
    }

    private Optional<Hash> getV1Hash(TorrentInfo torrentInfo) {
        return torrentInfo.hasV1() ? Optional.of(Hash.of(torrentInfo.swig().info_hashes().getV1())) : Optional.empty();
    }

    private Optional<Hash> getV2Hash(TorrentInfo torrentInfo) {
        return torrentInfo.hasV2() ? Optional.of(Hash.of(torrentInfo.swig().info_hashes().getV2())) : Optional.empty();
    }
}
