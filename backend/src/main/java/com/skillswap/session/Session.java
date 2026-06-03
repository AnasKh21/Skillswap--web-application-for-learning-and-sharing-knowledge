package com.skillswap.session;

import com.skillswap.common.SessionMode;
import com.skillswap.common.SessionStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A Session is created when a SessionRequest is accepted.
 * It represents the actual meeting / learning exchange between two users.
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Each session comes from exactly one request
    // @OneToOne: one request creates at most one session
    @OneToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false, unique = true)
    private SessionRequest request;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    // ONLINE or IN_PERSON
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionMode mode;

    // SCHEDULED, COMPLETED, or CANCELLED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    // Optional: only relevant for IN_PERSON sessions (address, place name...)
    @Column(length = 300)
    private String location;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = SessionStatus.SCHEDULED;
        }
    }

    // ── Constructors ────────────────────────────────────────────────────────────

    public Session() {}

    public Session(SessionRequest request, Instant scheduledAt, SessionMode mode, String location) {
        this.request = request;
        this.scheduledAt = scheduledAt;
        this.mode = mode;
        this.location = location;
        this.status = SessionStatus.SCHEDULED;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public SessionRequest getRequest() { return request; }

    public Instant getScheduledAt() { return scheduledAt; }

    public SessionMode getMode() { return mode; }

    public SessionStatus getStatus() { return status; }

    public String getLocation() { return location; }

    // ── Setters ─────────────────────────────────────────────────────────────────

    public void setId(UUID id) { this.id = id; }

    public void setRequest(SessionRequest request) { this.request = request; }

    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public void setMode(SessionMode mode) { this.mode = mode; }

    public void setStatus(SessionStatus status) { this.status = status; }

    public void setLocation(String location) { this.location = location; }

    // ── equals & hashCode ───────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Session{id=" + id + ", status=" + status + ", scheduledAt=" + scheduledAt + "}";
    }
}
