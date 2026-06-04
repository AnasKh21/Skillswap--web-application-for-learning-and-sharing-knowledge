package com.skillswap.auth;

import com.skillswap.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * A refresh token stored in the database.
 *
 * Security design:
 * - We store the SHA-256 HASH of the token, never the raw value.
 *   If the DB is leaked, attackers cannot use the hashes directly.
 * - On each use, the old token is revoked and a new one is issued (rotation).
 *   This limits the damage window if a token is stolen.
 * - Tokens have an expiration date enforced both here and at the DB level.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SHA-256 hash of the raw token — unique so we can look it up
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // true = this token has been used or the user logged out
    @Column(nullable = false)
    private boolean revoked = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ── Constructors ────────────────────────────────────────────────────────────

    public RefreshToken() {}

    public RefreshToken(User user, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // ── Getters / Setters ───────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
