package com.financetracker.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard error response")
public record ErrorResponse(
        @Schema(description = "ISO-8601 timestamp", example = "2024-01-15T10:30:00Z") Instant timestamp,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "HTTP status reason", example = "Not Found") String error,
        @Schema(description = "Error detail message", example = "Example with id 42 not found") String message,
        @Schema(description = "Request correlation ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") String requestId
) {}
