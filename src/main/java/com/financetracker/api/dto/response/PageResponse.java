package com.financetracker.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response wrapper")
public record PageResponse<T>(
        @Schema(description = "Page content") List<T> content,
        @Schema(description = "Current page number (0-based)", example = "0") int page,
        @Schema(description = "Page size", example = "20") int size,
        @Schema(description = "Total number of elements", example = "100") long totalElements,
        @Schema(description = "Total number of pages", example = "5") int totalPages
) {
    public PageResponse(List<T> content, int page, int size, long totalElements) {
        this(content, page, size, totalElements,
                size > 0 ? (int) Math.ceil((double) totalElements / size) : 0);
    }
}
