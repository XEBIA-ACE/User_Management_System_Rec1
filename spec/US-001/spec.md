# User Registration

| | |
|---|---|
| **ID** | US-001 |
| **Feature** | F-01 — User Registration |
| **Epic** | EP-001 — User Onboarding and Registration Flow |
| **Status** | Draft |
| **Date** | 2026-08-21 |

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
This specification details the functionality required for user registration, enabling new users to create an account in the User Management Service.

### Scope
This specification covers the User Management Service's user registration capabilities, including data validation, account creation, and email-based OTP notification.

### Non-Goals
1. User login process
2. Account deletion
3. Health check or metrics endpoints
4. Password recovery
5. User authentication after registration

### Key Entities
- **User**
  - `email` (String)
  - `name` (String)
  - `password` (String, hashed)
  
### Functional Requirements

- **FR-001**: User Management Service MUST validate that the email entered by the user is unique.
- **FR-002**: User Management Service MUST validate the entered name and password for format compliance.
- **FR-003**: User Management Service SHALL register the user when all input data is valid and unique.
- **FR-004**: User Management Service MUST hash the password securely before storing it.
- **FR-005**: User Management Service SHALL send an OTP to the user's email upon successful registration.
- **FR-006**: User Management Service MUST provide an error message when a duplicate email is detected.

### Assumptions Propagation
- **A-001**: Users have valid access to their email ([FR-003], [FR-005]).
  
### Success Criteria
- **SC-001**: Registration request processed with a unique email equals 100%
- **SC-002**: Error message for duplicate email delivered at least 95%
- **SC-003**: OTP sent for successful user registrations at least 95%

### Priority Levels
- **P1**: Successful user registration with email confirmation ([FR-003], [FR-005])
- **P2**: Duplicate email validation ([FR-006])
- **P2**: Password hashing compliance ([FR-004])

### Edge Cases

- **EC-001**: Given a weak password, When user registers, Then registration MUST be denied with a password strength error.
- **EC-002**: Given an email with incorrect format, When user registers, Then registration MUST be denied with a format error.
- **EC-003**: Given a missing name field, When user registers, Then registration MUST be denied due to incomplete profile.

### Independent Testability
- **Preconditions**: User registration page is accessible, form fields are present
- **User Action**: Submit registration form with all fields completed
- **Observable Outcome**: User receives OTP or an error message without dependencies

### Separation of Concerns
- The specification defines what the User Management Service must do for user registration but does not describe the technical methods used to achieve these outcomes.
- Error conditions are described in terms of user notification and internal process adherence without revealing internal state representations.
- Logical role names are used for external system interaction, not specific product identities.

### Technical Design

## S-001

### Contracts & Interfaces

1. **API Contract for User Registration**:
   - **Endpoint**: `POST /api/v1/users/register`
   - **Request Body Schema**:
     - `email` (String, required): Valid email address
     - `name` (String, required): User's name
     - `password` (String, required): Raw password to be hashed
   - **Response Codes**:
     - `201 Created`: User successfully registered, OTP sent
     - `400 Bad Request`: Validation errors, including duplicate email and format issues
     - **Error Messages**:
       - `email_duplicate`: Email already exists
       - `email_format_error`: Invalid email format
       - `password_strength_error`: Password does not meet strength requirements
       - `name_missing`: Name field is missing

2. **Database Schema**:
   - **Table**: `users`
     - `id` (UUID, primary key)
     - `email` (VARCHAR, unique, indexed)
     - `name` (VARCHAR)
     - `password_hash` (VARCHAR)

### Test Strategy

1. **Validation Tests**:
   - **Test**: Verify duplicate email error
     - Ensures error response for existing email is `400 Bad Request` with `email_duplicate`
   - **Test**: Validate email format
     - Submits invalid email; expects `400 Bad Request` with `email_format_error`
   - **Test**: Validate password strength
     - Ensures weak passwords result in `password_strength_error`
   - **Test**: Missing name field
     - Validates missing name returns `name_missing`

2. **Registration Success Tests**:
   - **Test**: Successful registration
     - Confirm database entry, password hashing, and OTP dispatch
    
**Testing Framework**: `JUnit` for unit and integration tests, `Mockito` for service mocking, and `Spring Boot Test` for end-to-end validation.

### Implementation Approach

1. **Core Logic**:
   - **Controller**: `UserRegistrationController`
     - Handles `POST /api/v1/users/register`
     - Method: `registerUser(UserRegistrationDto dto)`: delegates to `RegistrationApplicationService`
   - **Service**: `RegistrationApplicationService`
     - **Method**: `register(UserRegistrationDto dto)`
       - Validate email (FR-001), name, and password (FR-002)
       - Hash password using a secure method (e.g., BCrypt) (FR-004)
       - Persist user in the `users` table
       - Trigger email OTP service (FR-005)

2. **Password Hashing**:
   - **Algorithm**: BCryptPasswordEncoder (Spring Security)

3. **Email OTP Integration**:
   - **Service**: `EmailNotificationService`
     - **Method**: `sendOtp(String email)`: sends OTP to provided email

4. **Inter-Service Communication**:
   - **Email Service** interaction via Spring `RestTemplate` to call external OTP dispatch API

### Architectural Decision Records (ADRs)

- **ADR-001**: Use Spring Boot framework
  - **Context**: Matches existing system architecture
  - **Decision**: Continue using Spring Boot with JPA
  - **Rationale**: Familiarity and support for needed features (REST, security, validation)
  - **Alternatives**: Other frameworks discarded due to non-compatibility

- **ADR-002**: Password Hashing with BCrypt
  - **Context**: Secure storage of passwords
  - **Decision**: Use BCrypt for hashing
  - **Rationale**: Industry standard, good security-to-complexity ratio
  - **Alternatives**: Other hashing methods not considered as secure

### Simplicity Gate Assessment

- **Assessment**: Appropriate
  - All implemented technical elements directly map to specified functional requirements.
  - No over-engineered components present.

### Affected Services and API Changes

- **Service**: User Management Service
- **API Changes**: Introduction of a new endpoint `POST /api/v1/users/register`
  - Affects email service via OTP integration

This technical design adheres to the user registration criteria within the defined context and technological stack guidelines.

## Affected Services

- `S-001`

## API Changes

| Service | Endpoint | Method | Change |
|---------|----------|--------|--------|
| `S-001` | `/api/v1/users/register` | POST | modification |

## Open Questions / Gaps

_No gaps identified._