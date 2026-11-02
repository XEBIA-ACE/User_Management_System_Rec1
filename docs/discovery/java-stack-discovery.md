# Java Stack Discovery — User Management Service

- **Story:** `821e2e5f-1c36-562c-80a5-309b01416304` — Discover current Java stack, build tool, framework, and deployment model
- **Baseline commit:** `3dd4c77529a637b09a0e6ad5bae40e98804f2149` (grounding snapshot `08c69d7e2b1db79a`)
- **Accepted impact analysis:** `sha256:665cef57aa9be87c52242f066d0c8e90839cae9aaf152b8e98c92893d3141c3b`
- **Status:** Baseline for subsequent modernization stories (see [Baseline reference](#baseline-reference-fr-005--s1-ac5)).

All facts below are taken from files in this repository at the baseline commit. Anything that could not be
established from the repository is listed under [Unresolved unknowns](#unresolved-unknowns). Operational
contacts and external deployment systems were out of scope for this (automated) discovery.

## Summary

| Topic | Finding | Confidence |
|---|---|---|
| Java runtime (compile) | `java.version` = **8** in `app/pom.xml` — but source requires **Java 16+** (records) and Spring Boot 3.2.5 requires **Java 17+**; baseline does not compile | Observed — conflicting |
| Java runtime (run) | **Java 17** (`eclipse-temurin:17-jre-alpine`) in `app/Dockerfile` | Observed (container only) |
| Build tool | **Maven** — single module, `app/pom.xml`; no Maven wrapper committed | Observed |
| Dependency management | Maven Central via `spring-boot-starter-parent:3.2.5` BOM; only `jjwt` versions pinned explicitly | Observed |
| Primary framework | **Spring Boot 3.2.5** (Spring MVC on embedded Tomcat, Spring Security, Data JPA, Data Redis, AMQP, Actuator, Flyway) | Observed |
| Interfaces | HTTP/JSON REST via Spring MVC `@RestController`s; outbound AMQP (RabbitMQ) publishing; Actuator HTTP endpoints | Observed |
| Packaging | Executable **JAR** (`<packaging>jar</packaging>` + `spring-boot-maven-plugin`) | Observed |
| Hosting model | Standalone JVM (`java -jar`) inside a Docker container; local orchestration via Docker Compose | Observed for dev; production unknown |
| Containerized? | **Yes, containerization artifacts exist** (`app/Dockerfile`, `app/docker-compose.yml`). Whether production runs the container is **unknown** | Partially observed |

## Java runtime version (FR-001 / S1-AC1)

Evidence inspected: `app/pom.xml`, `app/Dockerfile`, `app/README.md`, `AGENTS.md`, `app/src/main/java/**`.

| Source | Declared Java version | Applies to |
|---|---|---|
| `app/pom.xml` → `<properties><java.version>8</java.version>` | 8 | Compile (`maven-compiler-plugin` `release`/`source`/`target` via the Spring Boot parent) |
| `app/Dockerfile` builder stage `FROM eclipse-temurin:17-jdk-alpine` | 17 (JDK) | Container build (compile) |
| `app/Dockerfile` runtime stage `FROM eclipse-temurin:17-jre-alpine` | 17 (JRE) | Container runtime |
| `app/README.md` "Technology Stack" / "Prerequisites" | 17 / "Java 17+" | Developer documentation |
| `AGENTS.md` "Stack" | 21 | Agent guidance (aspirational, see mismatch below) |
| `spring-boot-starter-parent` **3.2.5** | requires ≥ 17 | Framework baseline |
| Source code: `record` types in `app/src/main/java/com/usermanagement/adapter/in/web/dto/*.java` | requires ≥ 16 | Language level |

Findings:

- `java.version` was changed from `17` to `8` in the baseline commit `3dd4c77` ("Update pom.xml"). With JDK 21
  and Maven 3.9.9, `mvn compile` at the baseline fails with `records are not supported in -source 8`.
  Therefore **the declared compile target (8) is not a working configuration**; the effective, working Java
  version for compile cannot be confirmed from the repository.
- The only concrete runtime declaration is **Java 17** (Eclipse Temurin, Alpine) in the Docker image. The Docker
  build itself runs `mvn package` with JDK 17 but still honours `java.version=8` from the POM, so it is expected
  to hit the same compile failure.
- **Dev vs. prod:** The repository documents local development with "Java 17+" (README) and a containerized
  runtime on Java 17. No production-specific runtime declaration exists; production Java version is an unresolved
  unknown (U-1).
- **Mismatch with `AGENTS.md`:** `AGENTS.md` states Java 21 and describes a project layout (`docker/`,
  `.github/workflows/ci.yml`, `com/company/usermanagement/...`) that does not exist in this repository. The
  actual layout is under `app/` with package `com.usermanagement`. `AGENTS.md` is treated as target-state
  guidance, not as evidence of the current stack.

## Build tool and dependency management (FR-002 / S1-AC2)

- **(a) Primary build tool:** Apache **Maven** (POM model 4.0.0). No Gradle, Ant, or custom build scripts exist.
- **(b) Build descriptor location(s):**
  - `app/pom.xml` — the only build descriptor; single-module project
    (`com.usermanagement:user-management-service:1.0.0`).
  - `app/Dockerfile` — builder stage installs Maven from Alpine packages (`apk add maven`) and runs
    `mvn -B package -DskipTests`.
  - No Maven wrapper (`mvnw`, `.mvn/`) is committed, although `app/README.md` and `AGENTS.md` refer to `./mvnw`.
  - No CI definitions exist (no `.github/workflows/`, Jenkinsfile, GitLab CI, etc.).
- **(c) Dependency management:**
  - Declared in `app/pom.xml` `<dependencies>`; resolved from Maven Central (no custom `<repositories>` or
    `<distributionManagement>` in the POM).
  - Versions are managed by the parent BOM `org.springframework.boot:spring-boot-starter-parent:3.2.5` for all
    Spring Boot starters, PostgreSQL driver, Flyway, Lombok, H2 and test libraries.
  - Explicitly pinned: `io.jsonwebtoken:jjwt-api/jjwt-impl/jjwt-jackson` `0.11.5` via `${jjwt.version}`.
  - Declared dependencies: `spring-boot-starter-{web,data-jpa,data-redis,security,amqp,validation,actuator}`,
    `postgresql` (runtime), `flyway-core`, `jjwt-*`, `lombok` (optional), and test-scoped
    `spring-boot-starter-test`, `spring-security-test`, `h2`.
  - Build plugin: `spring-boot-maven-plugin` (repackage; excludes Lombok).
  - **Gap:** source code imports `org.mapstruct` (`infrastructure/persistence/mapper/*EntityMapper.java`) but
    MapStruct is not declared in the POM; with `-Djava.version=17` the compile still fails on missing MapStruct
    and other symbols. Dependency declarations are therefore incomplete relative to the source (U-3).

## Primary framework and interfaces (FR-003 / S1-AC3)

- **Primary framework:** **Spring Boot 3.2.5** (Spring Framework 6 / Jakarta EE namespace), evidenced by the
  parent POM, the starters above, and `@SpringBootApplication` entry points.
- **HTTP interface:** Spring MVC (`spring-boot-starter-web`, embedded Tomcat by default — no alternative server
  declared). Controllers (`@RestController`):
  - `adapter/in/web/HealthController.java` — `GET /api/v1/health`
  - `adapter/in/web/UserController.java` — `POST /api/v1/users/{register,login,verify}`, `DELETE /api/v1/users/{userId}`
  - `adapter/in/web/controller/AuthController.java` — `POST /api/v1/auth/{register,login,verify}`
  - `adapter/in/web/controller/UserController.java` — `DELETE /api/v1/users/me`
  - Spring Boot Actuator — `health`, `info` exposed (`app/src/main/resources/application.yml`).
  - Security: Spring Security with JWT filter(s) (`JwtAuthFilter`, `JwtAuthenticationFilter`).
- **Other interfaces:** outbound AMQP publishing to RabbitMQ (`RabbitMQEventPublisher`,
  `RabbitMqNotificationAdapter`); no inbound `@RabbitListener`, scheduled jobs, gRPC or WebSocket endpoints were
  found. Outbound data stores: PostgreSQL (JPA/Hibernate, Flyway migrations), Redis (Spring Data Redis).
- **Uncertainty (not assumed away):** the codebase contains **two parallel implementations** of several
  components — two `@SpringBootApplication` classes (`UserManagementApplication`,
  `UserManagementServiceApplication`), two `UserController`s, two `SecurityConfig`s, two
  `GlobalExceptionHandler`s, two RabbitMQ configs, and two DTO packages. Which set is the authoritative HTTP
  surface (`/api/v1/users/*` vs `/api/v1/auth/*`) cannot be determined from the repository (U-4).
  Spring Boot is unambiguously the primary framework.

## Packaging and deployment model (FR-004 / S1-AC4)

- **(a) Packaging format:** executable Spring Boot **JAR** — `<packaging>jar</packaging>` plus
  `spring-boot-maven-plugin`; artifact `target/user-management-service-1.0.0.jar` (README, Dockerfile
  `COPY --from=builder /build/target/user-management-service-*.jar app.jar`).
- **(b) Hosting/deployment model:** **standalone JVM** running the fat JAR (`ENTRYPOINT ["java", "-jar", "app.jar"]`)
  with embedded Tomcat on port 8080, **inside a Docker container** (multi-stage `app/Dockerfile`, non-root
  `appuser`). No external application server (WAR deployment) is used.
- **Containerization status:** **Containerized artifacts exist** — `app/Dockerfile`, `app/.dockerignore`,
  and `app/docker-compose.yml` (app + `postgres:15-alpine`, `redis:7-alpine`,
  `rabbitmq:3.12-management-alpine`). Docker Compose is documented as the recommended way to run the stack
  locally (`app/README.md`). There are **no** Kubernetes/Helm manifests, cloud deployment descriptors, or
  deployment pipelines, so whether production currently runs this container — and on what platform — is
  **unknown** (U-2).
- **Operations contacts / external deployment information:** none referenced in the repository; contacting
  operations is out of scope for this automated change (U-2, U-5).

## Unresolved unknowns

| ID | Unknown / ambiguity | Related | What is needed to resolve |
|---|---|---|---|
| U-1 | Effective Java version for compile, and the production runtime Java version. POM says 8 (non-working), Dockerfile/README say 17, `AGENTS.md` says 21. | FR-001 | Confirmation from service owners/operations of the intended and deployed JDK; POM correction in a follow-up story |
| U-2 | Production hosting platform and whether the Docker image is what runs in production (no K8s/Helm/cloud/IaC manifests, no deployment pipeline). | FR-004 | Operations/deployment documentation outside this repository |
| U-3 | Dependency declarations are incomplete: `org.mapstruct` is imported but not declared; baseline does not compile even with Java 17. Any internal artifact repository/mirror is not documented. | FR-002 | Build fix story; confirmation of artifact repository usage |
| U-4 | Duplicate application entry points, controllers, security configs and DTO packages; authoritative HTTP API surface is undetermined. | FR-003 | Service owner decision on which implementation is canonical |
| U-5 | No CI/build pipeline in the repository (`AGENTS.md` refers to `.github/workflows/ci.yml`, which does not exist); how artifacts are built/published for release is unknown. | FR-002, FR-004 | CI/CD ownership information |
| U-6 | Spec mismatch: the story references `specs/821e2e5f-1c36-562c-80a5-309b01416304/tasks.md` (tasks `T001`–`T015`), which is not present in the repository at the baseline commit. | FR-005 | Spec export to be committed by the spec pipeline |

## Baseline reference (FR-005 / S1-AC5)

This document (`docs/discovery/java-stack-discovery.md`) is the **baseline source** for the Java version, build
tool, framework, packaging/hosting model and containerization status of the user management service.
Subsequent modernization stories SHALL reference it, and it is linked from
`specs/821e2e5f-1c36-562c-80a5-309b01416304/README.md` and the repository `README.md`.

Conformance of this document to FR-001–FR-005 is checked by
`docs/discovery/tests/test_java_stack_discovery.py`
(`python3 -m unittest discover -s docs/discovery/tests`).
