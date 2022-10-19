package com.example.torrent.model.api;

public enum TorrentStatusDTO {
    CHECKING_FILES,
    DOWNLOADING_METADATA,
    DOWNLOADING,
    FINISHED,
    SEEDING,
    CHECKING_RESUME_DATA,
    UNKNOWN
}
