package com.skillswap.skill;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // A skill name must be unique in the platform (e.g. "Java", "Guitar")
    @Column(unique = true, nullable = false)
    private String name;

    // Examples: "Programming", "Music", "Language", "Sports"
    @Column(nullable = false)
    private String category;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Skill() {}

    public Skill(String name, String category) {
        this.name = name;
        this.category = category;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public String getName() { return name; }

    public String getCategory() { return category; }

    // ── Setters ─────────────────────────────────────────────────────────────────

    public void setId(UUID id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setCategory(String category) { this.category = category; }

    // ── equals & hashCode ───────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Skill{id=" + id + ", name='" + name + "', category='" + category + "'}";
    }
}
