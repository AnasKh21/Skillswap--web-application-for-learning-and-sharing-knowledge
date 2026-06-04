package com.skillswap.skill.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO — used both as a REQUEST (create a skill) and a RESPONSE (get skills).
 *
 * As a request:  Browser → POST /api/skills   body = { name, category }
 * As a response: Browser ← GET  /api/skills   body = list of { id, name, category }
 *
 * Note: when used as a request, the "id" field is ignored
 * (the backend generates the UUID automatically).
 */
@Data
@Builder
public class SkillDto {

    private UUID id;       // null when used as a request, filled by the backend in response
    private String name;
    private String category;
}
