# User Management Service

A production-ready Spring Boot microservice that handles user lifecycle management, built with **hexagonal architecture** (ports and adapters).

---

## Features

| Feature | Endpoint |
|---|---|
| User Registration | `POST /api/v1/users/register` |
| User Authentication (JWT) | `POST /api/v1/users/auth` |
| OTP Verification | `POST /api/v1/users/verify-otp` |
| Account Deletion (soft) | `DELETE /api/v1/users/{userId}` |
| Health Check | `GET /actuator/health` |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JJWT |
| Persistence | Spring Data JPA + PostgreSQL |
| Cache / OTP | Redis (Spring Data Redis) |
| Messaging | RabbitMQ (Spring AMQP) |
| Migrations | Flyway |
| Build | Maven |
| Runtime | Java 21 |

---

## Architecture

The service follows **hexagonal architecture** (ports and adapters):

```
src/main/java/com/usermanagement/
├── domain/                         # Core domain — no framework dependencies
│   ├── model/                      # Aggregates & value objects
│   └── exception/                  # Domain exceptions
├── application/
│   ├── port/
│   │   ├── in/                     # Inbound ports (use-case interfaces + commands)
│   │   └── out/                    # Outbound ports (repository / event interfaces)
│   └── service/                    # Use-case implementations
├── adapter/
│   ├── in/
│   │   └── web/                    # REST controllers + DTOs
│   └── out/
│       ├── persistence/            # JPA adapters (PostgreSQL)
│       ├── cache/                  # Redis adapter (OTP)
│       ├── messaging/              # RabbitMQ adapter
│       └── security/               # JWT token provider
└── infrastructure/
    └── config/                     # Spring configuration beans
```

---

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env and set strong values for DB_PASSWORD, JWT_SECRET, etc.
```

### 2. Start infrastructure

```bash
docker compose up -d postgres redis rabbitmq
```

### 3. Run the service

```bash
./mvnw spring-boot:run
```

Or with Docker:

```bash
docker build -t user-management-service .
docker run --env-file .env -p 8080:8080 user-management-service
```

---

## API Reference

### Register a user

```http
POST /api/v1/users/register
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "P@ssw0rd!",
  "firstName": "Alice",
  "lastName": "Smith"
}
```

**Response `201 Created`**
```json
{ "userId": "...", "email": "alice@example.com" }
```

---

### Verify OTP

```http
POST /api/v1/users/verify-otp
Content-Type: application/json

{ "email": "alice@example.com", "otp": "123456" }
```

**Response `204 No Content`**

---

### Authenticate

```http
POST /api/v1/users/auth
Content-Type: application/json

{ "email": "alice@example.com", "password": "P@ssw0rd!" }
```

**Response `200 OK`**
```json
{ "token": "<JWT>" }
```

---

### Delete account

```http
DELETE /api/v1/users/{userId}
Authorization: Bearer <JWT>
```

**Response `204 No Content`**

---

## Running Tests

```bash
# Unit tests only (no infrastructure required)
./mvnw test

# All tests including integration
./mvnw verify
```

---

## Environment Variables

See [`.env.example`](.env.example) for the full list of supported variables.

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `userdb` | Database name |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | — | DB password (**required**) |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `JWT_SECRET` | — | HS256 secret ≥ 32 chars (**required**) |
| `JWT_EXPIRATION_MS` | `3600000` | Token TTL in ms |

---

## Health Check

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

## License

Internal — All rights reserved.
