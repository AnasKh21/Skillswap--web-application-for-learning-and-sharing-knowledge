package com.skillswap.auth.dto;

import com.skillswap.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO — what the BACKEND SENDS BACK after a successful login.
 *
 * The frontend receives this, saves the token in localStorage,
 * and includes it in the Authorization header of every future request:
 *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * @AllArgsConstructor — Lombok generates a constructor with all fields,
 *   so you can write: new LoginResponseDto(token, userDto)
 */
@Data
@AllArgsConstructor
public class LoginResponseDto {

    // The JWT token — the frontend stores this and sends it with every request
    // TODO Phase 3: this field is always null until JwtUtil is implemented
    private String token;

    // The logged-in user's public profile (no password)
    private UserResponseDto user;
}
