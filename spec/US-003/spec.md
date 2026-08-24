# Login for Verified Users

| | |
|---|---|
| **ID** | US-003 |
| **Feature** | F-01 — User Registration |
| **Epic** | EP-001 — User Onboarding and Registration Flow |
| **Status** | Draft |
| **Date** | 2026-08-24 |

## Background

Part of feature *User Registration*.

## Acceptance Criteria

### Story

- [ ] Given the user account is verified with OTP, When the user inputs correct credentials, Then access is granted to the account.
- [ ] Given a user tries to log in, When the account is not verified, Then access is denied with a message prompting verification.
- [ ] Given a user logs in successfully, When accessing protected APIs, Then authentication is required for continued access.

### Epic

- [ ] Given a new user, when registering, then an OTP is sent to the user's email.
- [ ] Given a valid OTP, when entered, then the user's account is activated.
- [ ] Given an unverified user, when attempted to log in, then access is denied.
- [ ] Given a user, when account deletion is confirmed, then the account is permanently removed.
- [ ] All communication should be encrypted using HTTPS.

## Proposed Solution

### Functional Specification

## S-001

### Purpose
This specification defines the functionality for verified users to securely log in to their accounts, granting them access to protected features and data.

### Scope
The specification covers login capabilities of the User Management Service, focusing on user verification, credential validation, and authentication for API access.

### Non-Goals
1. User registration
2. Password recovery
3. Multi-factor authentication beyond OTP
4. Unverified user account creation
5. Deactivation of user accounts
6. OTP generation process
7. User data validation logic
8. User role management
9. Protected feature implementation
10. Detailed logging and monitoring solutions

### Key Entities
- **User**: Attributes: userId (String), isVerified (Boolean), credentials (Object)
- **Login**: Attributes: credentials (Object), isAuthenticated (Boolean)
- **Verification Status**: Attributes: status (Boolean), message (String)

### Functional Requirements
- **FR-001**: User Management Service SHALL verify user credentials for login.
- **FR-002**: User Management Service MUST grant access if the user is verified and credentials are correct.
- **FR-003**: User Management Service MUST deny access if the account is not verified.
- **FR-004**: User Management Service MUST provide feedback prompting verification for unverified accounts.
- **FR-005**: User Management Service SHALL require authentication for accessing protected APIs after login.

### Assumptions
- **A-001**: Verified users have received and entered OTP correctly for account verification. (Related: FR-002, FR-003)
- **A-002**: Users are aware of credential requirements, such as password strength. (Related: FR-001)

### Success Criteria
- **SC-001**: Percentage of verified users logging in successfully is at least 95%.
- **SC-002**: Unverified user login attempts correctly denied equal to 100% of attempts.
- **SC-003**: Authentication enforced on protected API access after user login at least 99% of the time.

### Priority Levels
- **P1**: Nominal path for successful login of verified users (AC1)
- **P2**: Denied access and messaging for unverified users (AC2)
- **P2**: Continuous authentication for protected APIs (AC3)

### Edge Cases
- **EC-001**: Given incorrect credentials, When user attempts login, Then access is denied with "Invalid credentials" message.
- **EC-002**: Given user account locked after multiple failed login attempts, When user tries login, Then access is denied with "Account locked" notification.
- **EC-003**: Given a verified user changes their password, When user attempts initial login with an old password, Then access is denied with "Invalid credentials" message.

### Independent Testability
- **Preconditions**: User account is registered, verified with OTP, credentials set.
- **User Action**: User attempts to log in with correct credentials.
- **Observable Outcome**: User is successfully logged in and can access protected features.

### Separation of Concerns
This specification focuses on login capabilities and user outcomes in the User Management Service, prioritizing the verification process, credential validation, and API access control. No implementation details, endpoints, HTTP codes, or framework annotations are included. Integration points with OTP services and authentication mechanisms are considered but not described in detail.

### Technical Design

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

## Affected Services

- `S-001`

## API Changes

| Service | Endpoint | Method | Change |
|---------|----------|--------|--------|
| `S-001` | `/api/v1/auth/login` | POST | modify |

## Open Questions / Gaps

_No gaps identified._