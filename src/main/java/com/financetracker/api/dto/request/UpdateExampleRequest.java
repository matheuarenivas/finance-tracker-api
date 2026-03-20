package com.financetracker.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for fully updating an example")
public record UpdateExampleRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        @Schema(description = "Name of the example", example = "Alice")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "Email address", example = "alice@example.com")
        String email,

        @NotNull(message = "Active status is required")
        @Schema(description = "Whether the example is active", example = "true")
        Boolean active
) {}
