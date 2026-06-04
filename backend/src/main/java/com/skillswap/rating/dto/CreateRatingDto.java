package com.skillswap.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * DTO — what the FRONTEND SENDS when a user rates another after a session.
 *
 * Flow:  Browser → POST /api/ratings  (body = this object as JSON)
 *        Backend  → creates a Rating row + updates target user's averageRating
 *
 * Note: the authorId is NOT here — the backend reads it from the JWT token.
 * TODO Phase 3: Replace @RequestParam authorId with @AuthenticationPrincipal
 *
 * Example:
 * {
 *   "sessionId": "...",
 *   "targetId":  "a0000000-0000-0000-0000-000000000002",
 *   "score":     5,
 *   "comment":   "Great teacher, very patient!"
 * }
 */
@Data
public class CreateRatingDto {

    @NotNull(message = "sessionId is required")
    private UUID sessionId;

    @NotNull(message = "targetId is required")
    private UUID targetId;

    @NotNull(message = "score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;

    private String comment;  // optional written feedback
}
