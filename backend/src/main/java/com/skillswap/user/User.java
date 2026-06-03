package com.skillswap.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // unique = true means two users cannot share the same email
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    // length = 1000 so users can write a longer bio
    @Column(length = 1000)
    private String bio;

    @Column(name = "last_login")
    private Instant lastLogin;

    // this value is updated each time the user receives a new rating
    @Column(name = "average_rating")
    private Double averageRating;

    // updatable = false means this field cannot be changed after it is first set
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // @PrePersist runs automatically just before the entity is saved for the first time
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ── Constructors ────────────────────────────────────────────────────────────

    // JPA requires a no-arg constructor
    public User() {}

    public User(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getDisplayName() { return displayName; }

    public String getBio() { return bio; }

    public Instant getLastLogin() { return lastLogin; }

    public Double getAverageRating() { return averageRating; }

    public Instant getCreatedAt() { return createdAt; }

    // ── Setters ─────────────────────────────────────────────────────────────────

    public void setId(UUID id) { this.id = id; }

    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public void setBio(String bio) { this.bio = bio; }

    public void setLastLogin(Instant lastLogin) { this.lastLogin = lastLogin; }

    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ── equals & hashCode ───────────────────────────────────────────────────────
    // We compare users by their database ID only

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', displayName='" + displayName + "'}";
    }
}
