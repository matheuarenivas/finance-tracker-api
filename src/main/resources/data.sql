-- Seed data for local development
-- Uses MERGE INTO for idempotency (H2 syntax)
MERGE INTO examples (id, name, email, active, created_at, updated_at) KEY (email)
    VALUES (1, 'Alice', 'alice@example.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO examples (id, name, email, active, created_at, updated_at) KEY (email)
    VALUES (2, 'Bob', 'bob@example.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
MERGE INTO examples (id, name, email, active, created_at, updated_at) KEY (email)
    VALUES (3, 'Charlie', 'charlie@example.com', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Reset identity so new inserts don't collide with seeded IDs
ALTER TABLE examples ALTER COLUMN id RESTART WITH 100;
