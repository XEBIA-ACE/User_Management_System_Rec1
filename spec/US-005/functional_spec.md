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