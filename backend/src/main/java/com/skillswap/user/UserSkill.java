package com.skillswap.user;

import com.skillswap.common.SkillType;
import com.skillswap.skill.Skill;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the link between a User and a Skill.
 * A UserSkill says "this user offers OR wants this skill, at this level".
 *
 * Example: Alice offers "Java" at level 4
 *          Alice wants  "Guitar" at level 1
 */
@Entity
@Table(
    name = "user_skills",
    indexes = {
        @Index(name = "idx_uskill_user_type",  columnList = "user_id, type"),
        @Index(name = "idx_uskill_skill_type", columnList = "skill_id, type")
    }
)
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // @ManyToOne: many UserSkills can point to the same User
    // @JoinColumn: this is the foreign key column in the "user_skills" table
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    // OFFERED or WANTED — stored as a readable string ("OFFERED") instead of a number
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType type;

    // Proficiency level: 1 = beginner, 5 = expert
    @Column(nullable = false)
    private Integer level;

    // ── Constructors ────────────────────────────────────────────────────────────

    public UserSkill() {}

    public UserSkill(User user, Skill skill, SkillType type, Integer level) {
        this.user = user;
        this.skill = skill;
        this.type = type;
        this.level = level;
    }

    // ── Getters ─────────────────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public User getUser() { return user; }

    public Skill getSkill() { return skill; }

    public SkillType getType() { return type; }

    public Integer getLevel() { return level; }

    // ── Setters ─────────────────────────────────────────────────────────────────

    public void setId(UUID id) { this.id = id; }

    public void setUser(User user) { this.user = user; }

    public void setSkill(Skill skill) { this.skill = skill; }

    public void setType(SkillType type) { this.type = type; }

    public void setLevel(Integer level) { this.level = level; }

    // ── equals & hashCode ───────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSkill other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserSkill{id=" + id + ", type=" + type + ", level=" + level + "}";
    }
}
