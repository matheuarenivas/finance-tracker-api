package com.financetracker.api.mapper;

import com.financetracker.api.dto.request.CreateExampleRequest;
import com.financetracker.api.dto.request.PatchExampleRequest;
import com.financetracker.api.dto.request.UpdateExampleRequest;
import com.financetracker.api.dto.response.ExampleResponse;
import com.financetracker.api.model.ExampleEntity;

import java.time.Instant;

public final class ExampleMapper {

    private ExampleMapper() {}

    public static ExampleEntity toEntity(CreateExampleRequest request) {
        ExampleEntity entity = new ExampleEntity();
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setActive(true);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    public static ExampleEntity toEntity(UpdateExampleRequest request, Long id) {
        ExampleEntity entity = new ExampleEntity();
        entity.setId(id);
        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setActive(request.active());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    public static void applyPatch(PatchExampleRequest patch, ExampleEntity entity) {
        if (patch.name() != null) {
            entity.setName(patch.name());
        }
        if (patch.email() != null) {
            entity.setEmail(patch.email());
        }
        if (patch.active() != null) {
            entity.setActive(patch.active());
        }
        entity.setUpdatedAt(Instant.now());
    }

    public static ExampleResponse toResponse(ExampleEntity entity) {
        return new ExampleResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
