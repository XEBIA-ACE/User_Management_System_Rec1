# Account Deletion

| | |
|---|---|
| **ID** | US-004 |
| **Feature** | F-01 — User Registration |
| **Epic** | EP-001 — User Onboarding and Registration Flow |
| **Status** | Draft |
| **Date** | 2026-08-24 |

## Background

Part of feature *User Registration*.

## Acceptance Criteria

### Story

- Given the user logs in successfully, When the user initiates account deletion, Then the system asks for confirmation.
- Given confirmation is granted, When the deletion process is initiated, Then the system permanently removes all associated data.
- Given a user tries to log in after deletion, When they attempt access, Then the system denies login and informs the account no longer exists.

### Epic

- Given a new user, when registering, then an OTP is sent to the user's email.
- Given a valid OTP, when entered, then the user's account is activated.
- Given an unverified user, when attempted to log in, then access is denied.
- Given a user, when account deletion is confirmed, then the account is permanently removed.
- All communication should be encrypted using HTTPS.

## Proposed Solution

### Functional Specification

## S-001

### Purpose
This specification describes the functional requirements for enabling account deletion by users in the User Management Service, ensuring personal data is permanently removed from the platform.

### Scope
The specification covers the deletion of user accounts, including confirmation, data removal, and ensuring no access post-deletion within the User Management Service.

### Non-Goals
1. User data backup or export process
2. Temporarily deactivating accounts
3. Notification to third parties about account deletion
4. Recovery of deleted accounts
5. Notification to friends or connections of a deleted user
6. Deletion of non-user-generated content
7. Impact analysis on aggregated data
8. Modification of external authentication systems
9. Handling legal requests for data retention
10. Integration with analytics tools

### Key Entities
- **UserAccount**: 
  - **Attributes**: 
    - `userId` (string), 
    - `username` (string), 
    - `email` (string), 
    - `data` (object)
  - **Relationships**: None

### Functional Requirements
- FR-001: User Management Service MUST prompt for confirmation when a user initiates account deletion.
- FR-002: User Management Service MUST delete all associated data permanently upon receiving confirmation.
- FR-003: User Management Service MUST deny any login attempts with a deleted account and inform users their account no longer exists.
- FR-004: User Management Service SHOULD validate that a user is logged in successfully before allowing account deletion.
- FR-005: User Management Service MAY log account deletion events for audit purposes.

### Assumptions Propagation
- A-001: The system assumes users are uniquely identified by `userId`. (FR-002)
- A-002: Deletion is irreversible and user-confirmed. (FR-001, FR-002)

### Success Criteria
- SC-001: Account deletion requests result in all user data being irrecoverable in at least 99% of cases.
- SC-002: Users are denied login with sufficient feedback in 100% of post-deletion attempts.

### Priority Levels
- P1: Account deletion prompt and data removal (FR-001, FR-002)
- P2: Denial of login post-deletion (FR-003)
- P3: Log account deletion events (FR-005)

### Edge Cases
- EC-001: Given a user who aborted initial deletion, When they retry, Then they must be prompted for confirmation again.
- EC-002: Given a user initiates deletion without login, When they attempt to delete, Then the system must block the action.
- EC-003: Given network failure during deletion, When the user retries, Then previous states must not affect new deletion attempts.

### Independent Testability
- Preconditions: User is logged in; account deletion is initiated.
- User Action: Confirm deletion.
- Outcome: User account and data are irrevocably deleted. 

### Separation of Concerns
The description outlines expected functionalities and constraints. Implementation details such as API paths, response formats, or specific protocol behaviors are intentionally omitted, focusing solely on user-facing capabilities and functional outcomes. External systems such as backup services or third-party integrators are represented abstractly.

### Technical Design

## S-001

### Technical Design Specification

#### Contracts & Interfaces

1. **API Contracts**:
   - **Delete User Endpoint**: 
     - **Method**: DELETE
     - **Path**: `/api/v1/users/{userId}`
     - **Request**: No body required. Requires user authentication.
     - **Responses**:
       - `204 No Content`: Successful deletion.
       - `401 Unauthorized`: User not authenticated.
       - `403 Forbidden`: User not authorized.
       - `404 Not Found`: User account does not exist.

2. **Data Schema Changes**:
   - **UserAccount Table**: 
     - `userId` (Primary Key, String)
     - `username` (String)
     - `email` (String)
     - `data` (JSON) 
     - An existing field indicating deletion status SHALL be leveraged for post-deletion checks.
   - **AuditLog Table** (New): 
     - `logId` (Primary Key, Auto Increment)
     - `userId` (String, Foreign Key)
     - `timestamp` (Timestamp)
     - `action` (String, Enum: 'DELETION')

#### Test Strategy

1. **Contract Validation Tests** (JUnit/Mockito):
   - **Successful Deletion**: Verify `204 No Content` response when a valid account is deleted.
   - **Unauthorized Deletion**: Ensure `401 Unauthorized` if user authentication fails.
   - **Post-Deletion Login Denial**: After deletion, trigger login attempt and assert `403 Forbidden`.

2. **Integration Tests**:
   - **Data Irreversibility**: Assert that data entries for a user in `UserAccount` are irrecoverable post-deletion.
   - **Audit Logging**: Validate entry creation in `AuditLog` table on successful account deletion.
   - **Edge Cases** (using Mockito to simulate failures):
     - Test network failure resilience during deletion.

#### Implementation Approach

1. **Core Logic**:
   - **AccountDeletionService** (Java Spring Boot):
     - `deleteUserAccount(String userId)`: Validates authorization, deletes user data from `UserAccount`, logs action in `AuditLog`.
     - `denyPostDeletionAccess(String userId)`: Checks deletion status during authentication and denies access if applicable.
   
2. **Inter-Service Calls**:
   - No direct inter-service calls specified. Any dependencies (e.g., notification) are outside the scope, ensuring separation of concerns.

3. **Asynchronous Patterns**:
   - Use Spring’s asynchronous capabilities to log audit information (`AuditLogService`) independently after user data removal to reduce latency impact on the delete operation.

#### Architectural Decision Records (ADRs)

- **ADR-001: Use Spring Boot for Account Deletion Implementation**
  - **Context**: Consistency with the existing tech stack (Spring Boot).
  - **Decision**: Maintain Spring Boot for user management operations.
  - **Rationale**: Leverages existing infrastructure and team expertise; simplifies integration and code reuse.
  - **Alternative**: Custom Java implementation without Spring, rejected due to increased complexity and maintenance overhead.

- **ADR-002: Introduction of `AuditLog` Table for Tracking Deletions**
  - **Context**: FR-005 requires logging of deletion events for audit purposes.
  - **Decision**: Introduce a dedicated `AuditLog` table.
  - **Rationale**: Provides structured log management, isolation from user data.
  - **Alternative**: Log deletions in existing log files, rejected for lack of structured queryability.

#### Simplicity Gate Assessment

- **Rating**: `appropriate`
  - All proposed technical elements map directly to specified functional requirements, ensuring coverage and alignment.

#### Affected Services and API Changes

- **Service**: User Management Service
- **API Changes**: Expansion of the DELETE `/api/v1/users/{userId}` endpoint to incorporate new response codes and post-deletion access checks.

This technical specification ensures that all functional requirements are adequately supported, with focus on core implementation using current technologies and methodologies pertinent to the existing system architecture.

## Affected Services

- `S-001`

## API Changes

| Service | Endpoint | Method | Change |
|---------|----------|--------|--------|
| `S-001` | `/api/v1/users/{userId}` | DELETE | modify |

## Open Questions / Gaps

_No gaps identified._