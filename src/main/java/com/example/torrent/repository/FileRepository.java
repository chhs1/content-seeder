package com.example.torrent.repository;

import com.example.torrent.model.entity.File;
import com.example.torrent.model.entity.TorrentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FileRepository extends JpaRepository<File, Long> {
    Set<File> findByPathIn(Set<String> paths);

    @Query(value = "SELECT id FROM File ORDER BY path")
    List<Long> findAllIds();

    Collection<File> findDistinctByMatchingTorrentFilesContains(TorrentFile torrentFile);
}
