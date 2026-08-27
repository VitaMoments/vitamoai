-- -----------------------------------------------------------------------------
-- Feed items
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS feed_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,

    CONSTRAINT fk_feed_items_author
        FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS feed_items_author_id_idx
    ON feed_items (author_id);

CREATE INDEX IF NOT EXISTS feed_items_created_at_idx
    ON feed_items (created_at DESC);

CREATE INDEX IF NOT EXISTS feed_items_active_created_at_idx
    ON feed_items (created_at DESC)
    WHERE deleted_at IS NULL;


-- -----------------------------------------------------------------------------
-- Media assets
--
-- Media already exists since V1.
-- Feed attachments are linked directly to a FeedItem.
--
-- feed_item_id = NULL:
--   media is not attached to a feed item (for example PROFILE_IMAGE)
--
-- position:
--   controls ordering inside FeedContent.assets
-- -----------------------------------------------------------------------------

ALTER TABLE media_assets
    ADD COLUMN IF NOT EXISTS feed_item_id UUID;

ALTER TABLE media_assets
    ADD COLUMN IF NOT EXISTS position INTEGER;

ALTER TABLE media_assets
    ADD CONSTRAINT fk_media_assets_feed_item
        FOREIGN KEY (feed_item_id)
        REFERENCES feed_items (id)
        ON DELETE SET NULL;

ALTER TABLE media_assets
    ADD CONSTRAINT chk_media_assets_position
        CHECK (position IS NULL OR position >= 0);

CREATE INDEX IF NOT EXISTS media_assets_feed_item_id_idx
    ON media_assets (feed_item_id);

CREATE UNIQUE INDEX IF NOT EXISTS media_assets_feed_item_position_uidx
    ON media_assets (feed_item_id, position)
    WHERE feed_item_id IS NOT NULL
      AND position IS NOT NULL;


-- -----------------------------------------------------------------------------
-- Posts
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID NOT NULL,
    title VARCHAR(255),
    message_json TEXT,

    CONSTRAINT fk_posts_feed_item
        FOREIGN KEY (feed_item_id)
        REFERENCES feed_items (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_posts_feed_item
        UNIQUE (feed_item_id)
);

CREATE INDEX IF NOT EXISTS posts_feed_item_id_idx
    ON posts (feed_item_id);


-- -----------------------------------------------------------------------------
-- Feed item likes
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS feed_item_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at BIGINT NOT NULL,

    CONSTRAINT fk_feed_item_likes_feed_item
        FOREIGN KEY (feed_item_id)
        REFERENCES feed_items (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_feed_item_likes_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_feed_item_likes_feed_item_user
        UNIQUE (feed_item_id, user_id)
);

CREATE INDEX IF NOT EXISTS feed_item_likes_feed_item_id_idx
    ON feed_item_likes (feed_item_id);

CREATE INDEX IF NOT EXISTS feed_item_likes_user_id_idx
    ON feed_item_likes (user_id);


-- -----------------------------------------------------------------------------
-- Feed item reactions
--
-- parent_reaction_id IS NULL     -> FeedItemComment
-- parent_reaction_id IS NOT NULL -> FeedItemReply
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS feed_item_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID NOT NULL,
    parent_reaction_id UUID,
    author_id UUID NOT NULL,
    content_json TEXT NOT NULL,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    deleted_at BIGINT,

    CONSTRAINT fk_feed_item_reactions_feed_item
        FOREIGN KEY (feed_item_id)
        REFERENCES feed_items (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_feed_item_reactions_author
        FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_feed_item_reactions_not_self_parent
        CHECK (
            parent_reaction_id IS NULL
            OR parent_reaction_id <> id
        ),

    CONSTRAINT uq_feed_item_reactions_id_feed_item
        UNIQUE (id, feed_item_id),

    CONSTRAINT fk_feed_item_reactions_parent
        FOREIGN KEY (parent_reaction_id, feed_item_id)
        REFERENCES feed_item_reactions (id, feed_item_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS feed_item_reactions_feed_item_id_idx
    ON feed_item_reactions (feed_item_id);

CREATE INDEX IF NOT EXISTS feed_item_reactions_parent_reaction_id_idx
    ON feed_item_reactions (parent_reaction_id);

CREATE INDEX IF NOT EXISTS feed_item_reactions_author_id_idx
    ON feed_item_reactions (author_id);

CREATE INDEX IF NOT EXISTS feed_item_reactions_feed_parent_created_idx
    ON feed_item_reactions (
        feed_item_id,
        parent_reaction_id,
        created_at
    )
    WHERE deleted_at IS NULL;


-- -----------------------------------------------------------------------------
-- Feed item reaction likes
--
-- Likes for both FeedItemComment and FeedItemReply.
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS feed_item_reaction_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reaction_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at BIGINT NOT NULL,

    CONSTRAINT fk_feed_item_reaction_likes_reaction
        FOREIGN KEY (reaction_id)
        REFERENCES feed_item_reactions (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_feed_item_reaction_likes_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_feed_item_reaction_likes_reaction_user
        UNIQUE (reaction_id, user_id)
);

CREATE INDEX IF NOT EXISTS feed_item_reaction_likes_reaction_id_idx
    ON feed_item_reaction_likes (reaction_id);

CREATE INDEX IF NOT EXISTS feed_item_reaction_likes_user_id_idx
    ON feed_item_reaction_likes (user_id);