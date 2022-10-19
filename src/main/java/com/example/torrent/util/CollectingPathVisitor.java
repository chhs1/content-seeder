package com.example.torrent.util;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class CollectingPathVisitor extends SimpleFileVisitor<Path> {
    @Getter
    private final Set<Path> paths = new HashSet<>();

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        requireNonNull(path);
        paths.add(path);
        return FileVisitResult.CONTINUE;
    }
}
