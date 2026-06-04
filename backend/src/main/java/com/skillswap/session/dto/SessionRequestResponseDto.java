package com.skillswap.session.dto;

import com.skillswap.common.RequestStatus;
import com.skillswap.skill.dto.SkillDto;
import com.skillswap.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO — what the BACKEND SENDS BACK when the frontend asks for session requests.
 *
 * It contains nested objects (UserResponseDto, SkillDto) so the frontend
 * has everything it needs to render a request card without extra API calls.
 *
 * Example JSON:
 * {
 *   "id": "...",
 *   "initiator": { "id": "...", "displayName": "Alice", ... },
 *   "receiver":  { "id": "...", "displayName": "Bob",   ... },
 *   "offeredSkill": { "id": "...", "name": "Java", "category": "Programming" },
 *   "wantedSkill":  { "id": "...", "name": "Guitar", "category": "Music" },
 *   "status": "PENDING",
 *   "message": "Hey! Want to swap?",
 *   "createdAt": "2026-06-04T10:00:00Z"
 * }
 */
@Data
@Builder
public class SessionRequestResponseDto {

    private UUID id;
    private UserResponseDto initiator;
    private UserResponseDto receiver;
    private SkillDto offeredSkill;
    private SkillDto wantedSkill;
    private RequestStatus status;
    private String message;
    private Instant createdAt;
}
