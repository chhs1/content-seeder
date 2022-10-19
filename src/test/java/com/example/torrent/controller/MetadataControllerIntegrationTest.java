package com.example.torrent.controller;

import com.example.torrent.AbstractIntegrationTest;
import com.example.torrent.model.Hash;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.libtorrent4j.TorrentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static com.example.torrent.controller.MetadataController.APPLICATION_X_BITTORRENT;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

class MetadataControllerIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Value("classpath:data.txt.v2.torrent")
    private Resource metadata;

    @BeforeEach
    void setUpMockMvc() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @Test
    void uploadMetadata() throws IOException {
        given()
                .contentType(APPLICATION_X_BITTORRENT)
                .body(metadata.getFile())
                .post("/api/metadata")
                .then()
                .status(CREATED);

        TorrentInfo torrentInfo = new TorrentInfo(metadata.getFile());

        byte[] info = given()
                .get("/api/metadata/v2/{infoHash}", Hash.of(torrentInfo.infoHashes().swig().getV2()))
                .then()
                .status(OK)
                .contentType(APPLICATION_X_BITTORRENT)
                .extract()
                .asByteArray();

        assertThat(info).isEqualTo(torrentInfo.toEntry().dictionary().get("info").bencode());
    }
}