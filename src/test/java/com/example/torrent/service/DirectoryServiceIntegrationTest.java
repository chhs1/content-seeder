package com.example.torrent.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(webEnvironment = MOCK)
class DirectoryServiceIntegrationTest {
    @Autowired
    private DirectoryService directoryService;

    @Test
    public void scan() {
        directoryService.scanForNewFiles();
        directoryService.findTorrentFiles();
    }
}