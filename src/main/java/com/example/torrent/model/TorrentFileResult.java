package com.example.torrent.model;

import com.example.torrent.model.entity.TorrentFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TorrentFileResult {
    private List<TorrentFile> matchingTorrentFiles;
    private List<TorrentFile> nonMatchingTorrentFiles;
}
