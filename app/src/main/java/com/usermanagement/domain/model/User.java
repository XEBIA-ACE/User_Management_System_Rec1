package com.usermanagement.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Core domain entity representing a user.
 * Pure POJO — no framework annotations.
 */
public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {}

    public User(UUID id,
                String email,
                String passwordHash,
                String firstName,
                String lastName,
                UserStatus status,
                Instant createdAt,
                Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    public static User create(String email,
                              String passwordHash,
                              String firstName,
                              String lastName) {
        Instant now = Instant.now();
        return new User(
                UUID.randomUUID(),
                email,
                passwordHash,
                firstName,
                lastName,
                UserStatus.PENDING_VERIFICATION,
                now,
                now
        );
    }

    // ── Domain behaviour ─────────────────────────────────────────────────────

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        this.status = UserStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
