package com.example.torrent.service;

import com.example.torrent.util.FileHasher;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.nio.file.Path;

@Service
public class FileHasherFactory {
    public FileHasher getFileHasher(Path path) throws FileNotFoundException {
        return new FileHasher(path);
    }
}
