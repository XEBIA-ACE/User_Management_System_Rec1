# User Management Service

A production-ready microservice for user lifecycle management, built with **Spring Boot 3** following **hexagonal architecture** (ports and adapters).

## Responsibilities

| Feature | Description |
|---|---|
| **Registration** | Create a new user account; triggers OTP email via RabbitMQ |
| **Authentication** | Validate credentials and issue a signed JWT |
| **Account Verification** | Activate an account using a 6-digit OTP code |
| **Account Deletion** | Soft-delete a user account (status → `DELETED`) |

---

## Technology Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt 0.12) |
| Persistence | PostgreSQL + Spring Data JPA + Flyway |
| Cache | Redis (Spring Data Redis) |
| Messaging | RabbitMQ (Spring AMQP) |
| Build | Maven 3 |
| Runtime | Java 21 |

---

## Architecture

```
src/main/java/com/usermanagement/
├── domain/
│   ├── model/          # Pure domain entities (User, OtpToken, UserStatus)
│   ├── port/
│   │   ├── in/         # Inbound ports (use-case interfaces)
│   │   └── out/        # Outbound ports (repository / messaging interfaces)
│   └── service/        # Domain services implementing inbound ports
├── infrastructure/
│   ├── persistence/    # JPA entities, repositories, adapters, mappers
│   ├── security/       # JWT adapter, filter, Spring Security config
│   └── messaging/      # RabbitMQ adapter and config
└── adapter/
    └── in/
        └── web/        # REST controllers, DTOs, exception handler
```

---

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose

### Run with Docker Compose

```bash
# 1. Copy environment variables
cp .env.example .env

# 2. Edit .env with your secrets
#    (at minimum, change JWT_SECRET and DB_PASSWORD)

# 3. Start infrastructure
docker compose up -d postgres redis rabbitmq

# 4. Run the service
./mvnw spring-boot:run
```

### Run with Docker

```bash
docker build -t user-management-service:latest .
docker run -p 8080:8080 --env-file .env user-management-service:latest
```

---

## API Reference

### Health Check

```
GET /actuator/health
```

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | None | Register a new user |
| `POST` | `/api/v1/auth/login` | None | Authenticate and receive JWT |
| `POST` | `/api/v1/auth/verify` | None | Verify account with OTP |

### User Management

| Method | Path | Auth | Description |
|---|---|---|---|
| `DELETE` | `/api/v1/users/me` | Bearer JWT | Soft-delete own account |

#### Register

```json
POST /api/v1/auth/register
{
  "email": "alice@example.com",
  "password": "securePassword123",
  "firstName": "Alice",
  "lastName": "Smith"
}
```

#### Login

```json
POST /api/v1/auth/login
{
  "email": "alice@example.com",
  "password": "securePassword123"
}
```

Response:
```json
{ "accessToken": "<jwt>" }
```

#### Verify Account

```json
POST /api/v1/auth/verify
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "otpCode": "123456"
}
```

---

## Configuration

All configuration is driven by environment variables. See [`.env.example`](.env.example) for the full list.

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:postgresql://localhost:5432/user_management` | JDBC URL |
| `DB_USERNAME` | `postgres` | DB username |
| `DB_PASSWORD` | `postgres` | DB password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `JWT_SECRET` | *(must set)* | HS256 signing key (≥32 chars) |
| `JWT_EXPIRATION_MS` | `3600000` | Token TTL in milliseconds |

---

## Running Tests

```bash
# Unit tests only (no infrastructure required)
./mvnw test

# Full test suite
./mvnw verify
```

---

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:

| Version | Description |
|---|---|
| `V1` | Create `users` table |
| `V2` | Create `otp_tokens` table |

---

## Security Notes

- Passwords are hashed with **BCrypt** (cost factor 10).
- JWTs are signed with **HMAC-SHA256**; the secret must be ≥ 32 characters.
- All endpoints except registration, login, verification, and health check require a valid Bearer token.
- Sessions are **stateless** — no server-side session storage.
