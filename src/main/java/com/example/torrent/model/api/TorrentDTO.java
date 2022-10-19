package com.example.torrent.model.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorrentDTO {
    private String name;
    private float progress;
    private TorrentStatusDTO status;
    private int seeds;
    private int peers;
    private int downloadRate;
    private int uploadRate;
    private long size;
}
