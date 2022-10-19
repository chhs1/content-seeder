package com.example.torrent.service;

import com.example.torrent.model.FileSlice;
import com.example.torrent.model.TorrentFileResult;
import com.example.torrent.model.entity.File;
import com.example.torrent.model.entity.Torrent;
import com.example.torrent.model.entity.TorrentFile;
import com.example.torrent.repository.TorrentFileRepository;
import com.example.torrent.util.FileHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.libtorrent4j.FileStorage;
import org.libtorrent4j.Sha1Hash;
import org.libtorrent4j.TorrentInfo;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.torrent.RandomUtils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceUnitTest {
    @InjectMocks
    private MetadataService metadataService;
    @Mock
    private TorrentFileRepository torrentFileRepository;
    @Mock
    private FileHasherFactory fileHasherFactory;
    @Mock
    private TorrentService torrentService;

    @Test
    @DisplayName("A single file v1 torrent consisting of a single piece exactly matches a file")
    void findTorrentFiles_singleFileV1TorrentExactMatch() throws FileNotFoundException {
        long size = randomLong();
        File file = File.builder()
                .piecesRoot(randomBytes(32))
                .path(randomAlphanumeric(32))
                .nonMatchingTorrentFiles(emptyList())
                .matchingTorrentFiles(emptyList())
                .build();
        Torrent torrent = Torrent.builder()
                .pieceLength(16384)
                .sha1(randomBytes(20))
                .build();
        TorrentFile torrentFile = TorrentFile.builder()
                .torrent(torrent)
                .fileIndex(randomInteger())
                .length(size)
                .build();
        when(torrentFileRepository.findByPiecesRootOrLengthAndIdNotIn(file.getPiecesRoot(), size, emptySet()))
                .thenReturn(List.of(torrentFile));

        FileStorage fileStorage = mockFileStorage(Map.of(0, List.of(FileSlice.of(0, torrent.getPieceLength()))));

        FileHasher fileHasher = mock(FileHasher.class);
        when(fileHasherFactory.getFileHasher(Path.of(file.getPath()))).thenReturn(fileHasher);

        byte[] fileSliceHash = randomBytes(20);
        when(fileHasher.getHash(FileSlice.of(0, torrent.getPieceLength()))).thenReturn(fileSliceHash);

        TorrentInfo torrentInfo = mock(TorrentInfo.class);
        when(torrentInfo.hasV1()).thenReturn(true);
        when(torrentInfo.origFiles()).thenReturn(fileStorage);
        when(torrentInfo.hashForPiece(0)).thenReturn(Sha1Hash.fromBytes(fileSliceHash));
        when(torrentInfo.pieceSize(0)).thenReturn(torrent.getPieceLength());
        when(torrentService.getTorrentInfo(torrent)).thenReturn(torrentInfo);

        try (var mockedStaticFiles = mockStatic(Files.class)) {
            mockedStaticFiles.when(() -> Files.size(Path.of(file.getPath()))).thenReturn(size);
            TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);

            assertThat(torrentFileResult.getMatchingTorrentFiles()).containsExactly(torrentFile);
            assertThat(torrentFileResult.getNonMatchingTorrentFiles()).isEmpty();
        }
    }

    @Test
    @DisplayName("A single file v1 torrent consisting of a single piece does not match a file")
    void findTorrentFiles_singleFileV1TorrentNotExactMatch() throws FileNotFoundException {
        long size = randomLong();
        File file = File.builder()
                .piecesRoot(randomBytes(32))
                .path(randomAlphanumeric(32))
                .nonMatchingTorrentFiles(emptyList())
                .matchingTorrentFiles(emptyList())
                .build();
        Torrent torrent = Torrent.builder()
                .pieceLength(16384)
                .sha1(randomBytes(20))
                .build();
        TorrentFile torrentFile = TorrentFile.builder()
                .torrent(torrent)
                .fileIndex(randomInteger())
                .length(size)
                .build();
        when(torrentFileRepository.findByPiecesRootOrLengthAndIdNotIn(file.getPiecesRoot(), size, emptySet()))
                .thenReturn(List.of(torrentFile));

        FileStorage fileStorage = mockFileStorage(Map.of(0, List.of(FileSlice.of(0, torrent.getPieceLength()))));

        FileHasher fileHasher = mock(FileHasher.class);
        when(fileHasherFactory.getFileHasher(Path.of(file.getPath()))).thenReturn(fileHasher);

        byte[] fileSliceHash = randomBytes(20);
        when(fileHasher.getHash(FileSlice.of(0, torrent.getPieceLength()))).thenReturn(fileSliceHash);

        TorrentInfo torrentInfo = mock(TorrentInfo.class);
        when(torrentInfo.hasV1()).thenReturn(true);
        when(torrentInfo.origFiles()).thenReturn(fileStorage);
        when(torrentInfo.hashForPiece(0)).thenReturn(Sha1Hash.fromBytes(randomBytes(20)));
        when(torrentInfo.pieceSize(0)).thenReturn(torrent.getPieceLength());
        when(torrentService.getTorrentInfo(torrent)).thenReturn(torrentInfo);

        try (var mockedStaticFiles = mockStatic(Files.class)) {
            mockedStaticFiles.when(() -> Files.size(Path.of(file.getPath()))).thenReturn(size);
            TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);

            assertThat(torrentFileResult.getNonMatchingTorrentFiles()).containsExactly(torrentFile);
            assertThat(torrentFileResult.getMatchingTorrentFiles()).isEmpty();
        }
    }

    @Test
    @DisplayName("A single file v2 torrent exactly matches a file")
    void findTorrentFiles_singleFileV2TorrentExactMatch() throws FileNotFoundException {
        long size = randomLong();
        File file = File.builder()
                .piecesRoot(randomBytes(32))
                .path(randomAlphanumeric(32))
                .nonMatchingTorrentFiles(emptyList())
                .matchingTorrentFiles(emptyList())
                .build();
        Torrent torrent = Torrent.builder()
                .pieceLength(16384)
                .sha256(randomBytes(32))
                .build();
        TorrentFile torrentFile = TorrentFile.builder()
                .torrent(torrent)
                .fileIndex(randomInteger())
                .length(size)
                .piecesRoot(file.getPiecesRoot())
                .build();
        when(torrentFileRepository.findByPiecesRootOrLengthAndIdNotIn(file.getPiecesRoot(), size, emptySet()))
                .thenReturn(List.of(torrentFile));

        FileHasher fileHasher = mock(FileHasher.class);
        when(fileHasherFactory.getFileHasher(Path.of(file.getPath()))).thenReturn(fileHasher);

        try (var mockedStaticFiles = mockStatic(Files.class)) {
            mockedStaticFiles.when(() -> Files.size(Path.of(file.getPath()))).thenReturn(size);
            TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);

            assertThat(torrentFileResult.getMatchingTorrentFiles()).containsExactly(torrentFile);
            assertThat(torrentFileResult.getNonMatchingTorrentFiles()).isEmpty();
        }
    }

    @Test
    @DisplayName("A single file v2 torrent does not match a file")
    void findTorrentFiles_singleFileV2TorrentDoesNotMatch() throws FileNotFoundException {
        long size = randomLong();
        File file = File.builder()
                .piecesRoot(randomBytes(32))
                .path(randomAlphanumeric(32))
                .nonMatchingTorrentFiles(emptyList())
                .matchingTorrentFiles(emptyList())
                .build();
        Torrent torrent = Torrent.builder()
                .pieceLength(16384)
                .sha256(randomBytes(32))
                .build();
        TorrentFile torrentFile = TorrentFile.builder()
                .torrent(torrent)
                .fileIndex(randomInteger())
                .length(size)
                .piecesRoot(randomBytes(32))
                .build();
        when(torrentFileRepository.findByPiecesRootOrLengthAndIdNotIn(file.getPiecesRoot(), size, emptySet()))
                .thenReturn(List.of(torrentFile));

        FileHasher fileHasher = mock(FileHasher.class);
        when(fileHasherFactory.getFileHasher(Path.of(file.getPath()))).thenReturn(fileHasher);

        TorrentInfo torrentInfo = mock(TorrentInfo.class);
        when(torrentService.getTorrentInfo(torrent)).thenReturn(torrentInfo);

        try (var mockedStaticFiles = mockStatic(Files.class)) {
            mockedStaticFiles.when(() -> Files.size(Path.of(file.getPath()))).thenReturn(size);
            TorrentFileResult torrentFileResult = metadataService.findTorrentFiles(file);

            assertThat(torrentFileResult.getNonMatchingTorrentFiles()).containsExactly(torrentFile);
            assertThat(torrentFileResult.getMatchingTorrentFiles()).isEmpty();
        }
    }

    private FileStorage mockFileStorage(Map<Integer, List<FileSlice>> pieceFileSlices) {
        FileStorage fileStorage = mock(FileStorage.class);

        pieceFileSlices.forEach((pieceIndex, fileSlices) -> {
            var fileSliceArrayList = fileSlices.stream()
                    .map(fileSlice -> {
                        org.libtorrent4j.FileSlice slice = mock(org.libtorrent4j.FileSlice.class);
                        when(slice.size()).thenReturn(fileSlice.getLength());
                        when(slice.offset()).thenReturn(fileSlice.getOffset());
                        return slice;
                    }).collect(Collectors.toCollection(ArrayList::new));
            when(fileStorage.mapBlock(eq(pieceIndex), anyLong(), anyInt())).thenReturn(fileSliceArrayList);
        });

        return fileStorage;
    }
}