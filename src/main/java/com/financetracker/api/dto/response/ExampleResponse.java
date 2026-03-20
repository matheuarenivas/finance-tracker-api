package com.financetracker.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response body representing an example")
public record ExampleResponse(
        @Schema(description = "Unique identifier", example = "1") Long id,
        @Schema(description = "Name of the example", example = "Alice") String name,
        @Schema(description = "Email address", example = "alice@example.com") String email,
        @Schema(description = "When the record was created") Instant createdAt,
        @Schema(description = "When the record was last updated") Instant updatedAt
) {}
