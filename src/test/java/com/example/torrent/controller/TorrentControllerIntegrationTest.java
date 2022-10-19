package com.example.torrent.controller;

import com.example.torrent.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

class TorrentControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Value("classpath:data.txt.v1.torrent")
    private Resource metadata;

    @Test
    void uploadMetadata() throws IOException {
        given()
                .webAppContextSetup(webApplicationContext)
                .body(metadata.getFile())
                .contentType("application/x-bittorrent")
                .post("/api/metadata");
    }
}