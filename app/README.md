# User Management Service

A production-ready **User Management Service** built with **Spring Boot 3** following **Hexagonal Architecture** (Ports & Adapters). It provides user registration with OTP verification, JWT-based authentication, and event-driven messaging via RabbitMQ, with Redis for token caching.

---

## Architecture Overview

This service follows the **Hexagonal Architecture** (also known as Ports & Adapters) pattern, which cleanly separates the domain logic from infrastructure concerns.

```
com.usermanagement
  ├── domain                  ← Pure business logic, no framework dependencies
  │   ├── model               ← Domain entities: User, OtpToken
  │   ├── port
  │   │   ├── in              ← Inbound use-case interfaces (driving ports)
  │   │   └── out             ← Outbound interfaces: repos, messaging, cache (driven ports)
  │   └── service             ← Domain services implementing in-ports
  ├── application
  │   └── dto                 ← Request/Response DTOs
  ├── adapter
  │   ├── in
  │   │   └── web             ← REST controllers (driving adapters)
  │   └── out
  │       ├── persistence     ← JPA entities, Spring Data repos, adapters (driven)
  │       ├── messaging       ← RabbitMQ event publisher (driven)
  │       └── cache           ← Redis token cache adapter (driven)
  └── infrastructure
      ├── config              ← Spring Security, JWT, RabbitMQ configuration
      └── exception           ← Global exception handler
```

### Key Principles
- **Domain layer** has zero Spring/framework dependencies — only plain Java.
- **Ports** define contracts; **Adapters** implement them.
- **Dependency inversion**: domain depends on port interfaces, not on concrete adapters.
- Infrastructure wires everything together via Spring's DI.

---

## Technology Stack

| Technology          | Version   | Purpose                          |
|---------------------|-----------|----------------------------------|
| Java                | 17        | Language                         |
| Spring Boot         | 3.2.x     | Application framework            |
| Spring Security     | 6.x       | Authentication & authorization   |
| JJWT                | 0.11.5    | JWT token generation/validation  |
| Spring Data JPA     | 3.x       | ORM / persistence                |
| PostgreSQL          | 15        | Primary relational database      |
| Flyway              | bundled   | Database schema migrations       |
| Spring Data Redis   | 3.x       | Token caching                    |
| Spring AMQP         | 3.x       | RabbitMQ messaging               |
| Maven               | 3.x       | Build tool                       |

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose** (for running infrastructure)
- **PostgreSQL 15** (or use Docker)
- **Redis 7** (or use Docker)
- **RabbitMQ 3.12** (or use Docker)

---

## Getting Started

### Option 1: Full Docker Compose (Recommended)

Spin up the entire stack (app + all infrastructure) with a single command:

```bash
# Clone / navigate to project root
cd /app

# Copy environment file
cp .env.example .env

# Build and start all services
docker-compose up --build
```

The service will be available at `http://localhost:8080`.

RabbitMQ Management UI: `http://localhost:15672` (guest/guest)

### Option 2: Local Development (Infrastructure via Docker)

Start only the infrastructure services:

```bash
docker-compose up postgres redis rabbitmq -d
```

Then run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

Or build and run the JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/user-management-service-1.0.0.jar
```

---

## API Endpoints

| Method   | Endpoint                    | Auth Required | Description                          |
|----------|-----------------------------|---------------|--------------------------------------|
| `GET`    | `/api/v1/health`            | No            | Service health check                 |
| `POST`   | `/api/v1/users/register`    | No            | Register a new user                  |
| `POST`   | `/api/v1/users/login`       | No            | Authenticate and receive JWT token   |
| `POST`   | `/api/v1/users/verify`      | No            | Verify account with OTP              |
| `DELETE` | `/api/v1/users/{userId}`    | Yes (JWT)     | Delete a user account                |
| `GET`    | `/actuator/health`          | No            | Spring Actuator health endpoint      |

### Request / Response Examples

#### Register User
```http
POST /api/v1/users/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "securePass123",
  "fullName": "John Doe"
}
```
```json
HTTP 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "verified": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Login
```http
POST /api/v1/users/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "securePass123"
}
```
```json
HTTP 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

#### Verify OTP
```http
POST /api/v1/users/verify
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "otp": "123456"
}
```
```
HTTP 204 No Content
```

#### Delete User
```http
DELETE /api/v1/users/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
```
HTTP 204 No Content
```

---

## Environment Variables

| Variable             | Default                                          | Description                          |
|----------------------|--------------------------------------------------|--------------------------------------|
| `SERVER_PORT`        | `8080`                                           | HTTP server port                     |
| `DB_URL`             | `jdbc:postgresql://localhost:5432/userdb`        | PostgreSQL JDBC URL                  |
| `DB_USERNAME`        | `postgres`                                       | Database username                    |
| `DB_PASSWORD`        | `postgres`                                       | Database password                    |
| `REDIS_HOST`         | `localhost`                                      | Redis hostname                       |
| `REDIS_PORT`         | `6379`                                           | Redis port                           |
| `REDIS_PASSWORD`     | *(empty)*                                        | Redis password (optional)            |
| `RABBITMQ_HOST`      | `localhost`                                      | RabbitMQ hostname                    |
| `RABBITMQ_PORT`      | `5672`                                           | RabbitMQ AMQP port                   |
| `RABBITMQ_USERNAME`  | `guest`                                          | RabbitMQ username                    |
| `RABBITMQ_PASSWORD`  | `guest`                                          | RabbitMQ password                    |
| `JWT_SECRET`         | `changeme-super-secret-key-at-least-256-bits-long` | JWT signing secret (min 256-bit)   |
| `JWT_EXPIRATION_MS`  | `86400000`                                       | JWT expiry in milliseconds (24h)     |

> **Security Note**: Always override `JWT_SECRET` with a strong, randomly generated secret in production.

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage report
./mvnw verify

# Run a specific test class
./mvnw test -Dtest=UserRegistrationServiceTest
```

Tests use the `test` Spring profile (`application-test.yml`) which configures:
- **H2 in-memory database** (no PostgreSQL needed)
- **Flyway disabled** (schema created by Hibernate)
- Mocked Redis and RabbitMQ connections

---

## RabbitMQ Events

The service publishes the following events to the `user.exchange` topic exchange:

| Routing Key       | Queue                    | Payload                          | Trigger                  |
|-------------------|--------------------------|----------------------------------|--------------------------|
| `user.registered` | `user.registered.queue`  | `{ "email": "...", "otp": "..." }` | New user registration  |
| `user.deleted`    | `user.deleted.queue`     | `{ "email": "..." }`             | User account deletion    |

---

## Database Schema

Managed by **Flyway** migrations located in `src/main/resources/db/migration/`.

| Migration                        | Description                          |
|----------------------------------|--------------------------------------|
| `V1__create_users_table.sql`     | Creates `users` and `otp_tokens` tables |

---

## Project Structure

```
/app
├── src/
│   ├── main/
│   │   ├── java/com/usermanagement/
│   │   │   ├── UserManagementApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── model/          (User, OtpToken)
│   │   │   │   ├── port/in/        (use-case interfaces)
│   │   │   │   ├── port/out/       (repository/messaging interfaces)
│   │   │   │   └── service/        (domain service implementations)
│   │   │   ├── application/
│   │   │   │   └── dto/            (request/response DTOs)
│   │   │   ├── adapter/
│   │   │   │   ├── in/web/         (REST controllers)
│   │   │   │   └── out/
│   │   │   │       ├── persistence/ (JPA entities, adapters)
│   │   │   │       ├── messaging/   (RabbitMQ adapter)
│   │   │   │       └── cache/       (Redis adapter)
│   │   │   └── infrastructure/
│   │   │       ├── config/          (Security, JWT, RabbitMQ configs)
│   │   │       └── exception/       (Global exception handler)
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-test.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/usermanagement/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
└── pom.xml
```
