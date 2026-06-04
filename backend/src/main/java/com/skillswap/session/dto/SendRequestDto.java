package com.skillswap.session.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * DTO — what the FRONTEND SENDS when a user swipes right and sends a swap request.
 *
 * Flow:  Browser → POST /api/session-requests  (body = this object as JSON)
 *        Backend  → creates a SessionRequest row with status PENDING
 *
 * Example: Alice wants to swap Java (she offers) for Guitar (she wants) with Bob:
 * {
 *   "receiverId":      "a0000000-0000-0000-0000-000000000002",   ← Bob's UUID
 *   "offeredSkillId":  "b0000000-0000-0000-0000-000000000001",   ← Java UUID
 *   "wantedSkillId":   "b0000000-0000-0000-0000-000000000002",   ← Guitar UUID
 *   "message":         "Hey Bob! I'd love to trade lessons."
 * }
 *
 * The initiatorId is NOT in this DTO — the backend knows who the sender is from the JWT token.
 * TODO Phase 3: Extract initiatorId from @AuthenticationPrincipal instead of @RequestParam
 */
@Data
public class SendRequestDto {

    @NotNull(message = "receiverId is required")
    private UUID receiverId;

    @NotNull(message = "offeredSkillId is required")
    private UUID offeredSkillId;

    @NotNull(message = "wantedSkillId is required")
    private UUID wantedSkillId;

    // Optional greeting message from the initiator
    private String message;
}
