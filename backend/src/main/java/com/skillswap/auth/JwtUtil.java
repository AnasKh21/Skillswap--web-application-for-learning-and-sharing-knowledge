package com.skillswap.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generates and validates JWT access tokens.
 *
 * Security points addressed:
 * - Secret is injected from environment variable (never hardcoded)
 * - Minimum 256 bits enforced at startup (required for HS256)
 * - extractEmail() never throws — returns null on invalid/expired token
 *   so stack traces never leak to the HTTP response
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long accessTokenExpiryMs) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // HS256 requires at least 256 bits = 32 bytes.
        // We enforce this at startup so a misconfigured secret fails fast.
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret too short: must be at least 32 characters (256 bits). " +
                "Set the JWT_SECRET environment variable to a strong secret.");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    // Generates a signed access token. The "subject" is the user's email.
    // The token embeds: subject (email), issued-at, expiration.
    // The signature ensures nobody can forge or tamper with it without the secret.
    public String generateAccessToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiryMs))
                .signWith(signingKey)
                .compact();
    }

    // Extracts the email (subject) from a token.
    // Returns null if the token is invalid, expired, or tampered with.
    // NEVER throws — callers check for null instead of catching exceptions.
    // This prevents stack traces from appearing in HTTP responses.
    public String extractEmail(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid signature, expired, malformed — all treated the same: null
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        return extractEmail(token) != null;
    }
}
