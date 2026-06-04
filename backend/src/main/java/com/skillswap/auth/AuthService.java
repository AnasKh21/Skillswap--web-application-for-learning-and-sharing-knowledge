package com.skillswap.auth;

import com.skillswap.auth.dto.LoginRequestDto;
import com.skillswap.auth.dto.RegisterRequestDto;
import com.skillswap.user.User;
import com.skillswap.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Handles authentication logic: register, login, refresh, logout.
 *
 * Security points:
 * - BCrypt via PasswordEncoder — passwords are NEVER stored in plaintext
 * - Tokens are set as HttpOnly + SameSite=Strict cookies (not JSON body)
 *   so JavaScript cannot read them (XSS protection)
 * - "Bad credentials" is the same message for wrong email AND wrong password
 *   (prevents user enumeration attacks)
 */
@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    // Set to true in production (HTTPS only). False in dev (HTTP localhost).
    // TODO: set app.jwt.cookie-secure=true in production environment
    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    public AuthService(UserService userService,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RefreshTokenService refreshTokenService,
                       @Value("${app.jwt.expiration-ms}") long accessTokenExpiryMs,
                       @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpiryMs) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    // Creates a new account. Hashes the password with BCrypt before saving.
    public User register(RegisterRequestDto dto) {
        // BCrypt generates a random salt and hashes the password.
        // The hash includes the salt, so two identical passwords produce different hashes.
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        return userService.createUser(dto.getEmail(), hashedPassword, dto.getDisplayName());
    }

    // Verifies credentials, generates tokens, sets them as HttpOnly cookies.
    public User login(LoginRequestDto dto, HttpServletResponse response) {
        // Always say "Bad credentials" — never reveal whether email or password was wrong
        User user = userService.findUserByEmail(dto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        issueTokenCookies(user, response);
        return user;
    }

    // Exchanges a refresh token for a new access token (and rotates the refresh token).
    public User refresh(String rawRefreshToken, HttpServletResponse response) {
        RefreshTokenService.RotationResult result = refreshTokenService.validateAndRotate(rawRefreshToken);
        User user = result.user();

        // Issue new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());
        setAccessTokenCookie(response, newAccessToken);

        // Issue rotated refresh token
        setRefreshTokenCookie(response, result.newRawRefreshToken());

        return user;
    }

    // Clears both cookies and revokes all refresh tokens for the user.
    public void logout(UUID userId, HttpServletResponse response) {
        refreshTokenService.revokeAllForUser(userId);
        clearCookies(response);
    }

    // ── Cookie helpers ────────────────────────────────────────

    private void issueTokenCookies(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String rawRefreshToken = refreshTokenService.createRefreshToken(user);
        setAccessTokenCookie(response, accessToken);
        setRefreshTokenCookie(response, rawRefreshToken);
    }

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)               // JavaScript cannot read this cookie
                .secure(cookieSecure)         // HTTPS only in production
                .sameSite("Strict")           // blocks CSRF from other origins
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpiryMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", rawToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/api/auth/refresh")    // cookie only sent to the refresh endpoint
                .maxAge(Duration.ofMillis(refreshTokenExpiryMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookies(HttpServletResponse response) {
        // Set Max-Age=0 to immediately expire both cookies
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("access_token", "").httpOnly(true).path("/").maxAge(0).build().toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                ResponseCookie.from("refresh_token", "").httpOnly(true).path("/api/auth/refresh").maxAge(0).build().toString());
    }
}
