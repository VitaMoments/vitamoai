CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
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
