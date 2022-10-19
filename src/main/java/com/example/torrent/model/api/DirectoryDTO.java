package com.example.torrent.model.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectoryDTO {
    private UUID id;
    private String path;
    private boolean enabled;
}
