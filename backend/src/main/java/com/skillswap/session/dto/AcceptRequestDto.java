package com.skillswap.session.dto;

import com.skillswap.common.SessionMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO — what the FRONTEND SENDS when a user accepts an incoming swap request.
 *
 * Flow:  Browser → PUT /api/session-requests/{id}/accept  (body = this object as JSON)
 *        Backend  → sets request status to ACCEPTED + creates a new Session row
 *
 * Example:
 * {
 *   "scheduledAt": "2026-07-01T14:00:00Z",
 *   "mode":        "ONLINE",
 *   "location":    null
 * }
 *
 * For IN_PERSON sessions, "location" would be something like "Starbucks, Rue de Rivoli, Paris".
 */
@Data
public class AcceptRequestDto {

    // ISO 8601 datetime string — Spring converts it to java.time.Instant automatically
    @NotNull(message = "scheduledAt is required")
    private java.time.Instant scheduledAt;

    @NotNull(message = "mode is required (ONLINE or IN_PERSON)")
    private SessionMode mode;

    // Only required when mode = IN_PERSON
    private String location;
}
