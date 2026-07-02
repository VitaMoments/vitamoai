CREATE TABLE IF NOT EXISTS feed_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content_json TEXT NOT NULL,
    privacy VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS feed_item_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID NOT NULL REFERENCES feed_items (id) ON DELETE CASCADE,
    category VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS feed_item_media_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feed_item_id UUID NOT NULL REFERENCES feed_items (id) ON DELETE CASCADE,
    media_asset_id UUID NOT NULL,
    media_asset_url TEXT NOT NULL,
    media_asset_type VARCHAR(32) NOT NULL,
    media_asset_metadata TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_feed_item_categories_item_category
    ON feed_item_categories(feed_item_id, category);
CREATE INDEX IF NOT EXISTS idx_feed_item_categories_feed_item
    ON feed_item_categories(feed_item_id);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_feed_item_media_assets_item_asset
    ON feed_item_media_assets(feed_item_id, media_asset_id);
CREATE INDEX IF NOT EXISTS idx_feed_item_media_assets_feed_item
    ON feed_item_media_assets(feed_item_id);
