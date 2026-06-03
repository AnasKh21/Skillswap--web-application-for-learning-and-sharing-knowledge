package com.skillswap.rating;

import com.skillswap.session.Session;
import com.skillswap.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A Rating is left by one user (author) about another (target) after a Session.
 *
 * After a completed session, each participant can rate the other:
 *   - Alice rates Bob  → author = Alice, target = Bob
 *   - Bob rates Alice  → author = Bob,   target = Alice
 *
 * So one session can have up to 2 ratings.
 */
@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The session this rating belongs to
    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    // The user who writes the rating (auteur in the diagram)
    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // The user who receives the rating (cible in the diagram)
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    // Score from 1 to 5
    @Column(nullable = false)
    private Integer score;

    // Optional written feedback
    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // ── Constructors ────────────────────────────────────────────────────────────

    public Rating() {}

    public Rating(Session session, User author, User target, Integer score, String comment) {
        this.session = session;
        this.author = author;
        this.target = target;
        this.score = score;
        this.comment = comment;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public Session getSession() { return session; }

    public User getAuthor() { return author; }

    public User getTarget() { return target; }

    public Integer getScore() { return score; }

    public String getComment() { return comment; }

    public Instant getCreatedAt() { return createdAt; }

    // ── Setters ─────────────────────────────────────────────────────────────────

    public void setId(UUID id) { this.id = id; }

    public void setSession(Session session) { this.session = session; }

    public void setAuthor(User author) { this.author = author; }

    public void setTarget(User target) { this.target = target; }

    public void setScore(Integer score) { this.score = score; }

    public void setComment(String comment) { this.comment = comment; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ── equals & hashCode ───────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rating{id=" + id + ", score=" + score + "}";
    }
}
