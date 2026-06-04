package com.skillswap.rating.dto;

import com.skillswap.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO — what the BACKEND SENDS BACK after creating a rating, or when listing ratings.
 *
 * Example JSON:
 * {
 *   "id": "...",
 *   "sessionId": "...",
 *   "author": { "id": "...", "displayName": "Alice" },
 *   "target": { "id": "...", "displayName": "Bob"   },
 *   "score": 5,
 *   "comment": "Amazing session!",
 *   "createdAt": "2026-06-04T18:00:00Z"
 * }
 */
@Data
@Builder
public class RatingResponseDto {

    private UUID id;
    private UUID sessionId;
    private UserResponseDto author;  // who left the rating
    private UserResponseDto target;  // who received the rating
    private Integer score;
    private String comment;
    private Instant createdAt;
}
