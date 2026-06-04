package com.skillswap.session.dto;

import com.skillswap.common.SessionMode;
import com.skillswap.common.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO — what the BACKEND SENDS BACK for a confirmed session.
 *
 * A Session is created when a SessionRequest is accepted.
 * The frontend uses this to show the "Upcoming Sessions" page.
 *
 * Example JSON:
 * {
 *   "id": "...",
 *   "request": { ... },           ← full SessionRequestResponseDto
 *   "scheduledAt": "2026-07-01T14:00:00Z",
 *   "mode": "ONLINE",
 *   "status": "SCHEDULED",
 *   "location": null
 * }
 */
@Data
@Builder
public class SessionResponseDto {

    private UUID id;
    // Embed the full request so the frontend knows who is meeting whom and for what skills
    private SessionRequestResponseDto request;
    private Instant scheduledAt;
    private SessionMode mode;
    private SessionStatus status;
    private String location;
}
