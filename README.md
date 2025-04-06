# Instant Payment API

A Spring Boot application that enables money transfers between accounts, storing transactions in a PostgreSQL database
and publishing transaction notifications to Kafka.

## Features

- REST Endpoint: POST /api/payments to process instant payments.

- Database: PostgreSQL, tracked with Flyway for schema migrations.

- Messaging: Kafka for asynchronous notifications.

- Containerization: Docker Compose setups for both a full production-like stack and local development.

---

## 1. Quick Start (Fully Containerized)

Use this approach when you want to spin up the app + Kafka + Postgres together in Docker.

1. Build the JAR (if not done already):

   `mvn clean package`

2. Run Docker Compose from the root directory (where docker-compose.yml is located):

   `docker compose up -d --build`

3. Check logs (optional):

   `docker compose logs -f`

4. Access the App:

- The service is exposed on http://localhost:8080.

- For example, http://localhost:8080/api/payments.

This will start:

- A Postgres container on port 5432
- A Kafka container on port 9092
- Your Spring Boot container on port 8080

---

## 2. Local Development Setup

If you prefer to run the Spring Boot app directly on your machine (e.g., through IntelliJ), but still want to use
Docker for Kafka and Postgres, follow these steps:

1. Go to the local/ directory and run its docker-compose.yml:
    ```
    cd local
    docker compose up -d
    ```
   This spins up Postgres (on localhost:5432) and Kafka (on localhost:9092) for local dev.


2. Run the Spring Boot app from your IDE or via Maven:

   `mvn spring-boot:run`


3. Test by hitting http://localhost:8080/api/payments or using the included `requests.http` (see below).

---

## 3. Testing the API (requests.http)

Inside `src/main/resources/requests.http`, you’ll find example HTTP requests for IntelliJ’s built-in HTTP client (or any
similar tool). You can:

1. Open requests.http in IntelliJ.

2. Click “Run” on each request to send it to the running Spring Boot instance.

---

## 4. Additional Notes

- Flyway Migrations: Run automatically on startup to create/update the schema.

- Kafka Topics: For local dev, topic auto-creation is enabled (transaction-notifications topic is created
  automatically).

- OpenAPI Spec: See openapi.yml in the repo for a manual specification of the POST /api/payments endpoint.



