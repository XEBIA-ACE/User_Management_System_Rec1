## S-001

### Contracts & Interfaces

**API Contracts:**

1. **Login Endpoint:**

   - **Method**: POST `/api/v1/auth/login`
   - **Request Body Schema**:
     - `username`: String (required)
     - `password`: String (required)
   - **Responses**:
     - `200 OK`: Successfully authenticated, returns authentication token.
     - `401 Unauthorized`: Incorrect credentials or account not verified.
     - `403 Forbidden`: Account locked message.
     - `400 Bad Request`: Missing or invalid request parameters.

2. **Verification Feedback Interface:**

   - **Status**: Boolean
   - **Message**: String

**Data Schema Changes:**

- **User Table Modifications:**
  - Table Name: `Users`
  - Add Column: `isLocked` BOOLEAN DEFAULT FALSE
  - Add Column: `failedAttempts` INTEGER DEFAULT 0
  - Add Index: `idx_user_verification` on `isVerified`

### Test Strategy

**Test Cases:**

1. **Login API Tests:**
   - Verify `200 OK` when valid credentials for a verified user are provided (Validates FR-001, FR-002).
   - Verify `401 Unauthorized` if an account is not verified (Validates FR-003).
   - Verify `403 Forbidden` after account lock due to multiple failed attempts (Validates EC-002).
   - Verify `400 Bad Request` on missing credentials in request (Validates request schema).

2. **Feedback Interface Tests:**
   - Ensure feedback contains correct `status` and `message` when an unverified user attempts login (Validates FR-004).

**Testing Tools**: Use **JUnit** for unit tests and **Mockito** for mocking dependencies in the Java environment. Integration tests with **Spring Boot Test** to validate endpoints.

### Implementation Approach

**Core Implementation Logic:**

1. **LoginService Class:**
   - Method: `public boolean authenticate(String username, String password)`
     - Retrieve user from `Users` table.
     - Check `isVerified`. If false, log attempt and return response with feedback.
     - Validate credentials; increment `failedAttempts` on failure.
     - Lock account by setting `isLocked = TRUE` after predefined threshold of `failedAttempts`.

2. **AuthenticationController Class:**
   - Endpoint Handler: `public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest)`
     - Call `LoginService.authenticate()`.
     - Generate JWT token on success; set HTTP 200.
     - Handle exceptions and set appropriate HTTP response status.

3. **Integration Patterns:**
   - Synchronous calls to `LoginService` without external async patterns as all required data resides within the service’s database.
   - Inter-service OTP verification call omitted as it's out of scope.

### Architectural Decision Records (ADRs)

**ADR-001: Use of Spring Security for Authentication**

- **Context**: The service requires authentication handling for logging in users.
- **Decision**: Spring Security's capabilities to manage user sessions and authenticate users with minimal additional logic is leveraged.
- **Rationale**: It offers extensive, customizable security configurations and integrates natively with Spring Boot.
- **Alternatives**: Custom authentication logic - rejected due to increased complexity and risk of security vulnerabilities.

**ADR-002: Adding 'isLocked' and 'failedAttempts' Columns**

- **Context**: Protect user accounts from brute-force attacks.
- **Decision**: Add columns to track login attempts and lock accounts.
- **Rationale**: Simplifies determining account status and reduces reliance on external services.
- **Alternatives**: Implementing throttling via an external service - rejected due to unnecessary complexity.

### Simplicity Gate Assessment

**Assessment**: `appropriate`

- The technical elements provided map directly to functional requirements and support secure login handling. No extra elements that are not directly supporting a functional requirement have been introduced.