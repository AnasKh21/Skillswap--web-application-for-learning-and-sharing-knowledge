package com.skillswap.session;

import com.skillswap.common.RequestStatus;
import com.skillswap.skill.Skill;
import com.skillswap.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A SessionRequest is what happens when one user invites another for a skill exchange.
 *
 * Example: Alice (initiator) sends Bob (receiver) a request:
 *   "I offer Java (offeredSkill) and I want Guitar (wantedSkill)"
 */
@Entity
@Table(name = "session_requests")
public class SessionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The user who sends the request
    @ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    // The user who receives the request
    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // The skill the initiator offers to teach
    @ManyToOne(optional = false)
    @JoinColumn(name = "offered_skill_id", nullable = false)
    private Skill offeredSkill;

    // The skill the initiator wants to learn
    @ManyToOne(optional = false)
    @JoinColumn(name = "wanted_skill_id", nullable = false)
    private Skill wantedSkill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    // Optional message from the initiator to the receiver
    @Column(length = 500)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        // Every new request starts as PENDING
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
    }

    // ── Constructors ────────────────────────────────────────────────────────────

    public SessionRequest() {}

    public SessionRequest(User initiator, User receiver, Skill offeredSkill, Skill wantedSkill, String message) {
        this.initiator = initiator;
        this.receiver = receiver;
        this.offeredSkill = offeredSkill;
        this.wantedSkill = wantedSkill;
        this.message = message;
        this.status = RequestStatus.PENDING;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public User getInitiator() { return initiator; }

    public User getReceiver() { return receiver; }

    public Skill getOfferedSkill() { return offeredSkill; }

    public Skill getWantedSkill() { return wantedSkill; }

    public RequestStatus getStatus() { return status; }

    public String getMessage() { return message; }

    public Instant getCreatedAt() { return createdAt; }

    // ── Setters ─────────────────────────────────────────────────────────────────

    public void setId(UUID id) { this.id = id; }

    public void setInitiator(User initiator) { this.initiator = initiator; }

    public void setReceiver(User receiver) { this.receiver = receiver; }

    public void setOfferedSkill(Skill offeredSkill) { this.offeredSkill = offeredSkill; }

    public void setWantedSkill(Skill wantedSkill) { this.wantedSkill = wantedSkill; }

    public void setStatus(RequestStatus status) { this.status = status; }

    public void setMessage(String message) { this.message = message; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ── equals & hashCode ───────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionRequest other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SessionRequest{id=" + id + ", status=" + status + "}";
    }
}
