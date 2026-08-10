# AGENTS.md — User Management Service

## 1. Stack

| Technology | Role |
|---|---|
| **Java 21** | Primary language (use virtual threads via Project Loom where applicable) |
| **Spring Boot 3.x** | Application framework, auto-configuration, embedded Tomcat |
| **Spring Security 6.x** | Authentication filter chain, JWT validation, method-level security |
| **Spring Data JPA / Hibernate** | ORM layer, repository abstractions over PostgreSQL |
| **Spring Data Redis** | OTP storage, session caching, rate-limit counters |
| **Spring AMQP (RabbitMQ)** | Async event publishing (user-registered, account-deleted events) |
| **PostgreSQL 16** | Primary relational datastore |
| **Redis 7** | Ephemeral OTP store with TTL, distributed cache |
| **RabbitMQ 3.13** | Message broker for domain events |
| **Flyway** | Database schema versioning and migrations |
| **JJWT (io.jsonwebtoken)** | JWT creation, signing (RS256), and validation |
| **MapStruct** | Compile-time DTO ↔ entity mapping |
| **Lombok** | Boilerplate reduction (builders, getters, loggers) |
| **SpringDoc OpenAPI 3** | Auto-generated API documentation |
| **JUnit 5 + Mockito** | Unit and slice testing |
| **Testcontainers** | Integration tests with real PostgreSQL, Redis, RabbitMQ containers |
| **JaCoCo** | Code coverage enforcement (90% minimum) |
| **Docker / Docker Compose** | Containerisation and local dev orchestration |
| **GitHub Actions** | CI pipeline |

---

## 2. Project Structure

```
user-management-service/
├── AGENTS.md                          # This file
├── tasks.md                           # Agent-generated task checklist (created before coding)
├── README.md                          # Human-readable service overview
├── pom.xml                            # Maven build descriptor
├── .env.example                       # Environment variable template (never commit .env)
├── .gitignore
├── docker/
│   ├── Dockerfile                     # Multi-stage production image
│   └── docker-compose.yml             # Local dev stack (app + postgres + redis + rabbitmq)
├── .github/
│   └── workflows/
│       └── ci.yml                     # GitHub Actions pipeline
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/company/usermanagement/
│   │   │       ├── UserManagementApplication.java   # @SpringBootApplication entry point
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java           # Filter chain, CORS, CSRF, endpoint rules
│   │   │       │   ├── JwtConfig.java                # RSA key loading, JwtParser/JwtBuilder beans
│   │   │       │   ├── RedisConfig.java              # RedisTemplate, key serialisers
│   │   │       │   ├── RabbitMQConfig.java           # Exchange, queue, binding declarations
│   │   │       │   └── OpenApiConfig.java            # SpringDoc metadata and security scheme
│   │   │       ├── domain/
│   │   │       │   ├── model/
│   │   │       │   │   ├── User.java                 # @Entity — users table
│   │   │       │   │   ├── UserStatus.java           # Enum: PENDING, ACTIVE, DELETED
│   │   │       │   │   └── Role.java                 # Enum: USER, ADMIN
│   │   │       │   └── event/
│   │   │       │       ├── UserRegisteredEvent.java  # AMQP outbound event record
│   │   │       │       └── UserDeletedEvent.java     # AMQP outbound event record
│   │   │       ├── repository/
│   │   │       │   └── UserRepository.java           # JpaRepository + custom queries
│   │   │       ├── service/
│   │   │       │   ├── UserRegistrationService.java  # Registration orchestration
│   │   │       │   ├── AuthenticationService.java    # Login, JWT issuance
│   │   │       │   ├── OtpService.java               # OTP generation, Redis storage, verification
│   │   │       │   ├── AccountVerificationService.java # Ties OTP to user activation
│   │   │       │   ├── AccountDeletionService.java   # Soft-delete + event publish
│   │   │       │   └── JwtService.java               # Token build/parse/validate helpers
│   │   │       ├── messaging/
│   │   │       │   └── EventPublisher.java           # RabbitTemplate wrapper for domain events
│   │   │       ├── web/
│   │   │       │   ├── controller/
│   │   │       │   │   ├── AuthController.java       # POST /auth/register, /auth/login, /auth/verify
│   │   │       │   │   └── AccountController.java    # DELETE /accounts/{id}
│   │   │       │   ├── dto/
│   │   │       │   │   ├── request/
│   │   │       │   │   │   ├── RegisterRequest.java
│   │   │       │   │   │   ├── LoginRequest.java
│   │   │       │   │   │   └── VerifyOtpRequest.java
│   │   │       │   │   └── response/
│   │   │       │   │       ├── AuthResponse.java
│   │   │       │   │       ├── UserResponse.java
│   │   │       │   │       └── ApiErrorResponse.java
│   │   │       │   └── mapper/
│   │   │       │       └── UserMapper.java           # MapStruct interface
│   │   │       ├── security/
│   │   │       │   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter — extracts/validates JWT
│   │   │       │   ├── UserDetailsServiceImpl.java   # Loads UserDetails from DB
│   │   │       │   └── SecurityUser.java             # UserDetails wrapper
│   │   │       └── exception/
│   │   │           ├── GlobalExceptionHandler.java   # @RestControllerAdvice
│   │   │           ├── UserAlreadyExistsException.java
│   │   │           ├── InvalidOtpException.java
│   │   │           ├── UserNotFoundException.java
│   │   │           └── TokenValidationException.java
│   │   └── resources/
│   │       ├── application.yml                       # Base configuration (references env vars)
│   │       ├── application-local.yml                 # Local overrides
│   │       ├── application-test.yml                  # Test-specific overrides
│   │       ├── keys/
│   │       │   ├── private.pem                       # RS256 private key (gitignored)
│   │       │   └── public.pem                        # RS256 public key
│   │       └── db/migration/
│   │           ├── V1__create_users_table.sql
│   │           └── V2__add_users_indexes.sql
│   └── test/
│       └── java/
│           └── com/company/usermanagement/
│               ├── unit/
│               │   ├── service/
│               │   │   ├── UserRegistrationServiceTest.java
│               │   │   ├── AuthenticationServiceTest.java
│               │   │   ├── OtpServiceTest.java
│               │   │   ├── AccountVerificationServiceTest.java
│               │   │   ├── AccountDeletionServiceTest.java
│               │   │   └── JwtServiceTest.java
│               │   └── web/
│               │       ├── AuthControllerTest.java   # @WebMvcTest slice
│               │       └── AccountControllerTest.java
│               ├── integration/
│               │   ├── BaseIntegrationTest.java      # Testcontainers base class
│               │   ├── UserRegistrationIT.java
│               │   ├── AuthenticationIT.java
│               │   ├── OtpVerificationIT.java
│               │   └── AccountDeletionIT.java
│               └── fixture/
│                   └── UserFixture.java              # Shared test data builders
```

---

## 3. Required Workflow

The agent **must** follow these steps in order. Do not skip or reorder steps.

### Step 1 — Read and Internalise Specifications
- Read every story-level spec file provided in the repository before writing any code.
- Identify all acceptance criteria, edge cases, and constraints.
- Note all external system interactions (PostgreSQL, Redis, RabbitMQ).

### Step 2 — Create `tasks.md`
- Create `tasks.md` in the repository root before writing any production code.
- Break the work into atomic, checkable tasks grouped by layer (config → domain → repository → service → web → security → exception handling → tests → Docker → CI).
- Use GitHub-flavoured Markdown checkboxes: `- [ ] Task description`.
- Each task must map to a single class or migration file.
- Example structure:
  ```markdown
  ## Tasks
  ### Infrastructure & Config
  - [ ] Create pom.xml with all required dependencies
  - [ ] Configure application.yml with env-var placeholders
  - [ ] Write V1__create_users_table.sql Flyway migration
  ...
  ### Domain
  - [ ] Implement User entity
  ...
  ### Tests
  - [ ] Write UserRegistrationServiceTest (unit)
  ...
  ```

### Step 3 — Implement in Layer Order
Implement in this strict sequence to avoid forward-dependency errors:

1. `pom.xml` and `application.yml` / `application-test.yml`
2. Flyway migrations (`V1__`, `V2__`)
3. Domain models and enums
4. Repository interfaces
5. Config classes (`SecurityConfig`, `JwtConfig`, `RedisConfig`, `RabbitMQConfig`)
6. Security classes (`JwtService`, `JwtAuthenticationFilter`, `UserDetailsServiceImpl`)
7. Service classes (registration → authentication → OTP → verification → deletion)
8. Messaging (`EventPublisher`)
9. MapStruct mappers
10. DTOs (request and response records)
11. Controllers
12. Exception handler and custom exceptions
13. `OpenApiConfig`
14. Dockerfile and `docker-compose.yml`
15. CI workflow (`ci.yml`)

### Step 4 — Write Tests Alongside Each Layer
- Write the unit test for every service class immediately after implementing that class.
- Write `@WebMvcTest` slice tests immediately after implementing each controller.
- Write integration tests after all layers are complete.
- Mark each `tasks.md` checkbox as complete (`- [x]`) after the test for that component passes.

### Step 5 — Validate
- Run `./mvnw verify` — all tests must pass.
- Run `./mvnw jacoco:report` — confirm line coverage ≥ 90% for `src/main`.
- Run `docker compose -f docker/docker-compose.yml up --build` — service must start healthy.
- Confirm Flyway migrations run without errors on startup.
- Confirm Swagger UI is reachable at `http://localhost:8080/swagger-ui.html`.
- Check all `tasks.md` items are marked `- [x]` before declaring done.

---

## 4. Coding Conventions

### General
- Java 21; enable virtual threads: `spring.threads.virtual.enabled=true` in `application.yml`.
- All classes are `final` unless designed for extension.
- No field injection (`@Autowired` on fields). Use constructor injection exclusively. Lombok `@RequiredArgsConstructor` is acceptable.
- Never suppress warnings without an inline comment explaining why.
- All public API surface must have Javadoc.

### Naming
| Artifact | Convention | Example |
|---|---|---|
| Entity | `PascalCase`, no suffix | `User` |
| Repository | `<Entity>Repository` | `UserRepository` |
| Service | `<Concern>Service` | `OtpService` |
| Controller | `<Resource>Controller` | `AuthController` |
| DTO (request) | `<Action>Request` (Java record) | `RegisterRequest` |
| DTO (response) | `<Resource>Response` (Java record) | `AuthResponse` |
| Event | `<Entity><Action>Event` (Java record) | `UserRegisteredEvent` |
| Mapper | `<Entity>Mapper` | `UserMapper` |
| Exception | descriptive + `Exception` suffix | `InvalidOtpException` |
| Migration | `V{n}__{snake_case_description}.sql` | `V1__create_users_table.sql` |
| Constants | `UPPER_SNAKE_CASE` in a dedicated `*Constants` class | `OtpConstants.OTP_TTL_SECONDS` |

### Architecture Patterns
- **Layered architecture**: web → service → repository. Services must not call controllers. Repositories must not contain business logic.
- **DTOs never leak into the service layer.** Services accept and return domain objects or primitives. Controllers perform DTO ↔ domain conversion via MapStruct mappers.
- **Soft deletes only.** Set `UserStatus.DELETED` and `deletedAt` timestamp; never issue `DELETE` SQL for user rows.
- **OTP flow:** Generate a 6-digit numeric OTP → store in Redis with key `otp:{userId}` and TTL of 5 minutes → verify and delete atomically using a Lua script or `GETDEL`.
- **JWT:** Use RS256 (asymmetric). Private key signs tokens; public key validates. Load keys from `classpath:keys/` via `JwtConfig`. Access token TTL: 15 minutes. Refresh token is out of scope.
- **RabbitMQ exchanges:** Use a topic exchange named `user.events`. Routing keys: `user.registered`, `user.deleted`.
- **Passwords:** Hash with `BCryptPasswordEncoder` (strength 12). Never log or return passwords.
- **Validation:** Use Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`) on all request DTOs. Validate at the controller boundary only.
- **Error responses:** All errors return `ApiErrorResponse` with fields: `timestamp`, `status`, `error`, `message`, `path`.
- **Logging:** Use SLF4J + Logback. Log at `INFO` for service operations, `WARN` for business rule violations, `ERROR` for unexpected exceptions. Never log PII (email, password, OTP value).

### Spring Security
- Disable CSRF (stateless JWT API).
- Permit: `POST /auth/register`, `POST /auth/login`, `POST /auth/verify`, `GET /actuator/health`, `GET /v3/api-docs/**`, `GET /swagger-ui/**`.
- Require authentication for all other endpoints.
- Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.

---

## 5. Testing

### Unit Tests (`src/test/java/.../unit/`)
- Framework: JUnit 5 + Mockito.
- Annotate with `@ExtendWith(MockitoExtension.class)`.
- Mock all collaborators. No Spring context loaded.
- Every service method must have tests covering: happy path, each validation branch, each exception path.
- Controller slice tests use `@WebMvcTest` + `@MockBean` for service dependencies. Test HTTP status codes, response body shape, and validation rejection (400) for invalid inputs.
- Use `UserFixture` to build consistent test data. No magic literals in tests — use named constants.

### Integration Tests (`src/test/java/.../integration/`)
- Extend `BaseIntegrationTest` which starts Testcontainers for PostgreSQL, Redis, and RabbitMQ.
- Annotate with `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
- Use `@ActiveProfiles("test")` and `application-test.yml` for config overrides.
- Use `TestRestTemplate` or `WebTestClient` for HTTP calls.
- Each