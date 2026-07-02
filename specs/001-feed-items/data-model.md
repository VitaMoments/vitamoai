# Data Model: User Feed Items

**Phase 1 Outcome**: Complete entity definitions, relationships, validation rules, and state transitions.

## Entity Hierarchy

### 1. BaseFeedItem (Sealed Interface - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/BaseFeedItem.kt`

**Purpose**: Canonical contract for feed item serialization across platforms and server-client boundaries.

**Definition**:
```kotlin
@Serializable
sealed interface BaseFeedItem {
    val uuid: Uuid                   // @Serializable(with = UuidSerializer::class)
    val author: User                 // Embedded user object
    val content: RichTextDocument    
    val privacy: PrivacyStatus       
    val createdAt: Instant           // @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant           // @Serializable(with = InstantSerializer::class)
    val deletedAt: Instant?          // Null means not deleted; non-null = deletion timestamp
}
```

**Immutability**: All fields are `val` (read-only); no setters. Modifications create new instances.

**Serialization**: Uses `@Serializable` with custom serializers for UUID and Instant (already defined in core).

---

### 2. FeedItem (Data Class - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedItem.kt`

**Purpose**: Concrete implementation of `BaseFeedItem`; represents a feed item in API responses and app state.

**Definition**:
```kotlin
@Serializable
data class FeedItem(
    override val uuid: Uuid,
    override val author: User,
    override val content: RichTextDocument,
    override val privacy: PrivacyStatus,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    override val deletedAt: Instant? = null,
    val categories: List<FeedCategory> = emptyList(),
    val mediaAssets: List<MediaAsset> = emptyList()
) : BaseFeedItem
```

**Relationships**:
- `author`: Foreign key relationship to User (embedded in API response, not normalized)
- `categories`: One-to-many (feed item has 0+ categories)
- `mediaAssets`: One-to-many (feed item has 0+ media references)

**Constraints**:
- `uuid`: Globally unique, immutable after creation (`kotlin.uuid.Uuid`, UUID v4 format)
- `content`: Must pass RichTextDocument validation (non-null, structure verified)
- `privacy`: One of {PUBLIC, FRIENDS_ONLY, PRIVATE}
- `createdAt` < `updatedAt` (updatedAt ≥ createdAt always)
- `deletedAt`: Null OR timestamp ≥ createdAt (deletion cannot predate creation)
- `categories`: 0-13 distinct FeedCategory values (no duplicates)
- `mediaAssets`: 0+ items; each must have valid MediaAsset structure

---

### 3. RichTextDocument (Data Class - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/RichTextDocument.kt`

**Purpose**: Flexible, schema-agnostic content representation.

**Definition**:
```kotlin
@Serializable
data class RichTextDocument(
    val type: String,           // e.g., "markdown", "plaintext", "document", "html"
    val content: JsonElement    // Serialized document structure; flexible schema
)
```

**Validation Rules**:
- `type`: Non-empty string; validated against allowed types (markdown, plaintext, document, html)
- `content`: Not empty (JsonElement.isEmpty() fails validation)
- Total size: ≤ 10 MB (enforced server-side on creation/update)
- Character limit: ≤ 500,000 characters (prevents DOS)

**Examples**:
```json
{
  "type": "markdown",
  "content": {
    "text": "# My wellness update\n\nFeeling great today after a 5km run!"
  }
}
```

---

### 4. PrivacyStatus (Enum - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/PrivacyStatus.kt`

**Purpose**: Controls visibility and access permissions for feed items.

**Definition**:
```kotlin
@Serializable
enum class PrivacyStatus(val value: String) {
    PUBLIC("PUBLIC"),                 // Visible to all authenticated users
    FRIENDS_ONLY("FRIENDS_ONLY"),     // Visible to author's friends only
    PRIVATE("PRIVATE")                // Visible to author only
}
```

**Visibility Rules**:
| Privacy | Owner | Friends | Other Users |
|---------|-------|---------|-------------|
| PUBLIC | ✓ | ✓ | ✓ |
| FRIENDS_ONLY | ✓ | ✓ | ✗ |
| PRIVATE | ✓ | ✗ | ✗ |

**Implementation Note**: Enforcement happens at query time (Exposed DSL filter), not at model layer.

---

### 5. FeedCategory (Enum - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedCategory.kt`

**Purpose**: Categorical tagging for wellness domain organization.

**Definition**:
```kotlin
@Serializable
enum class FeedCategory(val value: String) {
    MENTAL("MENTAL"),
    PHYSICAL("PHYSICAL"),
    FOOD("FOOD"),
    LIFESTYLE("LIFESTYLE"),
    MINDFULNESS("MINDFULNESS"),
    HABITS("HABITS"),
    SLEEP("SLEEP"),
    ENERGY("ENERGY"),
    RELATIONSHIPS("RELATIONSHIPS"),
    COMMUNITY("COMMUNITY"),
    PURPOSE("PURPOSE"),
    PERSONAL_GROWTH("PERSONAL_GROWTH"),
    REFLECTION("REFLECTION")
}
```

**Assignment**: Each feed item can have 0-13 categories (typically 1-3 for MVP).

---

### 6. MediaAsset (Data Class - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/MediaAsset.kt`

**Purpose**: Reference to attached media (images, videos, documents).

**Definition**:
```kotlin
@Serializable
data class MediaAsset(
    val id: Uuid,                      // UUID; references external media service
    val url: String,                   // CDN or storage URL
    val type: MediaAssetType,          // IMAGE, VIDEO, DOCUMENT
    val metadata: JsonElement? = null  // Optional: size, dimensions, mime type, etc.
)
```

**Enum - MediaAssetType**:
```kotlin
@Serializable
enum class MediaAssetType(val value: String) {
    IMAGE("IMAGE"),
    VIDEO("VIDEO"),
    DOCUMENT("DOCUMENT")
}
```

**Constraints**:
- `id`: Uuid
- `url`: Valid HTTP/HTTPS URL
- `type`: One of {IMAGE, VIDEO, DOCUMENT}
- Association: Feed item references 0+ media assets; NOT owned by feed item (references only)

**Out of Scope**: Asset upload, storage, deletion. This feature treats media as immutable references.

---

### 7. User (Embedded - Core Module)

**Location**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/user/User.kt` (existing)

**Purpose**: Author information embedded in FeedItem responses.

**Definition** (stub):
```kotlin
@Serializable
data class User(
    val id: Uuid,             // UUID
    val username: String,
    val avatar: String? = null, // URL or null
    val email: String? = null    // Null in public API (privacy)
)
```

**Privacy Note**: Email and sensitive fields omitted from public feed items. Only id, username, avatar returned.

---

## Server-Side Models (Server Module - Internal)

**Persistence Rule (Mandatory)**:
- Every database table in this feature inherits Exposed `UuidTable`
- Primary key type is `kotlin.uuid.Uuid`
- Foreign keys are UUID-to-UUID references
- `IntIdTable`, `LongIdTable`, and auto-increment IDs are prohibited

### 8. FeedItemEntity (Exposed Table)

**Location**: `server/src/main/kotlin/eu/vitamo/app/database/entities/FeedItemEntity.kt`

**Purpose**: Internal persistence model; maps to PostgreSQL `feed_items` table.

**Definition** (pseudo-code):
```kotlin
object FeedItems : UuidTable("feed_items") {
    val authorId = uuid("author_id").references(Users.id)
    val contentJson = text("content_json")  // Stored as TEXT; parsed as JSONB
    val privacy = varchar("privacy", 50)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
    
    index(authorId, createdAt)
    index(authorId, deletedAt)  // Fast filtering of deleted items
}
```

**Mapping to FeedItem**:
- Fetch from table; deserialize contentJson using AppJson
- Fetch categories from `feed_item_categories` junction table
- Fetch media assets from `feed_item_media_assets` junction table
- Construct User object from Users table (author_id FK)
- Return FeedItem instance for API response

### 9. FeedItemCategoryEntity (Exposed UuidTable - Junction)

**Location**: `server/src/main/kotlin/eu/vitamo/app/database/entities/FeedItemCategoryEntity.kt`

**Purpose**: Feed-to-category mapping with UUID primary key and UUID foreign key to feed items.

**Definition**:
```kotlin
object FeedItemCategories : UuidTable("feed_item_categories") {
    val feedItemId = uuid("feed_item_id").references(FeedItems.id)
    val category = varchar("category", 50)

    uniqueIndex(feedItemId, category)
    index(feedItemId)
}
```

### 10. FeedItemMediaAssetEntity (Exposed UuidTable - Junction)

**Location**: `server/src/main/kotlin/eu/vitamo/app/database/entities/FeedItemMediaAssetEntity.kt`

**Purpose**: Feed-to-media mapping with UUID primary key and UUID foreign key to feed items.

**Definition**:
```kotlin
object FeedItemMediaAssets : UuidTable("feed_item_media_assets") {
    val feedItemId = uuid("feed_item_id").references(FeedItems.id)
    val mediaAssetId = uuid("media_asset_id")  // Reference; no FK (external service)

    uniqueIndex(feedItemId, mediaAssetId)
    index(feedItemId)
}
```

---

## State Transitions

### Feed Item Lifecycle

```
Creation
   ↓
[ACTIVE] ← updatedAt advances on edits
   ↓
Deletion (user clicks delete)
   ↓
[SOFT_DELETED] (deletedAt = now; item excluded from queries)
   ↓
Optional: Hard Delete (admin batch job; out of scope for MVP)
```

**State Rules**:
- `ACTIVE`: deletedAt is null; visible in queries (subject to privacy rules)
- `SOFT_DELETED`: deletedAt is non-null; excluded from all user-facing queries
- Transition from ACTIVE → SOFT_DELETED: One-way (no automatic recovery)
- Transition from SOFT_DELETED → ACTIVE: Manual admin operation only (not MVP)

---

## Validation Rules Summary

### Creation Validation

| Field | Rule | Error |
|-------|------|-------|
| content | RichTextDocument.content not empty | "Content cannot be empty" |
| content | Total size ≤ 10 MB | "Content too large" |
| content | Character count ≤ 500k | "Content exceeds character limit" |
| privacy | One of {PUBLIC, FRIENDS_ONLY, PRIVATE} | "Invalid privacy level" |
| categories | ≤ 13 distinct values | "Too many categories" |
| mediaAssets | Each valid MediaAsset | "Invalid media asset" |

### Update Validation

Same as creation (content, privacy, categories, mediaAssets), except:
- uuid, createdAt, author remain immutable
- deletedAt cannot be updated (use DELETE endpoint)
- updatedAt auto-set to current timestamp

### Deletion Validation

- User must own the item (authorization check)
- deletedAt set to current timestamp
- No modification of other fields

---

## Relationships Diagram

```
User (id, username, ...)
  ↑
  │ (1:N) author
  │
FeedItem (uuid, content, privacy, createdAt, updatedAt, deletedAt)
  ├─ (1:N) mediaAssets via FeedItemMediaAssets junction
  │
  └─ (1:N) categories via FeedItemCategories junction
      ↓
      FeedCategory (enum: MENTAL, PHYSICAL, ...)
```

---

## Database Indexes

**Performance Critical**:
1. `feed_items(author_id, created_at DESC)` - User's own feed (most recent first)
2. `feed_items(author_id, deleted_at)` - Efficient filtering of deleted items
3. `feed_items(id)` - UUID primary key (already indexed)
4. `feed_item_categories(feed_item_id)` - Junction table lookup
5. `feed_item_media_assets(feed_item_id)` - Junction table lookup

**Optional (Future)**:
- Full-text search index on `content_json` (for content search feature)
- Composite index on `(privacy, created_at DESC)` (for general feed discovery)

---

## Migration Strategy

**Flyway Versioning**: Migrations placed in `server/src/main/resources/db/migration/`

**V001__CreateFeedItemsSchema.sql**:
- Creates `feed_items` table with `id UUID PRIMARY KEY` and UUID foreign keys
- Creates `feed_item_categories` table with `id UUID PRIMARY KEY` and `feed_item_id UUID REFERENCES feed_items(id)`
- Creates `feed_item_media_assets` table with `id UUID PRIMARY KEY` and `feed_item_id UUID REFERENCES feed_items(id)`
- Explicitly avoids any numeric auto-increment identifier columns

**Rollback Considerations**:
- Soft-delete only; no data loss on rollback (can drop tables if needed)
- No cascading deletes; manual cleanup if items orphaned

---

## Next Steps

1. Generate API contracts (routes, request/response DTOs)
2. Generate quickstart validation scenarios
3. Update implementation plan with technical decisions
4. Gate check against Constitution (Boundary Integrity, Shared Canonicality, etc.)
