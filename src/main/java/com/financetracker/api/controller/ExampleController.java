package com.financetracker.api.controller;

import com.financetracker.api.dto.request.CreateExampleRequest;
import com.financetracker.api.dto.request.PatchExampleRequest;
import com.financetracker.api.dto.request.UpdateExampleRequest;
import com.financetracker.api.dto.response.ExampleResponse;
import com.financetracker.api.dto.response.PageResponse;
import com.financetracker.api.service.ExampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/examples")
@Tag(name = "Examples", description = "CRUD operations for examples")
public class ExampleController {

    private final ExampleService service;

    public ExampleController(ExampleService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all examples (paginated)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved examples")
    public PageResponse<ExampleResponse> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by column (id, name, email, active)", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String direction) {
        return service.getAllPaginated(page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get example by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the example")
    @ApiResponse(responseCode = "404", description = "Example not found")
    public ExampleResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new example")
    @ApiResponse(responseCode = "201", description = "Example created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<ExampleResponse> create(@Valid @RequestBody CreateExampleRequest request) {
        ExampleResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Fully update an example")
    @ApiResponse(responseCode = "200", description = "Example updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Example not found")
    public ExampleResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateExampleRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update an example")
    @ApiResponse(responseCode = "200", description = "Example patched successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Example not found")
    public ExampleResponse patch(@PathVariable Long id,
                                 @Valid @RequestBody PatchExampleRequest request) {
        return service.patch(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an example")
    @ApiResponse(responseCode = "204", description = "Example deleted successfully")
    @ApiResponse(responseCode = "404", description = "Example not found")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
