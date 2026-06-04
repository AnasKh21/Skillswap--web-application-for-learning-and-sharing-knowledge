package com.skillswap.user.dto;

import com.skillswap.common.SkillType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * DTO — what the FRONTEND SENDS when a user adds a skill to their profile.
 *
 * Flow:  Browser → POST /api/users/me/skills  (body = this object as JSON)
 *        Backend  → creates a UserSkill row in the DB
 *
 * Example JSON body:
 * {
 *   "skillId": "b0000000-0000-0000-0000-000000000001",
 *   "type": "OFFERED",
 *   "level": 4
 * }
 */
@Data
public class AddUserSkillDto {

    // The UUID of the skill from the skills table (use GET /api/skills to find them)
    @NotNull(message = "skillId is required")
    private UUID skillId;

    // OFFERED = I can teach this skill / WANTED = I want to learn this skill
    @NotNull(message = "type is required (OFFERED or WANTED)")
    private SkillType type;

    // Proficiency: 1 = complete beginner, 5 = expert
    @NotNull(message = "level is required")
    @Min(value = 1, message = "Level must be at least 1")
    @Max(value = 5, message = "Level must be at most 5")
    private Integer level;
}
