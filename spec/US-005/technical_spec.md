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