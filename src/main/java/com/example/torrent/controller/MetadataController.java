package com.example.torrent.controller;

import com.example.torrent.model.entity.Torrent;
import com.example.torrent.repository.TorrentRepository;
import com.example.torrent.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.libtorrent4j.TorrentInfo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
@Transactional
public class MetadataController {
    public static final String APPLICATION_X_BITTORRENT = "application/x-bittorrent";
    private final TorrentRepository torrentMetadataRepository;
    private final MetadataService metadataService;

    @ResponseStatus(CREATED)
    @PostMapping(path = "/api/metadata", consumes = APPLICATION_X_BITTORRENT)
    public void uploadMetadata(@RequestBody byte[] metadata) {
        try {
            TorrentInfo torrentInfo = new TorrentInfo(metadata);
            metadataService.addTorrent(torrentInfo);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(FOUND, "", e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "", e);
        }
    }

    @GetMapping(path = "/api/metadata/v1/{infoHashHex}", produces = APPLICATION_X_BITTORRENT)
    public byte[] getV1Torrent(@PathVariable("infoHashHex") String infoHash) {
        byte[] sha1 = HexUtils.fromHexString(infoHash);
        return torrentMetadataRepository.findBySha1(sha1)
                .map(Torrent::getInfo)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @GetMapping(path = "/api/metadata/v2/{infoHashHex}", produces = APPLICATION_X_BITTORRENT)
    public byte[] getV2Torrent(@PathVariable("infoHashHex") String infoHash) {
        byte[] sha256 = HexUtils.fromHexString(infoHash);
        return torrentMetadataRepository.findBySha256(sha256)
                .map(Torrent::getInfo)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @GetMapping(path = "/api/metadata/search/{piecesRootHex}", produces = APPLICATION_JSON_VALUE)
    public Page<Torrent> searchPiecesRoot(@PathVariable("piecesRootHex") String piecesRootHex, Pageable pageable) {
        byte[] piecesRoot = HexUtils.fromHexString(piecesRootHex);
        return torrentMetadataRepository.findByFiles_PiecesRoot(piecesRoot, pageable);
    }
}
