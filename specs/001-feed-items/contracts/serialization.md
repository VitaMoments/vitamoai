# Serialization Contracts: Feed Items

**Purpose**: Specify the exact JSON schema and serialization behavior for all feed item models.

---

## Core Models (Serializable in `core` module)

All models in this section use `@Serializable` from `kotlinx.serialization`.

### BaseFeedItem

**Type**: Sealed interface

**JSON Schema** (conceptual):
```json
{
  "uuid": "string (UUID)",
  "author": { ... },
  "content": { ... },
  "privacy": "PUBLIC | FRIENDS_ONLY | PRIVATE",
  "createdAt": "string (ISO-8601)",
  "updatedAt": "string (ISO-8601)",
  "deletedAt": "string (ISO-8601) | null"
}
```

**Serialization**: Handled by `@Serializable` on concrete implementation `FeedItem`.

---

### FeedItem

**Type**: `@Serializable data class`

**Complete JSON Example**:
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "author": {
    "id": "user-123",
    "username": "alice",
    "avatar": "https://cdn.example.com/avatars/user-123.jpg"
  },
  "content": {
    "type": "markdown",
    "content": {
      "text": "# My Wellness Journey\n\nToday was a great day! I ran 5km...",
      "length": 50
    }
  },
  "privacy": "PUBLIC",
  "categories": ["MENTAL", "MINDFULNESS"],
  "mediaAssets": [
    {
      "id": "media-456",
      "url": "https://cdn.example.com/media/image-1.jpg",
      "type": "IMAGE",
      "metadata": {
        "width": 1920,
        "height": 1080,
        "size": 524288
      }
    }
  ],
  "createdAt": "2026-03-19T10:30:00Z",
  "updatedAt": "2026-03-19T10:30:00Z",
  "deletedAt": null
}
```

**Field Serialization**:
- `uuid`: String (UuidSerializer)
- `author`: Nested User object
- `content`: Nested RichTextDocument
- `privacy`: String enum value
- `categories`: Array of enum strings
- `mediaAssets`: Array of nested objects
- `createdAt`: String (InstantSerializer)
- `updatedAt`: String (InstantSerializer)
- `deletedAt`: String | null

---

### RichTextDocument

**Type**: `@Serializable data class`

**Valid Examples**:

**Markdown**:
```json
{
  "type": "markdown",
  "content": {
    "text": "# Heading\n\nParagraph with **bold** and *italic*."
  }
}
```

**Plaintext**:
```json
{
  "type": "plaintext",
  "content": {
    "text": "Just plain text content here."
  }
}
```

**Document (Generic JSON)**:
```json
{
  "type": "document",
  "content": {
    "blocks": [
      {
        "type": "heading",
        "level": 1,
        "text": "Title"
      },
      {
        "type": "paragraph",
        "text": "Content..."
      }
    ]
  }
}
```

**Schema Validation Rules**:
- `type`: Non-empty string; validated against whitelist (markdown, plaintext, document, html)
- `content`: JsonElement; must not be empty object `{}`
- Total serialized size: ≤ 10 MB
- Character count in all string fields: ≤ 500,000

---

### PrivacyStatus

**Type**: `@Serializable enum class`

**Enum Values**:
```json
"PUBLIC" | "FRIENDS_ONLY" | "PRIVATE"
```

**String Mapping**:
| Enum | String Value |
|------|--------------|
| PUBLIC | "PUBLIC" |
| FRIENDS_ONLY | "FRIENDS_ONLY" |
| PRIVATE | "PRIVATE" |

---

### FeedCategory

**Type**: `@Serializable enum class`

**Enum Values** (complete list):
```
"MENTAL"
"PHYSICAL"
"FOOD"
"LIFESTYLE"
"MINDFULNESS"
"HABITS"
"SLEEP"
"ENERGY"
"RELATIONSHIPS"
"COMMUNITY"
"PURPOSE"
"PERSONAL_GROWTH"
"REFLECTION"
```

**Serialization**: Each category serialized as its string value in the array.

**Example**:
```json
{
  "categories": ["MENTAL", "PHYSICAL", "SLEEP"]
}
```

---

### MediaAsset

**Type**: `@Serializable data class`

**JSON Example**:
```json
{
  "id": "media-456",
  "url": "https://cdn.example.com/media/image-1.jpg",
  "type": "IMAGE",
  "metadata": {
    "width": 1920,
    "height": 1080,
    "size": 524288,
    "mimeType": "image/jpeg"
  }
}
```

**Field Serialization**:
- `id`: UUID string on the wire, represented as `kotlin.uuid.Uuid` in Kotlin models
- `url`: String (HTTP/HTTPS URL)
- `type`: String enum (IMAGE, VIDEO, DOCUMENT)
- `metadata`: JsonElement | null (optional; flexible schema)

---

### MediaAssetType

**Type**: `@Serializable enum class`

**Enum Values**:
```
"IMAGE"
"VIDEO"
"DOCUMENT"
```

---

### User

**Type**: `@Serializable data class` (existing in core)

**JSON Example**:
```json
{
  "id": "user-123",
  "username": "alice",
  "avatar": "https://cdn.example.com/avatars/user-123.jpg",
  "email": null
}
```

**Field Serialization**:
- `id`: UUID string on the wire, represented as `kotlin.uuid.Uuid` in Kotlin models
- `username`: String
- `avatar`: String | null (URL or null)
- `email`: String | null (OMITTED from feed item responses for privacy)

---

## Request/Response DTOs

### CreateFeedItemRequest

**JSON Example**:
```json
{
  "content": {
    "type": "markdown",
    "content": {
      "text": "My wellness update..."
    }
  },
  "privacy": "PUBLIC",
  "categories": ["MENTAL", "MINDFULNESS"],
  "mediaAssets": []
}
```

**Required Fields**: content, privacy

**Optional Fields**: categories (default []), mediaAssets (default [])

---

### UpdateFeedItemRequest

**JSON Example** (partial update):
```json
{
  "content": {
    "type": "markdown",
    "content": {
      "text": "Updated content..."
    }
  }
}
```

or

```json
{
  "privacy": "PRIVATE",
  "categories": ["REFLECTION"]
}
```

**Required Fields**: None (partial update supported)

**Optional Fields**: content, privacy, categories, mediaAssets

**Semantics**: Null fields are ignored; only provided fields are updated.

---

### FeedItemsPageResponse

**JSON Example**:
```json
{
  "items": [
    { ... FeedItem object ... },
    { ... FeedItem object ... }
  ],
  "total": 42,
  "limit": 20,
  "offset": 0,
  "hasMore": true
}
```

**Field Serialization**:
- `items`: Array of FeedItem objects
- `total`: Number (long)
- `limit`: Number (int)
- `offset`: Number (int)
- `hasMore`: Boolean

---

### ApiErrorResponse

**JSON Example**:
```json
{
  "error": {
    "code": "INVALID_CONTENT",
    "message": "Content cannot be empty",
    "timestamp": "2026-03-19T10:30:00Z"
  }
}
```

**Field Serialization**:
- `error.code`: String (error code enum)
- `error.message`: String (human-readable)
- `error.timestamp`: String (ISO-8601)

---

## Serialization Configuration

### Persistence + Migration Notes (ID Strategy)

- Database schema for this feature uses Exposed `UuidTable` for every table.
- Primary keys are `kotlin.uuid.Uuid`, serialized as RFC-4122 UUID strings in API payloads.
- Foreign keys are UUID-based (`feed_item_id`, `author_id`, `media_asset_id`) and reference UUID primary keys.
- Migrations must create UUID PK/FK columns and must not introduce numeric auto-increment identifiers.

### AppJson Instance (in `core`)

**File**: `core/src/commonMain/kotlin/eu/vitamo/app/serialization/AppJson.kt`

**Configuration**:
```kotlin
val AppJson = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
    classDiscriminator = "type"
    coerceInputValues = true
    isLenient = false
}
```

**Rationale**:
- `encodeDefaults = false`: Don't emit null or default values (compact JSON)
- `ignoreUnknownKeys = true`: Backward compatibility (server adds fields; old clients ignore)
- `classDiscriminator = "type"`: For sealed class discrimination if needed
- `coerceInputValues = true`: Attempt coercion (e.g., "123" string → 123 number if expected)
- `isLenient = false`: Strict parsing (reject malformed JSON)

### Custom Serializers

**UuidSerializer** (existing): Converts UUID to/from string (RFC 4122)

**InstantSerializer** (existing): Converts Instant to/from ISO-8601 string with UTC timezone

---

## Backward Compatibility

### API Versioning Strategy

All routes include version in path: `/api/v1/feed`

**Forward Compatibility**:
1. New fields added to responses: Clients ignore unknown keys
2. New optional fields added to requests: Old clients omit; new clients include
3. Enum values extended: Old clients ignore new values; new values serialize as string

**Breaking Changes** (require version bump to v2):
1. Removing a field from response
2. Renaming a field
3. Changing field type (e.g., string → object)
4. Making previously optional field required

---

## Validation Rules by Field

### content

**Type**: RichTextDocument

**Validation**:
- `type`: Non-empty, must be one of (markdown, plaintext, document, html)
- `content`: Must not be empty JsonElement
- Total size: ≤ 10 MB
- Character count: ≤ 500,000
- Characters: Must be valid UTF-8

**Error Response**:
```json
{
  "error": {
    "code": "INVALID_CONTENT",
    "message": "Content must be between 1 and 500000 characters",
    "timestamp": "2026-03-19T10:30:00Z"
  }
}
```

### privacy

**Type**: PrivacyStatus (enum)

**Validation**: Must be one of (PUBLIC, FRIENDS_ONLY, PRIVATE)

**Error Response**:
```json
{
  "error": {
    "code": "INVALID_PRIVACY",
    "message": "Privacy must be one of: PUBLIC, FRIENDS_ONLY, PRIVATE",
    "timestamp": "2026-03-19T10:30:00Z"
  }
}
```

### categories

**Type**: List[FeedCategory]

**Validation**:
- Each category must be valid enum value
- No duplicates allowed
- Maximum 13 categories per item

**Error Response**:
```json
{
  "error": {
    "code": "INVALID_CATEGORIES",
    "message": "Categories contain invalid values or duplicates",
    "timestamp": "2026-03-19T10:30:00Z"
  }
}
```

### mediaAssets

**Type**: List[MediaAsset]

**Validation**:
- Each asset must have valid id (non-empty string)
- Each asset must have valid url (HTTP/HTTPS)
- Each asset must have valid type (IMAGE, VIDEO, DOCUMENT)
- No duplicates by id

**Error Response**:
```json
{
  "error": {
    "code": "INVALID_MEDIA_ASSETS",
    "message": "One or more media assets are invalid",
    "timestamp": "2026-03-19T10:30:00Z"
  }
}
```

---

## Next Steps

1. Implement serialization in core module
2. Implement API routes in server module
3. Implement repository/usecase in app/shared and server
4. Add serialization tests in commonTest and server test suites
