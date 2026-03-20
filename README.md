# Finance Tracker API

A REST API for tracking finances, built with Spring Boot and JdbcTemplate.

## Tech Stack

- **Java 21** + **Spring Boot 3.4**
- **JdbcTemplate** for data access (no JPA/Hibernate)
- **HikariCP** connection pool (auto-configured)
- **PostgreSQL** for production, **H2** in-memory for local dev
- **Flyway** for database migrations
- **Spring Security** with OAuth2/JWT (OIDC)
- **Jakarta Validation** for request validation
- **SpringDoc OpenAPI** for Swagger UI
- **Testcontainers** for integration tests
- **Docker** + **Docker Compose** for containerized deployment
- **GitHub Actions** CI pipeline

## Project Structure

```
src/main/java/com/financetracker/api/
├── Application.java              # Entry point + OpenAPI config
├── config/                       # Security, CORS, request logging + correlation ID
├── controller/                   # Versioned REST endpoints (/api/v1/...)
├── service/                      # Business logic with @Transactional
├── repository/                   # Data access (JdbcTemplate)
├── model/                        # Internal domain objects (POJOs with audit fields)
├── dto/
│   ├── request/                  # Incoming request bodies (Java records)
│   └── response/                 # Outgoing response bodies (Java records)
├── query/                        # SQL query constants (injection-safe)
├── mapper/                       # RowMappers + DTO converters
└── exception/                    # Custom exceptions + global handler
```

## Quick Start

```bash
# Clone the template
# Run locally with H2 (no external DB needed)
mvn spring-boot:run

# Or use Make
make run
```

The API starts at `http://localhost:8080`. Endpoints require a valid JWT token — see [Authentication](#authentication) below.

### Useful URLs (Local Dev)

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/api-docs` | OpenAPI JSON |
| `http://localhost:8080/h2-console` | H2 database console |
| `http://localhost:8080/actuator/health` | Health check |

> H2 console credentials: JDBC URL `jdbc:h2:mem:devdb`, User `sa`, no password.

## API Endpoints

All endpoints are versioned under `/api/v1/`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/examples` | Paginated list (query params: `page`, `size`, `sortBy`, `direction`) |
| `GET` | `/api/v1/examples/{id}` | Get by ID |
| `POST` | `/api/v1/examples` | Create new |
| `PUT` | `/api/v1/examples/{id}` | Full update (all fields required) |
| `PATCH` | `/api/v1/examples/{id}` | Partial update (only non-null fields applied) |
| `DELETE` | `/api/v1/examples/{id}` | Soft delete (sets `active=false`, record preserved) |

### Response Format

**Success** — returns the resource directly:
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Error** — typed `ErrorResponse` with correlation ID:
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Example with id 42 not found",
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Request Correlation

Every request gets an `X-Request-Id` header (generated or forwarded from the caller). This ID appears in:
- Response headers
- Log output
- Error response bodies

Use it to trace requests across services.

## Authentication

The template uses **OAuth2 Resource Server** with JWT validation. All `/api/**` endpoints require a valid Bearer token.

Configure your OIDC provider in `application.properties`:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://accounts.google.com
```

Public endpoints (no auth required): Swagger UI, OpenAPI docs, actuator, H2 console.

## Running with Docker

```bash
# Copy and configure environment variables
cp .env.example .env
# Edit .env with your values

# Start PostgreSQL + API
make docker-up

# Stop
make docker-down
```

## Running Tests

```bash
make test           # Unit tests only (no Docker needed)
make verify         # All tests including integration (requires Docker for Testcontainers)
```

## Adding a New Entity

For each new entity (e.g., `Product`), create these files using the `Example*` files as reference:

1. `model/ProductEntity.java` — POJO with audit fields (`createdAt`, `updatedAt`)
2. `dto/request/CreateProductRequest.java` — record with validation
3. `dto/request/UpdateProductRequest.java` — record (all fields required)
4. `dto/request/PatchProductRequest.java` — record (all fields optional)
5. `dto/response/ProductResponse.java` — record with timestamps
6. `query/ProductQueries.java` — SQL constants with safe pagination + soft delete
7. `mapper/ProductRowMapper.java` — `ResultSet` → entity
8. `mapper/ProductMapper.java` — DTO ↔ entity conversions
9. `repository/ProductRepository.java` — JdbcTemplate data access
10. `service/ProductService.java` — business logic with `@Transactional`
11. `controller/ProductController.java` — REST endpoints under `/api/v1/`
12. `db/migration/V2__Create_products_table.sql` — Flyway migration

## Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| **JdbcTemplate over JPA** | Full SQL control, no magic, easier to debug |
| **Java records for DTOs** | Immutable, concise, built-in equals/hashCode |
| **API versioning (`/v1/`)** | Breaking changes won't break existing clients |
| **Soft delete** | Data is preserved for audit/compliance, can be restored |
| **Audit timestamps** | Every table tracks `created_at` and `updated_at` |
| **Typed error responses** | Consistent, documented error contract across all endpoints |
| **Correlation IDs** | Enables distributed tracing across services |
| **`@Transactional`** | Prevents partial writes in multi-step operations |
| **SQL column whitelist** | ORDER BY injection protection without parameterized queries |

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `financetrackerdb` |
| `DB_USERNAME` | Database user | — |
| `DB_PASSWORD` | Database password | — |
| `OIDC_ISSUER_URI` | JWT issuer URI | `https://accounts.google.com` |
| `app.cors.allowed-origins` | Allowed CORS origins | `http://localhost:3000,http://localhost:5173` |

See `.env.example` for a full list.

### Profiles

- **`local`** (default): H2 in-memory database, seed data loaded, H2 console enabled, full actuator exposure
- **`prod`**: PostgreSQL, no seed data, H2 disabled, restricted actuator, connection pool tuning

```bash
# Local development (default, no flag needed)
mvn spring-boot:run

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Make Targets

```
make run          Start the app locally (H2)
make test         Run unit tests
make verify       Run all tests (needs Docker)
make build        Build JAR without tests
make docker-build Build Docker image
make docker-up    Start with Docker Compose
make docker-down  Stop Docker Compose
make clean        Remove build artifacts
```

## Request Flow

```
HTTP Request
    → RequestLoggingFilter (assigns X-Request-Id, starts timer)
    → Spring Security (validates JWT)
    → Controller (validates @RequestBody)
    → Service (@Transactional, business rules)
    → Repository (JdbcTemplate + SQL constants)
    → Database
    → Response (with X-Request-Id header)
```
