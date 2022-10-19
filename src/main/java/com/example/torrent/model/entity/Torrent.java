package com.example.torrent.model.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(columnList = "sha1"),
        @Index(columnList = "sha256"),
})
public class Torrent {
    @Id
    @GeneratedValue
    private Long id;

    // TODO Constraint sha1 == SHA1(info)
    // TODO Constraint sha256 == SHA256(info)
    // TODO has v1, has v2
    @Column(length = 20, unique = true)
    private byte[] sha1;

    @Column(length = 32, unique = true)
    private byte[] sha256;

    @ToString.Exclude
    @Column(nullable = false, columnDefinition = "BYTEA")
    private byte[] info;

    @Column(nullable = false)
    private int pieceLength;

    @OneToMany(mappedBy = "torrent")
    private List<TorrentFile> files;
}
