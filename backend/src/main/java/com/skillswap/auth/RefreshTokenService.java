package com.skillswap.auth;

import com.skillswap.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Manages refresh tokens: creation, validation, rotation, and revocation.
 *
 * Refresh token flow:
 * 1. Login  → createRefreshToken() → returns raw token (sent to client as cookie)
 * 2. Expiry → validateAndRotate()  → revokes old token, returns new raw token
 * 3. Logout → revokeAllForUser()   → all tokens for this user become invalid
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpiryMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration-ms}") long refreshExpiryMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    // Generates a new refresh token, stores its hash in DB, returns the raw value.
    // The raw value is sent to the client — we never store it ourselves.
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();  // cryptographically random
        String hash = hashToken(rawToken);
        Instant expiresAt = Instant.now().plusMillis(refreshExpiryMs);

        refreshTokenRepository.save(new RefreshToken(user, hash, expiresAt));
        return rawToken;
    }

    // Validates a refresh token and rotates it (old one revoked, new one created).
    // Returns the new raw refresh token.
    // Throws 401 if the token is invalid, expired, or already revoked.
    @Transactional
    public RotationResult validateAndRotate(String rawToken) {
        String hash = hashToken(rawToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.isRevoked()) {
            // Possible token reuse attack — revoke all tokens for this user
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token already used");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        // Revoke the old token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Issue a new one
        String newRawToken = createRefreshToken(stored.getUser());
        return new RotationResult(stored.getUser(), newRawToken);
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // SHA-256 hash of the raw token, base64-encoded for storage
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    // Simple value object returned by validateAndRotate
    public record RotationResult(User user, String newRawRefreshToken) {}
}
