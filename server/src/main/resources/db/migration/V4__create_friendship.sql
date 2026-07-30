CREATE TABLE friendships
(
    id              UUID PRIMARY KEY,
    user_low_id     UUID        NOT NULL,
    user_high_id    UUID        NOT NULL,
    requested_by_id UUID        NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    accepted_at     TIMESTAMP,

    CONSTRAINT fk_friendships_user_low
        FOREIGN KEY (user_low_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_friendships_user_high
        FOREIGN KEY (user_high_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_friendships_requested_by
        FOREIGN KEY (requested_by_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_friendships_different_users
        CHECK (user_low_id <> user_high_id),

    CONSTRAINT chk_friendships_requester_is_participant
        CHECK (
            requested_by_id = user_low_id
            OR requested_by_id = user_high_id
        ),

    CONSTRAINT chk_friendships_status
        CHECK (
            status IN ('PENDING', 'ACCEPTED')
        ),

    CONSTRAINT uq_friendships_user_pair
        UNIQUE (user_low_id, user_high_id)
);

CREATE INDEX idx_friendships_user_low
    ON friendships (user_low_id);

CREATE INDEX idx_friendships_user_high
    ON friendships (user_high_id);

CREATE INDEX idx_friendships_requested_by
    ON friendships (requested_by_id);

CREATE INDEX idx_friendships_status
    ON friendships (status);