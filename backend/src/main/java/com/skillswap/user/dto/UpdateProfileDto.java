package com.skillswap.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO — what the FRONTEND SENDS when a user edits their profile.
 *
 * Flow:  Browser → PUT /api/users/me  (body = this object as JSON)
 *        Backend  → updates displayName and bio in DB → returns updated UserResponseDto
 *
 * Note: email and password changes are NOT included here on purpose.
 * Changing a password should go through a dedicated "change password" endpoint
 * that requires the current password first (security best practice).
 */
@Data
public class UpdateProfileDto {

    @NotBlank(message = "Display name is required")
    private String displayName;

    // Bio is optional — a user might not want to write one
    private String bio;
}
