package com.example.torrent.repository;

import com.example.torrent.model.entity.TorrentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TorrentFileRepository extends JpaRepository<TorrentFile, UUID> {
    @Query("FROM TorrentFile WHERE (piecesRoot = :piecesRoot OR length = :length) AND id NOT IN :torrentFileIds")
    List<TorrentFile> findByPiecesRootOrLengthAndIdNotIn(@Param("piecesRoot") byte[] piecesRoot, @Param("length") long length, @Param("torrentFileIds") Collection<Long> torrentFileIds);
}
