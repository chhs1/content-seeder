package com.example.torrent.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class FileSlice {
    private long offset;
    private long length;

    public static FileSlice fromFileSlice(org.libtorrent4j.FileSlice fileSlice) {
        return FileSlice.of(fileSlice.offset(), fileSlice.size());
    }
}
