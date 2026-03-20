package com.financetracker.api.controller;

import com.financetracker.api.dto.request.CreateExampleRequest;
import com.financetracker.api.dto.request.PatchExampleRequest;
import com.financetracker.api.dto.request.UpdateExampleRequest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
class ExampleControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    private static Long createdExampleId;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    private void stubJwtDecoder() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("test-user")
                .issuer("https://accounts.google.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    private HttpHeaders authHeaders() {
        stubJwtDecoder();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("mock-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @Order(1)
    void getExamples_withoutToken_shouldReturn401() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/examples", String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    void createExample_withToken_shouldReturn201() {
        CreateExampleRequest request = new CreateExampleRequest("Integration Test", "integration@test.com");
        HttpEntity<CreateExampleRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples", HttpMethod.POST, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("id")).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Integration Test");
        assertThat(response.getBody().get("createdAt")).isNotNull();
        assertThat(response.getBody().get("updatedAt")).isNotNull();

        createdExampleId = ((Number) response.getBody().get("id")).longValue();
    }

    @Test
    @Order(3)
    void getExamples_withPagination_shouldReturn200() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples?page=0&size=10&sortBy=id&direction=ASC",
                HttpMethod.GET, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("content", "page", "size", "totalElements", "totalPages");
    }

    @Test
    @Order(4)
    void getExampleById_shouldReturn200() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples/" + createdExampleId,
                HttpMethod.GET, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Integration Test");
    }

    @Test
    @Order(5)
    void updateExample_shouldReturn200() {
        UpdateExampleRequest request = new UpdateExampleRequest("Updated Name", "updated@test.com", true);
        HttpEntity<UpdateExampleRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples/" + createdExampleId,
                HttpMethod.PUT, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Updated Name");
        assertThat(response.getBody().get("email")).isEqualTo("updated@test.com");
    }

    @Test
    @Order(6)
    void patchExample_shouldReturn200() {
        PatchExampleRequest request = new PatchExampleRequest("Patched Name", null, null);
        HttpEntity<PatchExampleRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples/" + createdExampleId,
                HttpMethod.PATCH, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("name")).isEqualTo("Patched Name");
        assertThat(response.getBody().get("email")).isEqualTo("updated@test.com");
    }

    @Test
    @Order(7)
    void deleteExample_shouldReturn204() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/examples/" + createdExampleId,
                HttpMethod.DELETE, entity, Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @Order(8)
    void getDeletedExample_shouldReturn404() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples/" + createdExampleId,
                HttpMethod.GET, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(9)
    void getExamples_withInvalidSortBy_shouldReturn400() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples?sortBy=invalid_column",
                HttpMethod.GET, entity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKeys("timestamp", "status", "error", "message", "requestId");
    }

    @Test
    @Order(10)
    void createExample_withDuplicateEmail_shouldReturn400() {
        CreateExampleRequest request = new CreateExampleRequest("First", "dup@test.com");
        HttpEntity<CreateExampleRequest> entity = new HttpEntity<>(request, authHeaders());
        restTemplate.exchange("/api/v1/examples", HttpMethod.POST, entity, Map.class);

        CreateExampleRequest duplicate = new CreateExampleRequest("Second", "dup@test.com");
        HttpEntity<CreateExampleRequest> dupEntity = new HttpEntity<>(duplicate, authHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples", HttpMethod.POST, dupEntity, Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(11)
    void response_shouldContainRequestIdHeader() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/v1/examples?page=0&size=10",
                HttpMethod.GET, entity, Map.class
        );

        assertThat(response.getHeaders().get("X-Request-Id")).isNotNull();
    }
}
