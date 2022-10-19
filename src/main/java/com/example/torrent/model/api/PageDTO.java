package com.example.torrent.model.api;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

public class PageDTO<T> extends PageImpl<T> {
    public PageDTO() {
        super(Collections.emptyList(), Pageable.unpaged(), 0);
    }
}
