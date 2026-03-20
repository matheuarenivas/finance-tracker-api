package com.financetracker.api.service;

import com.financetracker.api.dto.request.CreateExampleRequest;
import com.financetracker.api.dto.request.PatchExampleRequest;
import com.financetracker.api.dto.request.UpdateExampleRequest;
import com.financetracker.api.dto.response.ExampleResponse;
import com.financetracker.api.dto.response.PageResponse;
import com.financetracker.api.exception.BadRequestException;
import com.financetracker.api.exception.ResourceNotFoundException;
import com.financetracker.api.mapper.ExampleMapper;
import com.financetracker.api.model.ExampleEntity;
import com.financetracker.api.query.ExampleQueries;
import com.financetracker.api.repository.ExampleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExampleService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ExampleRepository repository;

    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExampleResponse> getAllPaginated(int page, int size, String sortBy, String direction) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
        String normalizedDirection = direction.toUpperCase();

        if (!ExampleQueries.VALID_SORT_COLUMNS.contains(sortBy)) {
            throw new BadRequestException("Invalid sort column: " + sortBy
                    + ". Valid columns: " + ExampleQueries.VALID_SORT_COLUMNS);
        }
        if (!ExampleQueries.VALID_DIRECTIONS.contains(normalizedDirection)) {
            throw new BadRequestException("Invalid sort direction: " + direction
                    + ". Valid directions: " + ExampleQueries.VALID_DIRECTIONS);
        }

        long totalElements = repository.count();
        int offset = page * size;

        List<ExampleResponse> content = repository
                .findAllPaginated(sortBy, normalizedDirection, size, offset)
                .stream()
                .map(ExampleMapper::toResponse)
                .toList();

        return new PageResponse<>(content, page, size, totalElements);
    }

    @Transactional(readOnly = true)
    public ExampleResponse getById(Long id) {
        ExampleEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Example", id));
        return ExampleMapper.toResponse(entity);
    }

    @Transactional
    public ExampleResponse create(CreateExampleRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists: " + request.email());
        }

        ExampleEntity entity = ExampleMapper.toEntity(request);
        long id = repository.save(entity);
        entity.setId(id);

        return ExampleMapper.toResponse(entity);
    }

    @Transactional
    public ExampleResponse update(Long id, UpdateExampleRequest request) {
        ExampleEntity existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Example", id));

        if (!existing.getEmail().equals(request.email())
                && repository.existsByEmailExcludingId(request.email(), id)) {
            throw new BadRequestException("Email already exists: " + request.email());
        }

        ExampleEntity entity = ExampleMapper.toEntity(request, id);
        entity.setCreatedAt(existing.getCreatedAt());
        repository.update(entity);

        return ExampleMapper.toResponse(entity);
    }

    @Transactional
    public ExampleResponse patch(Long id, PatchExampleRequest request) {
        ExampleEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Example", id));

        if (request.email() != null && !entity.getEmail().equals(request.email())
                && repository.existsByEmailExcludingId(request.email(), id)) {
            throw new BadRequestException("Email already exists: " + request.email());
        }

        ExampleMapper.applyPatch(request, entity);
        repository.update(entity);

        return ExampleMapper.toResponse(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Example", id));
        repository.softDelete(id);
    }
}
