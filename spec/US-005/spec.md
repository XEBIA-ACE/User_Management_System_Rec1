# Send OTP to User Email

| | |
|---|---|
| **ID** | US-005 |
| **Feature** | F-01 — User Registration |
| **Epic** | EP-002 — Account Activation via OTP |
| **Status** | Draft |
| **Date** | 2026-08-10 |

## Background

Part of feature *User Registration*.

## Acceptance Criteria

### Story

- [ ] Given a new registration attempt, When the user provides a valid email address, Then an OTP is generated and sent to the email within 2 seconds.
- [ ] Given an OTP is generated, When the OTP is sent, Then the system logs the OTP generation and sending attempt for audit purposes.

### Epic

- [ ] Given a user registers, when an OTP is generated, then it should be sent to the user’s registered email.
- [ ] Given a user receives an OTP, when it is entered in the verification page within 5 minutes, then the account should be activated.
- [ ] Given a user enters an expired OTP, then the account should not be activated.

## Proposed Solution

### Functional Specification

## S-001

### Purpose

This specification defines the functional requirements for sending a one-time password (OTP) to a user’s email address during registration as part of the account activation process.

### Scope

This specification covers the process beginning when a user submits a registration request with an email address and ending when the system logs the generation and attempted delivery of the OTP. It applies only to the User Management Service’s handling of email OTP for new registrations.

### Non-Goals

1. Password policy enforcement
2. Management of user login sessions
3. Resending OTPs post initial attempt
4. Mobile/SMS OTP delivery
5. User onboarding beyond OTP verification
6. Email content customization or localization
7. Rate limiting or anti-abuse measures
8. Delivery guarantees for external email failures
9. Management UI for audits
10. Registration workflows post-OTP verification

### Key Entities

- **UserRegistration**
  - email_address: string
  - registration_id: string
  - status: string (pending, verified, failed)
- **OTP**
  - code: string
  - expiration_timestamp: datetime
  - delivery_channel: string (email)
  - status: string (generated, sent, failed)
  - associated_registration: UserRegistration [1:1]
- **OTPEventLog**
  - event_id: string
  - event_type: string (generated, sent, failure)
  - timestamp: datetime
  - related_registration: UserRegistration [1:1]

### Functional Requirements

FR-001: User Management Service SHALL generate a one-time password (OTP) when a valid email address is submitted during registration.  
FR-002: User Management Service SHALL send the OTP to the provided email address within 2 seconds of OTP generation.  
FR-003: User Management Service SHALL validate that the provided email address is syntactically valid before generating OTP.  
FR-004: User Management Service MUST log each OTP generation event for audit purposes, linked to the registration attempt.  
FR-005: User Management Service MUST log every OTP sending attempt for audit purposes, regardless of outcome (success or failure).  
FR-006: User Management Service MUST NOT generate or send OTP if email address is missing or invalid.  
FR-007: User Management Service MUST NOT send more than one OTP per registration email until previous OTP expires or process is complete.  
FR-008: User Management Service SHOULD display a user-friendly error message if email sending fails.  
FR-009: User Management Service SHALL NOT include the OTP in any logs except secure audit logs accessible by privileged admins.

### Assumptions Propagation

A-001: A valid email address is required for OTP delivery. (FR-001, FR-003, FR-006)  
A-002: OTP generation and sending occur synchronously with registration attempt. (FR-001, FR-002)  
A-003: Audit logs are only accessible to privileged admins. (FR-009)  
A-004: The external email service is responsible for actual email delivery, not success guarantee. (FR-002, FR-005, FR-008)

### Success Criteria

SC-001: OTP sent to user email within 2 seconds for at least 99% of successful registration attempts.  
SC-002: 100% of OTP generation and sending attempts are logged with correct registration reference.  
SC-003: System SHALL reject registration attempts with invalid email addresses.

### Priority Levels

- FR-001: P1
- FR-002: P1
- FR-003: P2
- FR-004: P1
- FR-005: P1
- FR-006: P2
- FR-007: P2
- FR-008: P2
- FR-009: P3

### Edge Cases

EC-001: Given a registration attempt, When the provided email is syntactically invalid, Then OTP generation and sending SHALL NOT proceed and the user is notified.  
EC-002: Given a successful OTP generation, When the email service is temporarily unavailable, Then the system logs the failure and notifies the user of delivery failure.  
EC-003: Given a registration attempt, When an OTP has already been sent and is unexpired, Then the system SHALL NOT generate or send a new OTP.  
EC-004: Given an OTP is generated, When audit logging fails, Then the registration SHALL be marked as failed and no OTP is sent.  
EC-005: Given all systems are operating normally, When a registration is completed and OTP sent, Then all logs SHALL reference the same registration ID.

### Independent Testability

Preconditions:  
1. The user has not registered before with the email address used.  
2. The email address submitted is syntactically valid.  
3. The registration endpoint is reachable.  

User Action:  
The user submits a registration request with a valid email address.

Observable Outcome:  
An OTP is delivered to the submitted email address within 2 seconds and the audit logs contain entries for both OTP generation and sending attempt, both referencing the registration.

### Technical Design

## S-001

**Technical Design Specification – OTP Generation and Delivery during Registration**  
**Service: User Management Service (S-001)**  
**Tech Stack: Spring Boot, Spring Security, PostgreSQL, Redis, RabbitMQ**

---

### 1. Contracts & Interfaces

#### 1.1 API Contracts

**Endpoint:**  
`POST /api/v1/users/register`  
Request Body:
- `email`: string (required)

Response:
- `201 Created` if registration + OTP process started successfully (OTP sent or delivery in progress)
- `400 Bad Request` for missing or syntactically invalid email address
- `409 Conflict` if unexpired OTP already exists for registration
- `500 Internal Server Error` if audit/logging fails or another error occurs

Response Error Body (on failure):
- `message`: string (user-friendly)
- `error_code`: string (e.g., `INVALID_EMAIL`, `EMAIL_SEND_FAILED`, `LOGGING_FAILURE`, etc.)

#### 1.2 Data Model Changes

**Table:** `user_registration`
- Columns:
  - `registration_id`: UUID (PK)
  - `email_address`: varchar(255), unique
  - `status`: varchar(16) (enum: pending, verified, failed)
  - `created_at`, `updated_at`: TIMESTAMP

**Table:** `otp`
- Columns:
  - `otp_id`: UUID (PK)
  - `registration_id`: UUID (FK to user_registration)
  - `code`: varchar(8)
  - `expiration_timestamp`: TIMESTAMP
  - `delivery_channel`: varchar(8) (always 'email')
  - `status`: varchar(16) (generated, sent, failed)
  - `created_at`, `updated_at`: TIMESTAMP
- Index: unique on (`registration_id`, `status`) where `status`IN ('generated', 'sent') and `expiration_timestamp` > NOW()

**Table:** `otp_event_log`
- Columns:
  - `event_id`: UUID (PK)
  - `event_type`: varchar(16) (generated, sent, failure)
  - `timestamp`: TIMESTAMP
  - `registration_id`: UUID (FK to user_registration)
  - `details`: JSONB (includes masked email and, if event_type='generated', encrypted OTP code)
- Index: `registration_id`
- NOTE: Only store OTP code in `event_type='generated'` and encrypt at rest

#### 1.3 External Service Integration

- Email delivery via domain-registered SMTP or third-party SMTP provider
- Async event queue (`rabbitmq`) for audit/event logs (optional for resilience, see ADR)

---

### 2. Test Strategy

**Frameworks:**  
- API: JUnit 5 (`@WebMvcTest`), Spring MockMvc for endpoint tests  
- Repo/DB: Spring Data JPA Test with embedded PostgreSQL  
- Integration: Testcontainers (PostgreSQL, Redis, RabbitMQ)  
- Mocking/stubbing: Mockito, WireMock (for email provider if real upstream used)

**Key Test Cases:**
- `POST /api/v1/users/register` with valid email: Returns 201, OTP record created, audit logs present (FR-001, FR-002, FR-004, FR-005)
- Registration with invalid email syntax: Returns 400, no OTP or log records created (FR-003, FR-006)
- Registration when unexpired OTP exists: Returns 409, no new OTP or logs created (FR-007, EC-003)
- Simulated email delivery failure: Returns 500 or 201 with warning, logs failure event (FR-005, FR-008, EC-002)
- Simulated audit-logging failure: Registration marked failed, returns 500, no OTP generated or sent (FR-004, EC-004)
- Assert OTP is only present in `otp_event_log` with `event_type='generated'`, not in general app/log output (FR-009)
- Asserts for log referencing correct `registration_id` throughout (SC-002, EC-005)

---

### 3. Implementation Approach

#### 3.1 Core Classes & Methods

- `UserRegistrationController` (REST controller)
  - `registerUser(@RequestBody UserRegistrationRequest req)`

- `RegistrationApplicationService`
  - `register(String email)`: Orchestrates full registration logic
    - Validates email syntax using RFC 5322-compliant regex (`EmailValidator`)
    - Checks for existing user_registration with same email, status != failed
    - Looks up any non-expired OTP for registration (via OTPRepository with proper index)
    - If clear, creates `user_registration` record
    - Generates OTP (cryptographically random 6-8 digit alphanumeric, e.g., using `SecureRandom`)
    - Begins DB transaction:
      - Persists OTP
      - Writes to `otp_event_log` (event_type=generated, encrypts OTP code)
    - Commits transaction
    - Attempts synchronous email send (via `EmailDeliveryService.sendOtp(email, code)`)
      - On success, updates OTP.status to `sent`, logs `otp_event_log` (event_type=sent)
      - On failure, updates OTP.status to `failed`, logs `otp_event_log` (event_type=failure), returns user-friendly error
      - All log entries reference the registration ID

- `OTPRepository`, `UserRegistrationRepository`, `OtpEventLogRepository` (Spring Data JPA)

- `OtpCodeGenerator`: Utility, encapsulates OTP pattern and SecureRandom logic

- `EmailDeliveryService`: Abstracted/mocked for tests

- `OtpEncryptor`: Handles strong symmetric encryption of OTP in logs

#### 3.2 Transaction, Performance, Error Handling
- Transaction boundary covers OTP and

## Affected Services

- `S-001`

## API Changes

| Service | Endpoint | Method | Change |
|---------|----------|--------|--------|
| `S-001` | `/api/v1/users/register` | POST | Modify |
| `S-001` | `/api/v1/users/otp/send` | POST | Add (internal or async handler) |

## Open Questions / Gaps

_No gaps identified._