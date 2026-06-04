-- ============================================================
-- V3: Add refresh_tokens table
-- ============================================================
-- Refresh tokens let users stay logged in without re-entering
-- their password, while keeping access tokens short-lived.
--
-- Security design:
--   - token_hash: we store SHA-256(raw_token), not the raw value.
--     If this table is leaked, attackers cannot use the hashes.
--   - revoked: set to true when used (rotation) or on logout.
--     This prevents token reuse even before expiry.
-- ============================================================

CREATE TABLE refresh_tokens (
    id          UUID                     PRIMARY KEY,
    user_id     UUID                     NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255)             NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN                  NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
