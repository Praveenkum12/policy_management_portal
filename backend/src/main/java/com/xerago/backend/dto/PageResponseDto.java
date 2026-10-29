package com.xerago.backend.dto;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int page,
        int limit,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<String> sort) {
}
