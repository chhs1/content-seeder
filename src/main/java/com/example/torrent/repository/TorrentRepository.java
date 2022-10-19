package com.example.torrent.repository;

import com.example.torrent.model.entity.Torrent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface TorrentRepository extends JpaRepository<Torrent, Long> {
    Page<Torrent> findByFiles_PiecesRoot(byte[] value, Pageable pageable);

    Collection<Torrent> findByFiles_PiecesRootIn(Collection<byte[]> piecesRoots);

    Optional<Torrent> findBySha1(byte[] sha1);

    Optional<Torrent> findBySha256(byte[] sha256);

    @Query(value = "FROM Torrent WHERE (sha1 = :sha1 AND sha1 IS NOT NULL) OR (sha256 = :sha256 AND sha256 IS NOT NULL)")
    Optional<Torrent> findBySha1OrSha256(@Param("sha1") byte[] sha1, @Param("sha256") byte[] sha256);

    Collection<Torrent> findByFiles_Length(long size);
}
