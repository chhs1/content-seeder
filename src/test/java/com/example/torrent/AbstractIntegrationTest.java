package com.example.torrent;

import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@Transactional
@SpringBootTest(webEnvironment = MOCK)
public class AbstractIntegrationTest {
}
