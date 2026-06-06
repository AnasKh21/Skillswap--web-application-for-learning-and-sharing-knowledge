package com.skillswap.auth;

import com.skillswap.auth.dto.LoginRequestDto;
import com.skillswap.auth.dto.LoginResponseDto;
import com.skillswap.auth.dto.RegisterRequestDto;
import com.skillswap.user.User;
import com.skillswap.user.UserMapper;
import com.skillswap.user.UserService;
import com.skillswap.user.dto.UserResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

/**
 * AuthController — register, login, refresh, logout.
 *
 * Base URL: /api/auth
 *
 * All these endpoints are public (no token required) — see SecurityConfig.
 *
 * Token flow:
 *   1. POST /login   → sets access_token + refresh_token cookies
 *   2. Every request → JwtAuthFilter reads the access_token cookie automatically
 *   3. POST /refresh → when access token expires, call this to get a new one
 *   4. POST /logout  → clears both cookies + revokes refresh tokens in DB
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper  userMapper;

    public AuthController(AuthService authService, UserService userService, UserMapper userMapper) {
        this.authService = authService;
        this.userService = userService;
        this.userMapper  = userMapper;
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/auth/register
    // Creates a new account. Password is BCrypt-hashed by AuthService.
    // ─────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid RegisterRequestDto dto) {
        try {
            User user = authService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(user));
        } catch (IllegalArgumentException e) {
            // UserService throws this when the email is already taken
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/auth/login
    // Verifies credentials. On success, sets HttpOnly cookies.
    // Returns the user's public profile (no token in body — it's in the cookie).
    // ─────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto dto,
            HttpServletResponse response) {

        User user = authService.login(dto, response);
        // Token is NOT returned in the body — it's in the Set-Cookie header.
        // This prevents JavaScript from accessing it (XSS protection).
        return ResponseEntity.ok(new LoginResponseDto(null, toDto(user)));
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/auth/refresh
    // Exchanges the refresh_token cookie for a new access_token cookie.
    // Call this when your API returns 401 (access token expired).
    // ─────────────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<UserResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        String rawRefreshToken = extractCookie(request, "refresh_token");
        if (rawRefreshToken == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token");
        }

        // validateAndRotate: verifies token, revokes old one, issues new pair
        User user = authService.refresh(rawRefreshToken, response);
        return ResponseEntity.ok(toDto(user));
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/auth/logout
    // Clears cookies and revokes all refresh tokens in the DB.
    // Requires authentication (you must be logged in to log out).
    // ─────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {

        User user = userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        authService.logout(user.getId(), response);
        return ResponseEntity.noContent().build();  // 204
    }

    // ── Helpers ───────────────────────────────────────────────

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private UserResponseDto toDto(User user) {
        return userMapper.toDto(user);
    }
}
