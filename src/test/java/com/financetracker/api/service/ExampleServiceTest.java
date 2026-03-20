package com.financetracker.api.service;

import com.financetracker.api.dto.request.CreateExampleRequest;
import com.financetracker.api.dto.request.PatchExampleRequest;
import com.financetracker.api.dto.request.UpdateExampleRequest;
import com.financetracker.api.dto.response.ExampleResponse;
import com.financetracker.api.dto.response.PageResponse;
import com.financetracker.api.exception.BadRequestException;
import com.financetracker.api.exception.ResourceNotFoundException;
import com.financetracker.api.model.ExampleEntity;
import com.financetracker.api.repository.ExampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

    @Mock
    private ExampleRepository repository;

    @InjectMocks
    private ExampleService service;

    private ExampleEntity sampleEntity;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        sampleEntity = new ExampleEntity(1L, "Alice", "alice@example.com", true, now, now);
    }

    @Nested
    class GetAllPaginated {

        @Test
        void shouldReturnPaginatedResults() {
            when(repository.count()).thenReturn(1L);
            when(repository.findAllPaginated("id", "ASC", 20, 0))
                    .thenReturn(List.of(sampleEntity));

            PageResponse<ExampleResponse> result = service.getAllPaginated(0, 20, "id", "ASC");

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().getFirst().name()).isEqualTo("Alice");
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);
        }

        @Test
        void shouldCalculateCorrectOffset() {
            when(repository.count()).thenReturn(50L);
            when(repository.findAllPaginated("id", "ASC", 10, 20))
                    .thenReturn(List.of());

            service.getAllPaginated(2, 10, "id", "ASC");

            verify(repository).findAllPaginated("id", "ASC", 10, 20);
        }

        @Test
        void shouldNormalizeDirectionToUpperCase() {
            when(repository.count()).thenReturn(0L);
            when(repository.findAllPaginated("id", "DESC", 20, 0))
                    .thenReturn(List.of());

            service.getAllPaginated(0, 20, "id", "desc");

            verify(repository).findAllPaginated("id", "DESC", 20, 0);
        }

        @Test
        void shouldThrowOnInvalidSortColumn() {
            assertThatThrownBy(() -> service.getAllPaginated(0, 20, "invalid", "ASC"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid sort column");
        }

        @Test
        void shouldThrowOnInvalidDirection() {
            assertThatThrownBy(() -> service.getAllPaginated(0, 20, "id", "SIDEWAYS"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid sort direction");
        }

        @Test
        void shouldThrowOnPageSizeTooLarge() {
            assertThatThrownBy(() -> service.getAllPaginated(0, 101, "id", "ASC"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Page size must be between 1 and 100");
        }

        @Test
        void shouldThrowOnPageSizeZero() {
            assertThatThrownBy(() -> service.getAllPaginated(0, 0, "id", "ASC"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Page size must be between 1 and 100");
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnExampleWhenFound() {
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

            ExampleResponse result = service.getById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Alice");
            assertThat(result.email()).isEqualTo("alice@example.com");
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    class Create {

        @Test
        void shouldCreateAndReturnExample() {
            CreateExampleRequest request = new CreateExampleRequest("Bob", "bob@example.com");
            when(repository.existsByEmail("bob@example.com")).thenReturn(false);
            when(repository.save(any(ExampleEntity.class))).thenReturn(2L);

            ExampleResponse result = service.create(request);

            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.name()).isEqualTo("Bob");
            assertThat(result.createdAt()).isNotNull();
            verify(repository).save(any(ExampleEntity.class));
        }

        @Test
        void shouldThrowOnDuplicateEmail() {
            CreateExampleRequest request = new CreateExampleRequest("Bob", "alice@example.com");
            when(repository.existsByEmail("alice@example.com")).thenReturn(true);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email already exists");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateAllFields() {
            UpdateExampleRequest request = new UpdateExampleRequest("Updated", "updated@example.com", false);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(repository.existsByEmailExcludingId("updated@example.com", 1L)).thenReturn(false);

            ExampleResponse result = service.update(1L, request);

            assertThat(result.name()).isEqualTo("Updated");
            assertThat(result.email()).isEqualTo("updated@example.com");
            assertThat(result.createdAt()).isEqualTo(sampleEntity.getCreatedAt());
            verify(repository).update(any(ExampleEntity.class));
        }

        @Test
        void shouldAllowKeepingSameEmail() {
            UpdateExampleRequest request = new UpdateExampleRequest("Updated", "alice@example.com", true);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

            ExampleResponse result = service.update(1L, request);

            assertThat(result.email()).isEqualTo("alice@example.com");
            verify(repository, never()).existsByEmailExcludingId(anyString(), anyLong());
        }

        @Test
        void shouldThrowOnDuplicateEmailForDifferentEntity() {
            UpdateExampleRequest request = new UpdateExampleRequest("Updated", "taken@example.com", true);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(repository.existsByEmailExcludingId("taken@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.update(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email already exists");
        }

        @Test
        void shouldThrowWhenNotFound() {
            UpdateExampleRequest request = new UpdateExampleRequest("Updated", "u@example.com", true);
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class Patch {

        @Test
        void shouldApplyOnlyProvidedFields() {
            PatchExampleRequest request = new PatchExampleRequest("Patched", null, null);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

            ExampleResponse result = service.patch(1L, request);

            assertThat(result.name()).isEqualTo("Patched");
            assertThat(result.email()).isEqualTo("alice@example.com");
            verify(repository).update(any(ExampleEntity.class));
        }

        @Test
        void shouldUpdateEmailWhenProvided() {
            PatchExampleRequest request = new PatchExampleRequest(null, "new@example.com", null);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(repository.existsByEmailExcludingId("new@example.com", 1L)).thenReturn(false);

            ExampleResponse result = service.patch(1L, request);

            assertThat(result.email()).isEqualTo("new@example.com");
        }

        @Test
        void shouldThrowOnDuplicateEmailInPatch() {
            PatchExampleRequest request = new PatchExampleRequest(null, "taken@example.com", null);
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(repository.existsByEmailExcludingId("taken@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.patch(1L, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email already exists");
        }

        @Test
        void shouldThrowWhenNotFound() {
            PatchExampleRequest request = new PatchExampleRequest("x", null, null);
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.patch(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DeleteById {

        @Test
        void shouldSoftDeleteWhenExists() {
            when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

            service.deleteById(1L);

            verify(repository).softDelete(1L);
        }

        @Test
        void shouldThrowWhenNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(repository, never()).softDelete(anyLong());
        }
    }
}
