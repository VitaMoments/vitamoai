CREATE TABLE media_assets
(
    id              UUID PRIMARY KEY,
    owner_id        UUID        NOT NULL,
    media_type      VARCHAR(32) NOT NULL,
    purpose         VARCHAR(64) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    visibility      VARCHAR(32) NOT NULL,

    storage_key     VARCHAR(512) NOT NULL,
    mime_type       VARCHAR(128) NOT NULL,
    size_bytes      BIGINT       NOT NULL,

    width           INTEGER,
    height          INTEGER,
    sha256          VARCHAR(64) NOT NULL,

    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    deleted_at      TIMESTAMP,

    CONSTRAINT fk_media_assets_owner
        FOREIGN KEY (owner_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_media_assets_storage_key
        UNIQUE (storage_key),

    CONSTRAINT chk_media_assets_type
        CHECK (media_type IN ('IMAGE', 'VIDEO')),

    CONSTRAINT chk_media_assets_status
        CHECK (status IN ('PENDING', 'READY', 'FAILED', 'DELETED')),

    CONSTRAINT chk_media_assets_visibility
        CHECK (visibility IN (
            'PUBLIC',
            'AUTHENTICATED',
            'FRIENDS',
            'PRIVATE'
        )),

    CONSTRAINT chk_media_assets_size
        CHECK (size_bytes >= 0)
);

CREATE INDEX idx_media_assets_owner_id
    ON media_assets (owner_id);

CREATE INDEX idx_media_assets_status
    ON media_assets (status);

CREATE INDEX idx_media_assets_owner_status
    ON media_assets (owner_id, status);

ALTER TABLE users
    ADD COLUMN profile_image_id UUID;

CREATE INDEX idx_users_profile_image_id
    ON users (profile_image_id);