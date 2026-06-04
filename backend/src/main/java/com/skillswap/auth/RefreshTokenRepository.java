package com.skillswap.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Look up a token by its hash (used during validation)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Revoke all refresh tokens for a user (called on logout)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user.id = :userId")
    void revokeAllByUserId(UUID userId);
}
