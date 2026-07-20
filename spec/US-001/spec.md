# User Registration

| | |
|---|---|
| **ID** | US-001 |
| **Feature** | F-01 — User Registration |
| **Epic** | EP-001 — User Onboarding and Registration Flow |
| **Status** | Draft |
| **Date** | 2026-07-20 |

## Background

Part of feature *User Registration*.

## Acceptance Criteria

### Story

- [ ] Given the registration page is accessible, When a user enters a unique email, valid name, and password, Then the system registers the account and sends an OTP to the email.
- [ ] Given a user enters an already registered email, When they attempt to register, Then the system denies registration and shows an appropriate error message.
- [ ] Given a user registers successfully, When the registration is complete, Then the password must be stored securely in a hashed format.

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

The purpose of this specification is to define the functional requirements for the user registration process within the User Management Service, ensuring accurate account creation and secure handling of user credentials.

### Scope

This specification covers the registration capability of the User Management Service, specifically detailing requirements for account creation, email verification via OTP, and secure password storage.

### Non-Goals

1. Password reset functionality.
2. User profile updates post-registration.
3. Multi-factor authentication beyond OTP.
4. User deletion or deactivation.
5. Login and session management.
6. Third-party service integration.
7. Notification sending mechanisms beyond OTP.
8. Detailed UI/UX design specifications.
9. Performance optimization.
10. Support for non-email registration methods.

### Key Entities

- **User**: 
  - email: string
  - name: string
  - password: string (hashed)
- **OTP**:
  - otpCode: string
  - validUntil: datetime

### Functional Requirements

- FR-001: User Management Service SHALL allow registration with a unique email, valid name, and password.
- FR-002: User Management Service MUST send an OTP to the user's email upon successful registration.
- FR-003: User Management Service MUST NOT register a user with an already registered email.
- FR-004: User Management Service MUST provide an error message if registration fails due to an existing email.
- FR-005: User Management Service SHALL ensure the password is stored securely in a hashed format.

### Assumptions

- A-001: Registration requires email, name, and password (Assumed: Required fields verification).
- A-002: OTP is the only method of email verification.

### Success Criteria

- SC-001: Registration attempts with a unique email are greater than 95% successful.
- SC-002: 100% of successfully registered emails receive an OTP.
- SC-003: At least 99% of passwords are stored in a securely hashed format.

### Priority Levels

- P1: Successful registration with unique email and successful email OTP dispatch.
- P2: Error handling for duplicate email registration.
- P2: Secure password storage verification.

### Edge Cases

- EC-001: Given an offline user's email server, When OTP is sent, Then it should be retried thrice before failure.
- EC-002: Given a non-compliant email format, When attempting registration, Then the system must reject the request with a message.
- EC-003: Given a password below acceptable strength criteria, When submitted, Then registration must be denied with feedback.

### Independent Testability

To verify the functional requirements:
1. Preconditions:
   - Registration page is accessible.
   - Test email is available.
2. User Action:
   - Submit new user registration with a valid, unique email, valid name, and strong password.
3. Observable Outcome:
   - User receives an OTP email and registration succeeds.

### Separation of Concerns

This specification focuses on user registration capabilities, ensuring functionality for account creation, business rules for input validation, and feedback on errors. External systems involved include the OTP service for verification, without discussing underlying technical implementations.

### Technical Design

## S-001

### Technical Design Specification for User Registration in User Management Service

#### Contracts & Interfaces

1. **API Changes**
   - **Endpoint Addition**: 
     - **POST /api/v1/users/register**
       - Request Payload: `{"email": "string", "name": "string", "password": "string"}`
       - Response: `201 Created` on success, `409 Conflict` if email exists, `400 Bad Request` for invalid input.
   - **Endpoint Usage**:
     - **POST /api/v1/users/verify**
       - To be called after registration with payload `{"email": "string", "otpCode": "string"}`.
       - Response: `200 OK` on successful verification, `400 Bad Request` for invalid OTP.

2. **Data Model Changes**
   - **User Table**
     - Columns: 
       - `email VARCHAR(255) PRIMARY KEY`
       - `name VARCHAR(255)`
       - `password VARCHAR(255)` (hashed)
     - Indexes: 
       - UNIQUE index on `email`
   - **OTP Table**
     - Columns:
       - `otpCode VARCHAR(6)`
       - `email VARCHAR(255) FOREIGN KEY REFERENCES Users(email)`
       - `validUntil DATETIME`

#### Test Strategy

1. **API Contract Tests**
   - **Test Case 1**: Verify `POST /api/v1/users/register` returns `201 Created` for valid, unique email.
   - **Test Case 2**: Verify `POST /api/v1/users/register` returns `409 Conflict` for duplicate email.
   - **Test Case 3**: Verify `POST /api/v1/users/register` returns `400 Bad Request` for non-compliant email format.
   - **Test Case 4**: Confirm `POST /api/v1/users/verify` accepts correct OTP and returns successful verification.

2. **Security Compliance Tests**
   - **Test Case 5**: Verify password storage uses a secure hashing algorithm.
   - **Test Case 6**: Ensure OTP is generated, valid for the expected duration, and linked with the correct email.

#### Implementation Approach

1. **Core Implementation Logic**
   - **RegistrationController** (`POST /register`)
     - Method `registerUser(RequestBody registrationDto)` SHALL check for existing email using `UserRepository.findByEmail(String email)`.
     - If email is unique, create user and hash password using `PasswordHasher.hash(String password)` and save via `UserRepository.save(User user)`.
     - Call `OtpService.generateOtp(String email)` to send OTP, return `201 Created`.

2. **Inter-service Calls and Async Patterns**
   - **OTP Service Call (External Call)**
     - Used for OTP generation. Implement an async retry mechanism accessing `OtpService.generateOtp()` three times on failure using `RetryTemplate`.

3. **Architectural Decision Records (ADRs)**
   - **ADR-001: Secure Password Hashing**
     - Decision to use BCrypt for password hashing to satisfy SC-003.
   - **ADR-002: Unique Email Constraint with Database Index**
     - Provides assurance against duplicate email registration per FR-003.

#### Simplicity Gate Assessment

- **Appropriate**: Each technical element directly maps to the functional requirements:
  - Registration Controller serves FR-001, FR-004.
  - OTP Service integration serves FR-002.
  - Password hashing mechanism ensures FR-005.

#### Affected Services and API Changes

- **Affected Service**: User Management Service (S-001)
- **API Changes**:
  - Addition of `POST /api/v1/users/register` endpoint.
  - Clarification and test requirements for existing `POST /api/v1/users/verify` endpoint to handle email verification.

This technical specification adequately covers the functional requirements, ensuring separation of concerns with existing components and external services managing specific processing and validation logic.

## Affected Services

- `S-001`

## API Changes

| Service | Endpoint | Method | Change |
|---------|----------|--------|--------|
| `User Management Service` | `/api/v1/users/register` | POST | modify |

## Open Questions / Gaps

_No gaps identified._