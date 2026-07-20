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