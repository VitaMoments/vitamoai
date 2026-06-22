CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    alias VARCHAR(100),
    bio VARCHAR(500),
    birth_date DATE,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    email_verified_at TIMESTAMPTZ,
    deleted_at BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_uidx ON users (email);
CREATE INDEX IF NOT EXISTS users_display_name_idx ON users (display_name);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    client_instance_id UUID,
    client_type VARCHAR(20),
    device_name VARCHAR(255),
    platform VARCHAR(20),
    browser_name VARCHAR(255),
    app_version VARCHAR(50),
    expired_at_epoch_seconds BIGINT NOT NULL,
    last_used_at_epoch_seconds BIGINT,
    revoked_at_epoch_seconds BIGINT,
    replaced_by_session_id UUID,
    created_at_epoch_seconds BIGINT NOT NULL,
    updated_at_epoch_seconds BIGINT NOT NULL,
    deleted_at_epoch_seconds BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS refresh_tokens_token_hash_uidx ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS refresh_tokens_user_id_idx ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS refresh_tokens_expired_at_idx ON refresh_tokens (expired_at_epoch_seconds);
CREATE INDEX IF NOT EXISTS refresh_tokens_revoked_at_idx ON refresh_tokens (revoked_at_epoch_seconds);

CREATE TABLE IF NOT EXISTS email_verification_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    attempts INT NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS email_verification_challenges_user_id_idx
    ON email_verification_challenges (user_id);
CREATE INDEX IF NOT EXISTS email_verification_challenges_email_idx
    ON email_verification_challenges (email);
CREATE INDEX IF NOT EXISTS email_verification_challenges_purpose_idx
    ON email_verification_challenges (purpose);
CREATE INDEX IF NOT EXISTS email_verification_challenges_expires_at_idx
    ON email_verification_challenges (expires_at);
CREATE INDEX IF NOT EXISTS email_verification_challenges_consumed_at_idx
    ON email_verification_challenges (consumed_at);

CREATE TABLE IF NOT EXISTS health_check (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO health_check (name)
VALUES ('init')
ON CONFLICT (name) DO NOTHING;
