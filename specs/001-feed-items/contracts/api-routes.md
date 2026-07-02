# API Contracts: User Feed Items

**Phase 1 Outcome**: Complete REST API route definitions, request/response schemas, serialization rules, error handling.

## Base API Path

```
/api/v1/feed
```

All routes assume authentication via Bearer token in `Authorization` header.

---

## Route 1: Create Feed Item

### Endpoint

```
POST /api/v1/feed
```

### Request

**Headers**:
- `Authorization: Bearer {token}` (required)
- `Content-Type: application/json` (required)

**Body**:
```json
{
  "content": {
    "type": "markdown",
    "content": {
      "text": "My wellness update..."
    }
  },
  "privacy": "PUBLIC",
  "categories": ["MENTAL", "MINDFULNESS"]
}
```

**Request DTO** (in `core`):
```kotlin
@Serializable
data class CreateFeedItemRequest(
    val content: RichTextDocument,
    val privacy: PrivacyStatus,
    val categories: List<FeedCategory> = emptyList(),
    val mediaAssets: List<Uuid> = emptyList()  // UUID media asset IDs
)
```

### Response

**Status**: `201 Created`

**Headers**:
- `Content-Type: application/json`
- `Location: /api/v1/feed/{uuid}` (link to created resource)

**Body**:
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "author": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "username": "alice",
    "avatar": "https://cdn.example.com/avatar/user-123.jpg"
  },
  "content": {
    "type": "markdown",
    "content": {
      "text": "My wellness update..."
    }
  },
  "privacy": "PUBLIC",
  "categories": ["MENTAL", "MINDFULNESS"],
  "mediaAssets": [],
  "createdAt": "2026-03-19T10:30:00Z",
  "updatedAt": "2026-03-19T10:30:00Z",
  "deletedAt": null
}
```

### Error Responses

| Status | Code | Message | Reason |
|--------|------|---------|--------|
| 400 | INVALID_CONTENT | Content is required and cannot be empty | Validation failed |
| 400 | CONTENT_TOO_LARGE | Content exceeds maximum size (10 MB) | Payload too large |
| 400 | INVALID_PRIVACY | Privacy must be one of: PUBLIC, FRIENDS_ONLY, PRIVATE | Invalid enum |
| 400 | INVALID_CATEGORIES | Categories must be valid enum values | Invalid enum values |
| 401 | UNAUTHORIZED | Authentication token missing or invalid | Not authenticated |
| 500 | INTERNAL_ERROR | An unexpected error occurred | Server error (no details leaked) |

---

## Route 2: Get Feed Item by UUID

### Endpoint

```
GET /api/v1/feed/{uuid}
```

### Request

**Parameters**:
- `uuid` (path): Feed item UUID (required)

**Headers**:
- `Authorization: Bearer {token}` (required)

### Response

**Status**: `200 OK`

**Body**: (same as Route 1 response)

### Error Responses

| Status | Code | Message | Reason |
|--------|------|---------|--------|
| 401 | UNAUTHORIZED | Not authenticated | Missing/invalid token |
| 403 | FORBIDDEN | You do not have permission to view this feed item | Privacy violation |
| 404 | NOT_FOUND | Feed item not found | UUID doesn't exist or deleted |

---

## Route 3: Get User's Feed (Paginated)

### Endpoint

```
GET /api/v1/feed/me
```

### Request

**Query Parameters**:
- `limit` (optional, default: 20, max: 100): Number of items to return
- `offset` (optional, default: 0): Number of items to skip

**Headers**:
- `Authorization: Bearer {token}` (required)

### Response

**Status**: `200 OK`

**Body**:
```json
{
  "items": [
    {
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "author": { ... },
      "content": { ... },
      "privacy": "PUBLIC",
      "categories": ["MENTAL"],
      "mediaAssets": [],
      "createdAt": "2026-03-19T10:30:00Z",
      "updatedAt": "2026-03-19T10:30:00Z",
      "deletedAt": null
    }
  ],
  "total": 42,
  "limit": 20,
  "offset": 0,
  "hasMore": true
}
```

**Response DTO**:
```kotlin
@Serializable
data class FeedItemsPageResponse(
    val items: List<FeedItem>,
    val total: Long,
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean
)
```

### Error Responses

| Status | Code | Message | Reason |
|--------|------|---------|--------|
| 400 | INVALID_PAGINATION | Limit must be between 1 and 100 | Invalid params |
| 401 | UNAUTHORIZED | Not authenticated | Missing token |

---

## Route 4: Get General Feed (Paginated, Privacy-Aware)

### Endpoint

```
GET /api/v1/feed
```

### Request

**Query Parameters**:
- `limit` (optional, default: 20, max: 100)
- `offset` (optional, default: 0)
- `categories` (optional, comma-separated): Filter by FeedCategory values

**Headers**:
- `Authorization: Bearer {token}` (required)

### Response

**Status**: `200 OK`

**Body**: (same as Route 3)

**Behavior**:
- Returns PUBLIC items from all users
- Returns FRIENDS_ONLY items where current user is in author's friend list
- Returns PRIVATE items only if current user is the author
- Excludes all items where deletedAt is non-null
- Results ordered by createdAt DESC (most recent first)

### Error Responses

Same as Route 3.

---

## Route 5: Update Feed Item

### Endpoint

```
PATCH /api/v1/feed/{uuid}
```

### Request

**Parameters**:
- `uuid` (path): Feed item UUID (required)

**Headers**:
- `Authorization: Bearer {token}` (required)
- `Content-Type: application/json` (required)

**Body** (partial update):
```json
{
  "content": {
    "type": "markdown",
    "content": {
      "text": "Updated wellness update..."
    }
  },
  "privacy": "FRIENDS_ONLY",
  "categories": ["PHYSICAL", "ENERGY"]
}
```

**Request DTO**:
```kotlin
@Serializable
data class UpdateFeedItemRequest(
    val content: RichTextDocument? = null,
    val privacy: PrivacyStatus? = null,
    val categories: List<FeedCategory>? = null,
    val mediaAssets: List<Uuid>? = null
)
```

**Update Semantics**:
- Only non-null fields are updated
- createdAt and uuid remain immutable
- updatedAt auto-set to current timestamp
- author remains immutable

### Response

**Status**: `200 OK`

**Body**: Updated FeedItem (same as Route 1)

### Error Responses

| Status | Code | Message | Reason |
|--------|------|---------|--------|
| 400 | INVALID_CONTENT | Content update validation failed | Invalid content |
| 401 | UNAUTHORIZED | Not authenticated | Missing token |
| 403 | FORBIDDEN | You do not have permission to update this feed item | Not owner |
| 404 | NOT_FOUND | Feed item not found | UUID doesn't exist |

---

## Route 6: Delete Feed Item (Soft Delete)

### Endpoint

```
DELETE /api/v1/feed/{uuid}
```

### Request

**Parameters**:
- `uuid` (path): Feed item UUID (required)

**Headers**:
- `Authorization: Bearer {token}` (required)

**Body**: Empty

### Response

**Status**: `204 No Content`

**Body**: Empty

### Error Responses

| Status | Code | Message | Reason |
|--------|------|---------|--------|
| 401 | UNAUTHORIZED | Not authenticated | Missing token |
| 403 | FORBIDDEN | You do not have permission to delete this feed item | Not owner |
| 404 | NOT_FOUND | Feed item not found | UUID doesn't exist |

---

## Error Response Format

All error responses follow this schema:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable message",
    "timestamp": "2026-03-19T10:30:00Z"
  }
}
```

**Error DTO** (in `core`):
```kotlin
@Serializable
data class ApiErrorResponse(
    val error: ApiError
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val timestamp: Instant
)
```

**Privacy Note**: Error responses MUST NOT include stack traces, internal database details, or schema information. Client receives only error code and generic message.

---

## Serialization Rules

### Persistence ID Contract

- Route path `uuid` parameters are UUID strings on the wire and parsed as `kotlin.uuid.Uuid`.
- Server persistence for this feature uses Exposed `UuidTable` only.
- All persisted identifiers and foreign keys are UUID-based (`Uuid`), including `feed_items.id`, `feed_item_categories.feed_item_id`, and `feed_item_media_assets.feed_item_id`.
- Numeric ID strategies (`IntIdTable`, `LongIdTable`, auto-increment IDs) are not allowed.

### JSON Format Standards

1. **UUID Fields**: Serialized as string (e.g., "550e8400-e29b-41d4-a716-446655440000")
   - Serializer: `UuidSerializer` in core module
   - Format: RFC 4122

2. **Timestamp Fields**: ISO-8601 format with UTC timezone (e.g., "2026-03-19T10:30:00Z")
   - Serializer: `InstantSerializer` in core module
   - Precision: Milliseconds (ignored on input if present; normalized on output)

3. **Enum Fields**: Serialized as string values (e.g., "PUBLIC", "MENTAL")
   - Not as numeric ordinals

4. **Null Fields**: Explicitly included in JSON (e.g., `"deletedAt": null`)
   - Exception: deletedAt omitted from response if null (API convenience)

5. **Unknown Fields**: Ignored on input (backward compatibility)
   - Future API fields added to response won't break clients

### Example Serialization

```kotlin
// Input (client sends):
val json = """
{
  "content": {
    "type": "markdown",
    "content": {"text": "Hello"}
  },
  "privacy": "PUBLIC",
  "categories": ["MENTAL"]
}
"""
val request = AppJson.decodeFromString<CreateFeedItemRequest>(json)

// Output (server responds):
val response = FeedItem(
    uuid = "550e8400-e29b-41d4-a716-446655440000",
    author = User(...),
    content = RichTextDocument(...),
    privacy = PrivacyStatus.PUBLIC,
    categories = listOf(FeedCategory.MENTAL),
    mediaAssets = emptyList(),
    createdAt = Instant.parse("2026-03-19T10:30:00Z"),
    updatedAt = Instant.parse("2026-03-19T10:30:00Z"),
    deletedAt = null
)
val responseJson = AppJson.encodeToString(response)
```

---

## Authentication & Authorization

### Authentication Flow

1. Client obtains Bearer token from `/api/v1/auth/login` (out of scope)
2. Client sends token in `Authorization: Bearer {token}` header
3. Server validates token, extracts user_id, passes to route handlers
4. Route handler uses user_id for authorization checks

### Authorization Rules

| Operation | Rule |
|-----------|------|
| Create | Authenticated user; author always set to current user |
| Read | Privacy rules enforced; visibility filter applied at query time |
| Update | User must be author (ownership check) |
| Delete | User must be author (ownership check) |

---

## Rate Limiting (Out of Scope MVP)

Current implementation has no rate limiting. Future enhancement can add token bucket or sliding window throttling.

---

## Next Steps

1. Generate quickstart.md with end-to-end validation scenarios
2. Update implementation plan with project structure and architectural decisions
3. Perform Constitution Check gates
