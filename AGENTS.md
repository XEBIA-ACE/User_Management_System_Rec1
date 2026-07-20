# AGENTS.md — User Management Service

## 1. Stack

| Technology | Role |
|---|---|
| **Java 21** | Primary language (use virtual threads where applicable) |
| **Spring Boot 3.x** | Application framework, auto-configuration, embedded Tomcat |
| **Spring Security 6.x** | Authentication filter chain, password encoding, method-level security |
| **Spring Data JPA / Hibernate** | ORM layer, repository abstraction over PostgreSQL |
| **Spring Data Redis** | OTP storage, token blacklisting, session caching |
| **Spring AMQP (RabbitMQ)** | Async event publishing (registration events, OTP dispatch) |
| **PostgreSQL 16** | Primary relational data store |
| **Redis 7** | Ephemeral key-value store for OTPs and JWT denylist |
| **Flyway** | Database schema versioning and migrations |
| **JJWT (io.jsonwebtoken)** | JWT creation, signing, and validation |
| **MapStruct** | Compile-time DTO ↔ entity mapping |
| **Lombok** | Boilerplate reduction (getters, builders, constructors) |
| **SpringDoc OpenAPI 3** | Auto-generated API documentation |
| **JUnit 5 + Mockito** | Unit and slice testing |
| **Testcontainers** | Integration tests against real PostgreSQL, Redis, RabbitMQ |
| **JaCoCo** | Code coverage enforcement |
| **Docker / Docker Compose** | Local environment orchestration |
| **GitHub Actions** | CI pipeline |

---

## 2. Project Structure

```
user-management-service/
├── .github/
│   └── workflows/
│       └── ci.yml                        # GitHub Actions CI pipeline
├── docker/
│   ├── Dockerfile                        # Multi-stage production image
│   └── docker-compose.yml               # Local dev stack (app + PG + Redis + RabbitMQ)
├── src/
│   ├── main/
│   │   ├── java/com/company/usermgmt/
│   │   │   ├── UserManagementApplication.java   # @SpringBootApplication entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java           # SecurityFilterChain, CORS, CSRF, BCrypt bean
│   │   │   │   ├── JwtConfig.java                # JWT secret, expiry properties binding
│   │   │   │   ├── RedisConfig.java              # RedisTemplate, serializers
│   │   │   │   ├── RabbitMQConfig.java           # Exchange, queue, binding declarations
│   │   │   │   └── OpenApiConfig.java            # SpringDoc bearer-auth configuration
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java                 # @Entity: id, email, passwordHash, status, roles, timestamps
│   │   │   │   │   ├── UserStatus.java           # Enum: PENDING, ACTIVE, DELETED
│   │   │   │   │   └── Role.java                 # Enum: ROLE_USER, ROLE_ADMIN
│   │   │   │   └── repository/
│   │   │   │       └── UserRepository.java       # JpaRepository + custom finders
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── RegisterRequest.java      # email, password, fullName (Bean Validation)
│   │   │   │   │   ├── LoginRequest.java         # email, password
│   │   │   │   │   ├── VerifyOtpRequest.java     # email, otp
│   │   │   │   │   └── DeleteAccountRequest.java # password confirmation
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java         # accessToken, refreshToken, expiresIn
│   │   │   │       └── UserResponse.java         # id, email, fullName, status
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java               # MapStruct: User ↔ UserResponse
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java         # generate, validate, parse JWT; check denylist
│   │   │   │   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter extracting Bearer token
│   │   │   │   └── UserDetailsServiceImpl.java   # Loads UserDetails from UserRepository
│   │   │   ├── service/
│   │   │   │   ├── UserService.java              # Interface defining all use-case contracts
│   │   │   │   ├── impl/
│   │   │   │   │   ├── UserServiceImpl.java      # Registration, deletion orchestration
│   │   │   │   │   ├── AuthServiceImpl.java      # Login, token refresh, logout
│   │   │   │   │   └── OtpServiceImpl.java       # OTP generation, Redis storage, verification
│   │   │   │   └── event/
│   │   │   │       ├── UserEventPublisher.java   # Publishes domain events to RabbitMQ
│   │   │   │       └── UserRegisteredEvent.java  # Event payload POJO
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java           # POST /api/v1/auth/register, /login, /logout
│   │   │   │   ├── OtpController.java            # POST /api/v1/auth/verify, /resend
│   │   │   │   └── UserController.java           # GET /api/v1/users/me, DELETE /api/v1/users/me
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java   # @RestControllerAdvice, RFC 7807 ProblemDetail
│   │   │       ├── UserAlreadyExistsException.java
│   │   │       ├── InvalidOtpException.java
│   │   │       ├── AccountNotVerifiedException.java
│   │   │       └── UserNotFoundException.java
│   │   └── resources/
│   │       ├── application.yml                   # Base config (profiles, server port, logging)
│   │       ├── application-local.yml             # Local overrides (datasource, redis, rabbit URLs)
│   │       ├── application-test.yml              # Test profile (Testcontainers auto-config)
│   │       └── db/migration/
│   │           ├── V1__create_users_table.sql
│   │           └── V2__add_user_roles_index.sql
│   └── test/
│       └── java/com/company/usermgmt/
│           ├── unit/
│           │   ├── service/
│           │   │   ├── UserServiceImplTest.java
│           │   │   ├── AuthServiceImplTest.java
│           │   │   └── OtpServiceImplTest.java
│           │   ├── security/
│           │   │   └── JwtTokenProviderTest.java
│           │   └── mapper/
│           │       └── UserMapperTest.java
│           ├── integration/
│           │   ├── controller/
│           │   │   ├── AuthControllerIT.java     # @SpringBootTest + Testcontainers
│           │   │   ├── OtpControllerIT.java
│           │   │   └── UserControllerIT.java
│           │   └── repository/
│           │       └── UserRepositoryIT.java     # @DataJpaTest + Testcontainers PG
│           └── BaseIntegrationTest.java          # Shared Testcontainers lifecycle, MockMvc setup
├── tasks.md                                      # Agent-generated task breakdown (see §3)
├── pom.xml                                       # Maven build descriptor
├── .env.example                                  # Template for local secrets
├── .gitignore
└── README.md
```

---

## 3. Required Workflow

The agent **must** follow these steps in order. Do not skip or reorder them.

### Step 1 — Read Specifications
- Parse all story-level specification documents provided in context.
- Identify every endpoint, business rule, data contract, error scenario, and non-functional requirement.
- Note any ambiguities; surface them as `TODO` comments in `tasks.md`.

### Step 2 — Create `tasks.md`
Create `tasks.md` in the project root before writing any source code. Use this exact structure:

```markdown
# tasks.md

## Status Legend
- [ ] Not started  |  [~] In progress  |  [x] Done

## Tasks
### Infrastructure
- [ ] TASK-001: Scaffold Maven project with all dependencies in pom.xml
- [ ] TASK-002: Write docker-compose.yml (PostgreSQL, Redis, RabbitMQ)
- [ ] TASK-003: Write Flyway migration V1 (users table)

### Domain & Persistence
- [ ] TASK-004: Implement User entity and UserStatus/Role enums
- [ ] TASK-005: Implement UserRepository with custom finders

### Security
- [ ] TASK-006: Implement JwtTokenProvider (generate, validate, denylist check)
- [ ] TASK-007: Implement JwtAuthenticationFilter
- [ ] TASK-008: Configure SecurityFilterChain in SecurityConfig

### Services
- [ ] TASK-009: Implement UserServiceImpl (registration, deletion)
- [ ] TASK-010: Implement AuthServiceImpl (login, logout, refresh)
- [ ] TASK-011: Implement OtpServiceImpl (generate, store in Redis, verify)
- [ ] TASK-012: Implement UserEventPublisher (RabbitMQ)

### Controllers & DTOs
- [ ] TASK-013: Implement DTOs with Bean Validation annotations
- [ ] TASK-014: Implement AuthController, OtpController, UserController
- [ ] TASK-015: Implement GlobalExceptionHandler (RFC 7807)

### Testing
- [ ] TASK-016: Write unit tests (target ≥ 90% coverage)
- [ ] TASK-017: Write integration tests with Testcontainers
- [ ] TASK-018: Verify JaCoCo report passes threshold gate

### CI/CD
- [ ] TASK-019: Write GitHub Actions ci.yml
- [ ] TASK-020: Validate Docker multi-stage build succeeds
```

Update task status (`[~]` / `[x]`) as each task is completed.

### Step 3 — Implement
- Implement tasks in the order listed in `tasks.md`.
- Mark each task `[~]` when started, `[x]` when complete.
- Commit logically: one commit per completed task group (infrastructure → domain → security → services → controllers → tests → CI).
- Never leave a compilation error before moving to the next task.

### Step 4 — Test
- Run `./mvnw test` after completing every service-layer task.
- Run `./mvnw verify` (triggers integration tests + JaCoCo) before marking the testing tasks done.
- Fix all failures before proceeding.

### Step 5 — Validate
- Confirm JaCoCo line coverage ≥ 90% (build must fail below threshold).
- Confirm `./mvnw spring-boot:run -Dspring-boot.run.profiles=local` starts without errors.
- Confirm `docker compose -f docker/docker-compose.yml up --build` completes successfully.
- Confirm SpringDoc UI is reachable at `http://localhost:8080/swagger-ui.html`.

---

## 4. Coding Conventions

### Naming
| Artefact | Convention | Example |
|---|---|---|
| Classes | `PascalCase` | `OtpServiceImpl` |
| Methods / variables | `camelCase` | `generateOtp()` |
| Constants | `UPPER_SNAKE_CASE` | `JWT_EXPIRY_MS` |
| REST endpoints | `kebab-case` nouns, versioned | `/api/v1/auth/verify-otp` |
| DB tables / columns | `snake_case` | `users`, `password_hash` |
| Flyway scripts | `V{n}__{description}.sql` | `V1__create_users_table.sql` |
| RabbitMQ exchange | `{service}.{entity}.exchange` | `usermgmt.user.exchange` |
| RabbitMQ queue | `{service}.{event}.queue` | `usermgmt.user.registered.queue` |
| Redis key | `{namespace}:{entity}:{id}` | `otp:user:user@example.com` |

### Architecture Patterns
- **Layered architecture**: Controller → Service Interface → Service Impl → Repository. Controllers must never call repositories directly.
- **DTOs at the boundary**: Entities must never be returned from controllers. Always map through MapStruct.
- **Dependency injection**: Constructor injection only — no `@Autowired` on fields.
- **Immutability**: All DTOs must use `@Builder` and have no public setters. Use Java records for response-only DTOs where no validation is needed.
- **Transactions**: `@Transactional` lives on service implementations, never on controllers or repositories.
- **Exception handling**: Throw domain-specific unchecked exceptions from services; catch and map them exclusively in `GlobalExceptionHandler`.
- **Password encoding**: Always use `BCryptPasswordEncoder` (strength 12). Plaintext passwords must never be logged or stored.
- **OTP lifecycle**: OTPs are 6-digit numeric codes, stored in Redis with a 10-minute TTL, deleted immediately after successful verification.
- **JWT denylist**: On logout, store the token JTI in Redis with TTL equal to the token's remaining validity.
- **Soft delete**: Deletion sets `UserStatus = DELETED` and anonymises PII fields; no hard deletes.

### Style
- Max method length: 30 lines. Extract helpers if exceeded.
- Max class length: 250 lines. Split responsibilities if exceeded.
- All public methods on service interfaces must have Javadoc.
- No `System.out.println`; use SLF4J `@Slf4j` only.
- Validate all inbound DTOs with `@Valid` + Bean Validation 3.x annotations; add custom `@Constraint` validators where standard annotations are insufficient.

---

## 5. Testing

### Unit Tests (`src/test/java/.../unit/`)
- Framework: **JUnit 5** + **Mockito** (no Spring context loaded).
- Annotate with `@ExtendWith(MockitoExtension.class)`.
- Mock all collaborators; test one class in isolation.
- Cover: happy path, all exception branches, edge cases (null inputs, boundary values).
- `JwtTokenProviderTest` must test: valid token, expired token, tampered signature, denylisted JTI.
- `OtpServiceImplTest` must test: OTP generation range, Redis TTL set correctly, successful verification, expired OTP, wrong OTP.

### Integration Tests (`src/test/java/.../integration/`)
- Annotate with `@SpringBootTest(webEnvironment = RANDOM_PORT)`.
- Extend `BaseIntegrationTest` which starts Testcontainers singletons:
  - `PostgreSQLContainer` (image: `postgres:16-alpine`)
  - `GenericContainer` for Redis (`redis:7-alpine`)
  - `RabbitMQContainer` (image: `rabbitmq:3-management-alpine`)
- Use `@AutoConfigureMockMvc` + `MockMvc` for HTTP-layer tests.
- Each IT class must reset database state with `@Sql` scripts or `@Transactional` rollback.
- Test full flows: register → verify OTP → login → get profile → delete account.

### Coverage
```xml
<!-- pom.xml JaCoCo configuration -->
<configuration>
  <rules>
    <rule>
      <element>BUNDLE</element>
      <limits>
        <limit>
          <counter>LINE</counter>
          <value>COVEREDRATIO</value>
          