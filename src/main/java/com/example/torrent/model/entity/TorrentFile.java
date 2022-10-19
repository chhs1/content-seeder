package com.example.torrent.model.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(columnList = "torrent_id"),
        @Index(columnList = "piecesRoot")
})
public class TorrentFile {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Torrent torrent;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long length;

    @Column(length = 32)
    private byte[] piecesRoot;

    @Column(nullable = false)
    private Integer fileIndex;
}
