package com.example.torrent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.Collection;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String path;

    @Column
    private byte[] piecesRoot;

    @Column
    private OffsetDateTime lastModifiedAt;

    @ManyToMany
    @JoinTable
    private Collection<TorrentFile> matchingTorrentFiles;

    @ManyToMany
    @JoinTable
    private Collection<TorrentFile> nonMatchingTorrentFiles;
}
