package com.nutricare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Generic wrapper untuk response pagination.
 * Digunakan oleh semua endpoint yang mengembalikan daftar data.
 */
@Getter
@Builder
public class PageResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
