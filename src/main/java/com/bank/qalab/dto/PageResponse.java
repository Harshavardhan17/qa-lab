package com.bank.qalab.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Stable, self-documenting pagination envelope (nicer than the raw Spring Page JSON). */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isFirst(), p.isLast());
    }
}
