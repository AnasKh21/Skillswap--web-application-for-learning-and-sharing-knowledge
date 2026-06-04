package com.skillswap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO — what the FRONTEND SENDS when a user logs in.
 *
 * Flow:  Browser → POST /api/auth/login  (body = this object as JSON)
 *        Backend  → checks credentials → returns LoginResponseDto (contains the JWT token)
 */
@Data
public class LoginRequestDto {

    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
