# AGENTS.md — User Management Service

## 1. Stack

| Technology | Role |
|---|---|
| **Java 21** | Primary language (use virtual threads via `spring.threads.virtual.enabled=true`) |
| **Spring Boot 3.x** | Application framework, auto-configuration, embedded Tomcat |
| **Spring Security 6.x** | Authentication filter chain, JWT validation, method-level security |
| **Spring Data JPA / Hibernate** | ORM layer, repository pattern for PostgreSQL |
| **Spring Data Redis** | OTP storage, session caching, rate-limit counters |
| **Spring AMQP (RabbitMQ)** | Async event publishing (user-registered, account-deleted, otp-requested) |
| **PostgreSQL 16** | Primary relational datastore |
| **Redis 7** | TTL-backed OTP store, distributed caching |
| **RabbitMQ 3.13** | Message broker for domain events |
| **Flyway** | Database schema versioning and migrations |
| **JJWT (io.jsonwebtoken)** | JWT creation, signing (RS256), and validation |
| **MapStruct** | Compile-time DTO ↔ entity mapping |
| **Lombok** | Boilerplate reduction (builders, loggers, accessors) |
| **SpringDoc OpenAPI 3** | Auto-generated API documentation at `/swagger-ui.html` |
| **JUnit 5 + Mockito** | Unit and slice testing |
| **Testcontainers** | Integration tests with real PostgreSQL, Redis, RabbitMQ containers |
| **JaCoCo** | Code coverage enforcement (90% minimum) |
| **Docker / Docker Compose** | Local environment orchestration |
| **GitHub Actions** | CI pipeline |

---

## 2. Project Structure

```
user-management-service/
├── .github/
│   └── workflows/
│       └── ci.yml                        # CI pipeline definition
├── docker/
│   ├── Dockerfile                        # Multi-stage production image
│   └── docker-compose.yml               # Local dev stack (app + postgres + redis + rabbitmq)
├── src/
│   ├── main/
│   │   ├── java/com/company/usermanagement/
│   │   │   ├── UserManagementApplication.java      # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java             # Filter chain, CORS, CSRF, permit rules
│   │   │   │   ├── JwtConfig.java                  # RSA key loading, JwtParser/JwtBuilder beans
│   │   │   │   ├── RedisConfig.java                # RedisTemplate, key serializers, TTL defaults
│   │   │   │   ├── RabbitMqConfig.java             # Exchange, queue, binding bean definitions
│   │   │   │   └── FlywayConfig.java               # (if custom baseline needed)
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java                   # JPA entity — users table
│   │   │   │   │   ├── UserStatus.java             # Enum: PENDING, ACTIVE, DELETED
│   │   │   │   │   └── Role.java                   # Enum: USER, ADMIN
│   │   │   │   └── event/
│   │   │   │       ├── UserRegisteredEvent.java    # AMQP outbound event record
│   │   │   │       ├── OtpRequestedEvent.java      # AMQP outbound event record
│   │   │   │       └── AccountDeletedEvent.java    # AMQP outbound event record
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java             # JpaRepository<User, UUID>
│   │   │   ├── service/
│   │   │   │   ├── RegistrationService.java        # Orchestrates user creation + event publish
│   │   │   │   ├── AuthenticationService.java      # Credential validation, JWT issuance
│   │   │   │   ├── OtpService.java                 # OTP generation, Redis storage, verification
│   │   │   │   ├── AccountDeletionService.java     # Soft-delete + event publish
│   │   │   │   └── JwtService.java                 # Token build / parse / refresh logic
│   │   │   ├── messaging/
│   │   │   │   └── EventPublisher.java             # RabbitTemplate wrapper for all domain events
│   │   │   ├── web/
│   │   │   │   ├── controller/
│   │   │   │   │   ├── RegistrationController.java # POST /api/v1/users/register
│   │   │   │   │   ├── AuthController.java         # POST /api/v1/auth/login, /refresh, /logout
│   │   │   │   │   ├── OtpController.java          # POST /api/v1/auth/otp/send, /verify
│   │   │   │   │   └── AccountController.java      # DELETE /api/v1/users/{id}
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   ├── OtpVerifyRequest.java
│   │   │   │   │   │   └── DeleteAccountRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── AuthResponse.java
│   │   │   │   │       ├── UserResponse.java
│   │   │   │   │       └── MessageResponse.java
│   │   │   │   ├── mapper/
│   │   │   │   │   └── UserMapper.java             # MapStruct interface
│   │   │   │   └── filter/
│   │   │   │       └── JwtAuthenticationFilter.java # OncePerRequestFilter — validates Bearer token
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   │   │   │   ├── UserAlreadyExistsException.java
│   │   │   │   ├── InvalidOtpException.java
│   │   │   │   ├── AccountNotFoundException.java
│   │   │   │   └── JwtValidationException.java
│   │   │   └── util/
│   │   │       ├── OtpGenerator.java               # SecureRandom 6-digit OTP
│   │   │       └── SecurityUtils.java              # Extract principal from SecurityContext
│   │   └── resources/
│   │       ├── application.yml                     # Base configuration
│   │       ├── application-local.yml               # Local overrides (docker-compose URLs)
│   │       ├── application-test.yml                # Test profile (Testcontainers / H2 overrides)
│   │       ├── keys/
│   │       │   ├── private.pem                     # RSA private key (gitignored in prod)
│   │       │   └── public.pem                      # RSA public key
│   │       └── db/migration/
│   │           ├── V1__create_users_table.sql
│   │           └── V2__add_user_status_index.sql
│   └── test/
│       └── java/com/company/usermanagement/
│           ├── unit/
│           │   ├── service/
│           │   │   ├── RegistrationServiceTest.java
│           │   │   ├── AuthenticationServiceTest.java
│           │   │   ├── OtpServiceTest.java
│           │   │   ├── AccountDeletionServiceTest.java
│           │   │   └── JwtServiceTest.java
│           │   ├── web/
│           │   │   ├── RegistrationControllerTest.java   # @WebMvcTest slice
│           │   │   ├── AuthControllerTest.java
│           │   │   ├── OtpControllerTest.java
│           │   │   └── AccountControllerTest.java
│           │   └── util/
│           │       └── OtpGeneratorTest.java
│           ├── integration/
│           │   ├── RegistrationIntegrationTest.java      # Full HTTP → DB → RabbitMQ
│           │   ├── AuthenticationIntegrationTest.java
│           │   ├── OtpIntegrationTest.java
│           │   └── AccountDeletionIntegrationTest.java
│           └── config/
│               └── TestContainersConfig.java             # Shared @Container definitions
├── tasks.md                              # Agent-generated task checklist (see §3)
├── pom.xml                              # Maven build descriptor
├── .gitignore
├── .editorconfig
└── README.md
```

---

## 3. Required Workflow

The agent **must** follow these steps in order. Do not skip or reorder them.

### Step 1 — Read Specifications
- Read all story-level spec documents provided in the task context before writing any code.
- Identify all acceptance criteria, edge cases, and constraints.
- Note every endpoint, payload shape, and business rule explicitly stated.

### Step 2 — Create `tasks.md`
Create `tasks.md` in the repository root with the following structure:

```markdown
# Task Checklist — User Management Service

## Setup
- [ ] Initialise Maven project with correct group/artifact IDs
- [ ] Add all dependencies to pom.xml
- [ ] Configure application.yml profiles (base / local / test)
- [ ] Add Flyway migration scripts

## Domain
- [ ] Implement User entity and enums
- [ ] Implement domain event records

## Persistence
- [ ] Implement UserRepository

## Services
- [ ] RegistrationService
- [ ] AuthenticationService
- [ ] OtpService
- [ ] AccountDeletionService
- [ ] JwtService

## Messaging
- [ ] EventPublisher

## Web Layer
- [ ] Controllers (Registration, Auth, Otp, Account)
- [ ] DTOs (request + response)
- [ ] UserMapper (MapStruct)
- [ ] JwtAuthenticationFilter

## Exception Handling
- [ ] GlobalExceptionHandler + custom exceptions

## Tests
- [ ] Unit tests — all service classes
- [ ] Unit tests — all controllers (@WebMvcTest)
- [ ] Integration tests — all flows
- [ ] Verify JaCoCo coverage ≥ 90%

## Infrastructure
- [ ] Dockerfile
- [ ] docker-compose.yml
- [ ] CI pipeline (ci.yml)

## Validation
- [ ] All tests pass (`mvn verify`)
- [ ] Coverage report reviewed
- [ ] Application starts with `docker compose up`
- [ ] Swagger UI reachable at /swagger-ui.html
```

Check off each item (`[x]`) as it is completed. Never mark an item complete until the corresponding code compiles and tests pass.

### Step 3 — Implement

Follow this implementation order to respect dependency direction:

1. `pom.xml` — all dependencies before any source file
2. `application.yml` / profile YAMLs / `keys/`
3. Flyway SQL migrations
4. Domain models (`domain/model/`, `domain/event/`)
5. Repository layer
6. Config beans (`config/` package) — Security last, after JWT beans exist
7. Utility classes (`util/`)
8. Service layer (bottom-up: `JwtService` → `OtpService` → `AuthenticationService` → `RegistrationService` → `AccountDeletionService`)
9. Messaging (`EventPublisher`)
10. Web layer (DTOs → Mapper → Filter → Controllers)
11. Exception handler
12. Tests (unit first, then integration)
13. Docker / CI files

### Step 4 — Test

```bash
# Run all tests with coverage
mvn verify -Ptest

# View coverage report
open target/site/jacoco/index.html

# Run only unit tests
mvn test -Dtest="**/unit/**"

# Run only integration tests (requires Docker daemon)
mvn verify -Dtest="**/integration/**"
```

Ensure every test class compiles and every test method passes before proceeding to Step 5.

### Step 5 — Validate

```bash
# Full build gate
mvn clean verify

# Start local stack
docker compose -f docker/docker-compose.yml up --build -d

# Smoke test — registration
curl -s -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Str0ng!Pass","username":"testuser"}' | jq .

# Confirm Swagger UI
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/swagger-ui.html
# Expected: 200
```

Validation passes only when: build is green, coverage ≥ 90%, stack starts cleanly, and smoke tests return expected HTTP codes.

---

## 4. Coding Conventions

### Naming
| Artifact | Convention | Example |
|---|---|---|
| Packages | `com.company.usermanagement.<layer>` | `com.company.usermanagement.service` |
| Classes | PascalCase, suffix indicates role | `RegistrationService`, `AuthController` |
| Methods | camelCase, verb-first | `registerUser()`, `verifyOtp()` |
| Constants | SCREAMING_SNAKE_CASE | `OTP_TTL_SECONDS` |
| DB tables | snake_case, plural | `users`, `user_roles` |
| DB columns | snake_case | `created_at`, `is_deleted` |
| Redis keys | `<service>:<entity>:<id>:<purpose>` | `ums:otp:user123:verify` |
| RabbitMQ exchanges | `<domain>.events` (topic) | `user.events` |
| RabbitMQ queues | `<service>.<action>` | `user-management.user-registered` |
| Routing keys | `<domain>.<action>.<status>` | `user.account.registered` |

### Architecture Rules
- **Strict layering**: `web` → `service` → `repository`. No layer may skip or reverse this direction.
- **No JPA entities in controllers**: always map to/from DTOs via MapStruct before returning from a controller.
- **No business logic in controllers**: controllers validate input and delegate; all logic lives in services.
- **No `@Autowired` field injection**: use constructor injection only (Lombok `@RequiredArgsConstructor`).
- **Immutable DTOs**: all request/response records use Java `record` types or Lombok `@Value`.
- **Transactional boundaries**: `@Transactional` on service methods only, never on controllers or repositories.
- **Soft deletes**: set `UserStatus.DELETED` and `deleted_at` timestamp; never issue `DELETE` SQL for user rows.

### Spring Security
- Stateless session (`SessionCreationPolicy.STATELESS`).
- JWT signed with RS256 (asymmetric keys); private key never leaves the service.
- All endpoints require authentication except: `POST /api/v1/users/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/otp/send`.
- Use `@PreAuthorize("hasRole('ADMIN')")` for admin-only operations, not URL matchers.
- Passwords hashed with `BCryptPasswordEncoder(12)`.

### JWT
- Access token TTL: 15 minutes.
- Refresh token TTL: 7 days (stored as Redis key, invalidated on logout/deletion).
- Claims: `sub` (userId UUID), `email`, `roles`, `iat`, `exp`.
- Token issued only after OTP verification completes successfully.

### OTP
- 6-digit numeric code, generated via `SecureRandom`.
- Stored in Redis with key `