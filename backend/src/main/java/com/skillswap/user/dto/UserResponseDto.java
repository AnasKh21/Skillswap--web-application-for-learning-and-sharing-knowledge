package com.skillswap.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO — what the BACKEND SENDS BACK when the frontend asks for a user's profile.
 *
 * KEY POINT: This has no "password" field.
 * The User entity has a password, but we NEVER send it to the browser.
 * That's the whole point of having a DTO — it's a filtered copy of the entity.
 *
 * @Builder — Lombok generates a builder pattern, so you can write:
 *   UserResponseDto.builder()
 *       .id(user.getId())
 *       .displayName(user.getDisplayName())
 *       .build();
 */
@Data
@Builder
public class UserResponseDto {

    private UUID id;
    private String displayName;
    private String email;
    private String bio;
    private Double averageRating;
    private String avatarUrl;
    private String bannerUrl;
    private Instant createdAt;
}
