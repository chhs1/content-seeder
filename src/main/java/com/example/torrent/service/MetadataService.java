package com.example.torrent.service;

import com.example.torrent.model.FileSlice;
import com.example.torrent.model.Hash;
import com.example.torrent.model.TorrentFileResult;
import com.example.torrent.model.entity.File;
import com.example.torrent.model.entity.Torrent;
import com.example.torrent.model.entity.TorrentFile;
import com.example.torrent.repository.TorrentFileRepository;
import com.example.torrent.repository.TorrentRepository;
import com.example.torrent.util.FileHasher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.libtorrent4j.FileStorage;
import org.libtorrent4j.TorrentInfo;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataService {
    private final TorrentRepository torrentRepository;
    private final TorrentFileRepository torrentFileRepository;
    private final FileHasherFactory fileHasherFactory;
    private final TorrentService torrentService;

    private static String getBestHash(TorrentInfo torrentInfo) {
        if (torrentInfo.hasV2()) {
            return torrentInfo.swig().info_hashes().getV2().to_hex();
        } else if (torrentInfo.hasV1()) {
            return torrentInfo.swig().info_hashes().getV1().to_hex();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String getBestHash(TorrentFile torrentFile) {
        return getBestHash(torrentFile.getTorrent());
    }

    public static String getBestHash(Torrent torrent) {
        if (torrent.getSha256() != null) {
            return HexUtils.toHexString(torrent.getSha256());
        } else if (torrent.getSha1() != null) {
            return HexUtils.toHexString(torrent.getSha1());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Transactional
    public Torrent addTorrent(TorrentInfo torrentInfo) {

        byte[] info = torrentInfo.toEntry().dictionary().get("info").bencode();
        int pieceLength = torrentInfo.pieceLength();
        byte[] sha1 = torrentInfo.infoHashes().hasV1() ? Hash.of(torrentInfo.infoHashes().swig().getV1()).getData() : null;
        byte[] sha256 = torrentInfo.infoHashes().hasV2() ? Hash.of(torrentInfo.infoHashes().swig().getV2()).getData() : null;

        Optional<Torrent> existingTorrent = torrentRepository.findBySha1OrSha256(sha1, sha256);

        if (existingTorrent.isPresent()) {
            return existingTorrent.get();
        }

        Torrent torrent = Torrent.builder()
                .info(info)
                .pieceLength(pieceLength)
                .sha1(sha1)
                .sha256(sha256)
                .build();

        torrentRepository.saveAndFlush(torrent);

        FileStorage fileStorage = torrentInfo.origFiles();
        List<TorrentFile> torrentFileList = IntStream.range(0, fileStorage.numFiles())
                .mapToObj(fileIndex -> {
                    byte[] piecesRoot = fileStorage.hasV2() ? Hash.of(fileStorage.swig().root(fileIndex)).getData() : null;
                    return TorrentFile.builder()
                            .torrent(torrent)
                            .fileIndex(fileIndex)
                            .length(fileStorage.fileSize(fileIndex))
                            .name(fileStorage.fileName(fileIndex))
                            .piecesRoot(piecesRoot)
                            .build();
                })
                .toList();
        torrentFileRepository.saveAllAndFlush(torrentFileList);

        return torrent;
    }

    @SneakyThrows
    @Transactional
    public TorrentFileResult findTorrentFiles(File file) {
        checkNotNull(file.getPiecesRoot(), "file.piecesRoot must not be null");
        checkNotNull(file.getPath(), "file.path must not be null");

        Path path = Path.of(file.getPath());
        byte[] piecesRoot = file.getPiecesRoot();

        long size = Files.size(path);
        log.info("Ignoring {} torrent files that have already been checked", file.getMatchingTorrentFiles().size() + file.getNonMatchingTorrentFiles().size());
        Set<Long> torrentFileIds = Stream.concat(file.getNonMatchingTorrentFiles().stream(), file.getMatchingTorrentFiles().stream())
                .map(TorrentFile::getId)
                .collect(toSet());
        List<TorrentFile> torrentFiles = torrentFileRepository.findByPiecesRootOrLengthAndIdNotIn(piecesRoot, size, torrentFileIds);

        FileHasher fileHasher = fileHasherFactory.getFileHasher(path);

        List<TorrentFile> matchingTorrentFiles = new ArrayList<>(file.getMatchingTorrentFiles());
        List<TorrentFile> nonMatchingTorrentFiles = new ArrayList<>(file.getNonMatchingTorrentFiles());

        log.info("Found {} new potentially matching torrent files", torrentFiles.size());

        for (TorrentFile torrentFile : torrentFiles) {
            if (isTorrentFileMatch(file, torrentFile, fileHasher)) {
                matchingTorrentFiles.add(torrentFile);
            } else {
                nonMatchingTorrentFiles.add(torrentFile);
            }
        }

        return TorrentFileResult.builder()
                .matchingTorrentFiles(matchingTorrentFiles)
                .nonMatchingTorrentFiles(nonMatchingTorrentFiles)
                .build();
    }

    private boolean isTorrentFileMatch(File file, TorrentFile torrentFile, FileHasher fileHasher) {
        assert file.getPiecesRoot() != null;

        if (torrentFile.getPiecesRoot() != null && Arrays.equals(file.getPiecesRoot(), torrentFile.getPiecesRoot())) {
            log.info("Torrent {} file {} matched by pieces root", getBestHash(torrentFile), torrentFile.getFileIndex());
            return true;
        }

        TorrentInfo torrentInfo = torrentService.getTorrentInfo(torrentFile.getTorrent());

        if (!torrentInfo.hasV1()) {
            return false;
        }

        FileStorage fileStorage = torrentInfo.origFiles();
        Integer fileIndex = torrentFile.getFileIndex();
        List<Integer> pieces = IntStream.rangeClosed(fileStorage.pieceIndexAtFile(fileIndex), fileStorage.lastPieceIndexAtFile(fileIndex)).boxed().toList();
        int validPieces = 0;

        for (Integer pieceIndex : pieces) {
            List<FileSlice> fileSlices = fileStorage.mapBlock(pieceIndex, 0L, torrentInfo.pieceSize(pieceIndex))
                    .stream()
                    .map(FileSlice::fromFileSlice)
                    .toList();

            if (fileSlices.size() > 1) {
                // Piece covers multiple files, since we don't know what the other files are, we'll skip this
                continue;
            }

            byte[] pieceHash = Hash.of(torrentInfo.hashForPiece(pieceIndex)).getData();
            FileSlice fileSlice = fileSlices.stream().findFirst().orElseThrow();
            byte[] fileHash = fileHasher.getHash(fileSlice);
            boolean pieceValid = Arrays.equals(pieceHash, fileHash);

            if (!pieceValid) {
                log.info("Torrent {} file {} did not match on piece {}", getBestHash(torrentFile), fileIndex, pieceIndex);
                return false;
            }

            validPieces++;
        }

        if (validPieces > 0) {
            log.info("Torrent {} file {} matched {} pieces", getBestHash(torrentFile), fileIndex, validPieces);
        } else {
            log.info("Torrent {} file {} did not match", getBestHash(torrentFile), fileIndex);
        }

        return validPieces > 0;
    }
}
