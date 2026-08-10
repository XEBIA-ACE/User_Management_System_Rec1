# Verify OTP for Account Activation

| | |
|---|---|
| **ID** | US-006 |
| **Feature** | F-01 — User Registration |
| **Epic** | EP-002 — Account Activation via OTP |
| **Status** | Draft |
| **Date** | 2026-08-10 |

## Background

Part of feature *User Registration*.

## Acceptance Criteria

### Story

- [ ] Given a user has received an OTP, When the user enters the valid OTP within 5 minutes, Then the account is activated and the attempt is logged.
- [ ] Given a user enters an expired OTP, When the system verifies it, Then the OTP is rejected and the user is informed of the expiration.

### Epic

- [ ] Given a user registers, when an OTP is generated, then it should be sent to the user’s registered email.
- [ ] Given a user receives an OTP, when it is entered in the verification page within 5 minutes, then the account should be activated.
- [ ] Given a user enters an expired OTP, then the account should not be activated.

## Proposed Solution

### Functional Specification

_Not specified._

### Technical Design

_Not specified._

## Affected Services

_None identified._

## API Changes

_No API changes identified._

## Open Questions / Gaps

_No gaps identified._