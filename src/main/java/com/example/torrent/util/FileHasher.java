package com.example.torrent.util;

import com.example.torrent.model.FileSlice;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileHasher implements AutoCloseable {
    private final Map<FileSlice, byte[]> hashes = new HashMap<>();
    private final RandomAccessFile randomAccessFile;

    public FileHasher(Path path) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(path.toFile(), "r");
    }

    @SneakyThrows
    public byte[] getHash(FileSlice fileSlice) {
        if (hashes.containsKey(fileSlice)) {
            return hashes.get(fileSlice);
        }

        byte[] buffer = new byte[(int) fileSlice.getLength()];
        randomAccessFile.seek(fileSlice.getOffset());
        randomAccessFile.read(buffer);

        byte[] hash = DigestUtils.sha1(buffer);
        hashes.put(fileSlice, hash);
        return hash;
    }

    @Override
    public void close() throws Exception {
        this.randomAccessFile.close();
    }
}
