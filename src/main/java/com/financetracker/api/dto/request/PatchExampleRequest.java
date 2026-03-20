package com.financetracker.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for partially updating an example. Only non-null fields are applied.")
public record PatchExampleRequest(

        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        @Schema(description = "Name of the example", example = "Alice")
        String name,

        @Email(message = "Email must be valid")
        @Schema(description = "Email address", example = "alice@example.com")
        String email,

        @Schema(description = "Whether the example is active", example = "true")
        Boolean active
) {}
