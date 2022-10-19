package com.example.torrent.service;

import com.example.torrent.config.TorrentConfigurationProperties;
import com.example.torrent.model.TorrentFileResult;
import com.example.torrent.model.entity.File;
import com.example.torrent.repository.FileRepository;
import com.example.torrent.util.CollectingPathVisitor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.libtorrent4j.TorrentInfo;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.transaction.Transactional;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final MetadataService metadataService;
    private final FileRepository fileRepository;
    private final TorrentConfigurationProperties torrentConfigurationProperties;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final TransactionTemplate transactionTemplate;

    @SneakyThrows
    public static byte[] getPiecesRoot(Path path) {
        byte[] buffer = new byte[16384];
        MessageDigest messageDigest = DigestUtils.getSha256Digest();

        List<byte[]> hashes = new ArrayList<>();

        try (FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
            int read;
            while ((read = fileInputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, read);
                byte[] hash = messageDigest.digest();
                hashes.add(hash);
            }
        }

        if (hashes.size() < nextPowerOf2(hashes.size())) {
            byte[] zeroes = new byte[32];
            int missingHashes = nextPowerOf2(hashes.size()) - hashes.size();
            for (int i = 0; i < missingHashes; i++) {
                hashes.add(zeroes);
            }
        }

        assert (hashes.size() == nextPowerOf2(hashes.size()));

        while (hashes.size() > 1) {
            List<byte[]> newHashes = new ArrayList<>(hashes.size() / 2);

            for (int i = 0; i < hashes.size() / 2; i++) {
                messageDigest.update(hashes.get((i * 2)));
                messageDigest.update(hashes.get((i * 2) + 1));
                newHashes.add(messageDigest.digest());
            }

            hashes = newHashes;
        }

        return hashes.get(0);
    }

    private static int nextPowerOf2(int number) {
        return number == 1 ? 1 : Integer.highestOneBit(number - 1) * 2;
    }

    @SneakyThrows
    @EventListener
    public void start(ApplicationReadyEvent ignored) {
        Files.createDirectories(torrentConfigurationProperties.getResumePath());
        Files.createDirectories(torrentConfigurationProperties.getDownloadPath());

        executorService.scheduleAtFixedRate(this::scanForNewFiles, 0, torrentConfigurationProperties.getFileScanInterval().getSeconds(), SECONDS);
    }

    @Transactional
    public void scanForNewFiles() {
        try {
            log.info("Scanning for new files");
            CollectingPathVisitor collectingPathVisitor = new CollectingPathVisitor();

            torrentConfigurationProperties.getContentPaths().forEach(path -> {
                try {
                    log.info("Scanning directory {}", path.toAbsolutePath());
                    Files.walkFileTree(path.toAbsolutePath(), collectingPathVisitor);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Set<String> paths = collectingPathVisitor.getPaths()
                    .stream()
                    .map(path -> path.toAbsolutePath().toString())
                    .collect(toSet());
            log.info("Found {} files", paths.size());

            // Add metadata
            Set<String> torrentPaths = paths.stream()
                    .filter(path -> FilenameUtils.isExtension(path, "torrent"))
                    .peek(metadataPath -> {
                        try {
                            log.info("Adding metadata {}", metadataPath);
                            java.io.File file = new java.io.File(metadataPath);
                            TorrentInfo torrentInfo = new TorrentInfo(file);
                            metadataService.addTorrent(torrentInfo);
                        } catch (Exception e) {
                            log.error("Exception while adding metadata {}", metadataPath, e);
                        }
                    })
                    .collect(toSet());
            // Remove metadata from future operations
            paths.removeAll(torrentPaths);

            Collection<File> files = fileRepository.findByPathIn(paths);

            Set<String> existingPaths = files.stream()
                    .map(File::getPath)
                    .collect(toSet());

            List<File> newFiles = paths.stream()
                    .filter(path -> !existingPaths.contains(path))
                    .map(path -> File.builder().path(path).build())
                    .toList();

            fileRepository.saveAllAndFlush(newFiles);

            executorService.submit(this::findTorrentFiles);

            log.info("Finished scanning for new files");
        } catch (Exception e) {
            log.error("Exception while scanning for new files", e);
        }
    }

    public void findTorrentFiles() {
        log.info("Finding matching torrent files");
        List<Long> fileIds = fileRepository.findAllIds();
        AtomicInteger count = new AtomicInteger(1);

        for (Long id : fileIds) {
            try {
                transactionTemplate.executeWithoutResult(transactionStatus -> {
                    File file = fileRepository.findById(id).orElseThrow();
                    Path path = Path.of(file.getPath()).toAbsolutePath();
                    log.info("({}/{}) Finding matching torrent files for {}", count.getAndIncrement(), fileIds.size(), path);
                    findTorrentFilesForFile(file);
                });
            } catch (Exception e) {
                log.error("Exception while finding matching torrent files", e);
            }
        }
    }

    @Transactional
    public void findTorrentFilesForFile(File file) {
        Path path = Path.of(file.getPath()).toAbsolutePath();

        if (!Files.exists(path)) {
            log.warn("File {} no longer exists, skipping", path);
            return;
        }

        if (file.getPiecesRoot() == null) {
            log.info("Calculating pieces root hash for {}", path);
            file.setPiecesRoot(getPiecesRoot(Path.of(file.getPath())));
            fileRepository.saveAndFlush(file);
        }

        log.info("Searching metadata for files matching {}", path);
        TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);
        file.setMatchingTorrentFiles(torrentFileResult.getMatchingTorrentFiles());
        file.setNonMatchingTorrentFiles(torrentFileResult.getNonMatchingTorrentFiles());
        log.info("Found {} matching files, and {} non-matching files", file.getMatchingTorrentFiles().size(), file.getNonMatchingTorrentFiles().size());
        fileRepository.saveAndFlush(file);
    }
}
