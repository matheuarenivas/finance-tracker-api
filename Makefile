.PHONY: run test verify build docker-build docker-up docker-down clean

## run        — Start the app locally (H2, no Docker needed)
run:
	mvn spring-boot:run

## test       — Run unit tests
test:
	mvn test

## verify     — Run all tests including integration (needs Docker for Testcontainers)
verify:
	mvn clean verify

## build      — Build the JAR without running tests
build:
	mvn clean package -DskipTests

## docker-build — Build the Docker image
docker-build:
	docker build -t finance-tracker-api:latest .

## docker-up  — Start PostgreSQL + app with Docker Compose
docker-up:
	docker compose up --build -d

## docker-down — Stop Docker Compose services
docker-down:
	docker compose down

## clean      — Remove build artifacts
clean:
	mvn clean
