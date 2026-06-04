package com.skillswap.user.dto;

import com.skillswap.common.SkillType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO — what the BACKEND SENDS BACK when listing a user's skills.
 *
 * Flow:  Browser → GET /api/users/me/skills
 *        Backend  → returns a list of this DTO
 *
 * Example JSON response item:
 * {
 *   "id": "c0000000-0000-0000-0000-000000000001",
 *   "skillId": "b0000000-0000-0000-0000-000000000001",
 *   "skillName": "Java",
 *   "type": "OFFERED",
 *   "level": 5
 * }
 */
@Data
@Builder
public class UserSkillDto {

    private UUID id;          // the UserSkill row ID (needed to delete it)
    private UUID skillId;     // the Skill row ID
    private String skillName; // included so the frontend doesn't need a second request
    private SkillType type;   // OFFERED or WANTED
    private Integer level;    // 1–5
}
