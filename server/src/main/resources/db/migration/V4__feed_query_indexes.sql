CREATE INDEX IF NOT EXISTS idx_feed_items_author_created_at
    ON feed_items(author_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_feed_items_author_deleted_at
    ON feed_items(author_id, deleted_at);

CREATE INDEX IF NOT EXISTS idx_feed_items_privacy_created_at
    ON feed_items(privacy, created_at DESC);
