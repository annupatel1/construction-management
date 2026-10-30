package com.construction.construction_management;

import com.construction.construction_management.util.Pages;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagesTest {

    @Test
    void clampsPageAndSize() {
        Pageable pageable = Pages.of(-3, 1000);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(Pages.MAX_SIZE, pageable.getPageSize());
    }

    @Test
    void defaultsToASafeMinimum() {
        Pageable pageable = Pages.of(2, 0);
        assertEquals(2, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
    }
}
