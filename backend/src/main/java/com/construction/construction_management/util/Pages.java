package com.construction.construction_management.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Keeps list endpoints bounded even when the client sends no pagination parameters. */
public final class Pages {

    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_SIZE = 100;

    private Pages() {
    }

    public static Pageable of(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
    }
}
