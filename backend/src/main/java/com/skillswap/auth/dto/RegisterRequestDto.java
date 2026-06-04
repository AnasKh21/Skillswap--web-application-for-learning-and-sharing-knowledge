package com.skillswap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO — what the FRONTEND SENDS when a user signs up.
 *
 * Flow:  Browser → POST /api/auth/register  (body = this object as JSON)
 *        Backend  → creates a User in the DB → returns UserResponseDto
 *
 * @NotBlank  = field cannot be null or empty string (triggers a 400 if violated)
 * @Email     = must look like a valid email address
 * @Size      = min/max character length
 */
@Data  // Lombok: generates getters, setters, equals, hashCode, toString
public class RegisterRequestDto {

    @NotBlank(message = "Display name is required")
    private String displayName;

    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
