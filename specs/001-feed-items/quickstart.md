# Quickstart: User Feed Items Feature

**Phase 1 Outcome**: Runnable validation scenarios proving the feature works end-to-end.

## Implementation validation notes (2026-07-01)

- Implemented routes are available at `/api/v1/feed` on the Ktor server.
- Authentication is enforced by the existing server cookie JWT setup (`access_token` cookie).
- Feed persistence uses UUID-only tables (`feed_items`, `feed_item_categories`, `feed_item_media_assets`) via Flyway V3 migration.
- Run validation with:
  - `./gradlew :core:test`
  - `./gradlew :server:test`

---

## Prerequisites

1. **Server Running**: Ktor server running on `http://localhost:8080`
2. **Database Ready**: PostgreSQL with migrations applied (`Flyway` migrations run)
   - Verify feed tables are UUID-based (no numeric PK): `\d feed_items`, `\d feed_item_categories`, `\d feed_item_media_assets`
3. **Authentication**: Two test users created with IDs:
   - User A: `550e8400-e29b-41d4-a716-446655440000` (username: alice)
   - User B: `550e8400-e29b-41d4-a716-446655440001` (username: bob)
4. **Friendship**: Alice and Bob are friends (required for FRIENDS_ONLY tests)
5. **Bearer Tokens**: Obtained from `/api/v1/auth/login` endpoint
6. **Secrets**: Server secrets loaded from environment variables / local `.env` (never hardcoded)

### Quick Setup Commands

```bash
# Build and migrate database
./gradlew server:build
./gradlew server:run

# In another terminal, verify database
psql vitamo_dev -c "SELECT COUNT(*) FROM feed_items;"
psql vitamo_dev -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name IN ('feed_items','feed_item_categories','feed_item_media_assets') AND column_name IN ('id','feed_item_id','author_id');"

# Obtain tokens for test users
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", "password": "test123"}' \
  | jq '.token'
```

---

## Test Scenario 1: Create Feed Item (PUBLIC)

**Story**: Alice creates a PUBLIC feed item visible to all users.

### Request

```bash
TOKEN_ALICE="<token_from_login>"

curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {
      "type": "markdown",
      "content": {
        "text": "# My Wellness Journey\n\nToday was a great day! I completed my morning run."
      }
    },
    "privacy": "PUBLIC",
    "categories": ["MENTAL", "PHYSICAL"]
  }'
```

### Expected Response

**Status**: `201 Created`

```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440100",
  "author": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "alice",
    "avatar": "https://cdn.example.com/avatars/alice.jpg"
  },
  "content": {
    "type": "markdown",
    "content": {
      "text": "# My Wellness Journey\n\nToday was a great day! I completed my morning run."
    }
  },
  "privacy": "PUBLIC",
  "categories": ["MENTAL", "PHYSICAL"],
  "mediaAssets": [],
  "createdAt": "2026-03-19T10:30:00Z",
  "updatedAt": "2026-03-19T10:30:00Z",
  "deletedAt": null
}
```

### Validation

- [ ] Status is `201 Created`
- [ ] Response includes generated `uuid`
- [ ] `author` matches Alice's user ID
- [ ] `createdAt` and `updatedAt` are identical (creation time)
- [ ] `deletedAt` is `null`
- [ ] `privacy` is `PUBLIC`
- [ ] `categories` matches input

---

## Test Scenario 2: Bob Reads Alice's PUBLIC Item

**Story**: Bob queries the general feed and sees Alice's PUBLIC item.

### Request

```bash
TOKEN_BOB="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

curl -X GET http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_BOB" \
  -H "Content-Type: application/json"
```

### Expected Response

**Status**: `200 OK`

```json
{
  "items": [
    {
      "uuid": "550e8400-e29b-41d4-a716-446655440100",
      "author": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "username": "alice",
        "avatar": "https://cdn.example.com/avatars/alice.jpg"
      },
      "content": { ... },
      "privacy": "PUBLIC",
      "categories": ["MENTAL", "PHYSICAL"],
      "mediaAssets": [],
      "createdAt": "2026-03-19T10:30:00Z",
      "updatedAt": "2026-03-19T10:30:00Z",
      "deletedAt": null
    }
  ],
  "total": 1,
  "limit": 20,
  "offset": 0,
  "hasMore": false
}
```

### Validation

- [ ] Status is `200 OK`
- [ ] Items array includes Alice's PUBLIC feed item
- [ ] Total count reflects actual items
- [ ] Pagination fields (limit, offset, hasMore) are correct

---

## Test Scenario 3: Alice Updates Her Feed Item

**Story**: Alice updates the content and privacy level of her feed item.

### Request

```bash
TOKEN_ALICE="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

curl -X PATCH http://localhost:8080/api/v1/feed/$ALICE_UUID \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {
      "type": "markdown",
      "content": {
        "text": "# My Wellness Journey\n\nUpdated: Great day with 10km run and meditation!"
      }
    },
    "privacy": "FRIENDS_ONLY"
  }'
```

### Expected Response

**Status**: `200 OK`

```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440100",
  "author": { ... },
  "content": {
    "type": "markdown",
    "content": {
      "text": "# My Wellness Journey\n\nUpdated: Great day with 10km run and meditation!"
    }
  },
  "privacy": "FRIENDS_ONLY",
  "categories": ["MENTAL", "PHYSICAL"],
  "mediaAssets": [],
  "createdAt": "2026-03-19T10:30:00Z",
  "updatedAt": "2026-03-19T10:31:00Z",
  "deletedAt": null
}
```

### Validation

- [ ] Status is `200 OK`
- [ ] Content is updated
- [ ] Privacy is updated to `FRIENDS_ONLY`
- [ ] `uuid` and `author` unchanged
- [ ] `createdAt` unchanged
- [ ] `updatedAt` advanced to new timestamp
- [ ] `deletedAt` remains `null`

---

## Test Scenario 4: Privacy Enforcement - FRIENDS_ONLY

**Story**: Bob (friend of Alice) can see the FRIENDS_ONLY item. A non-friend user cannot.

### Request (Bob - Friend)

```bash
TOKEN_BOB="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

curl -X GET http://localhost:8080/api/v1/feed/$ALICE_UUID \
  -H "Authorization: Bearer $TOKEN_BOB"
```

### Expected Response (Bob - Friend)

**Status**: `200 OK`

```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440100",
  "author": { ... },
  "content": { ... },
  "privacy": "FRIENDS_ONLY",
  "categories": ["MENTAL", "PHYSICAL"],
  "mediaAssets": [],
  "createdAt": "2026-03-19T10:30:00Z",
  "updatedAt": "2026-03-19T10:31:00Z",
  "deletedAt": null
}
```

### Request (Charlie - Non-Friend)

```bash
TOKEN_CHARLIE="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

curl -X GET http://localhost:8080/api/v1/feed/$ALICE_UUID \
  -H "Authorization: Bearer $TOKEN_CHARLIE"
```

### Expected Response (Charlie - Non-Friend)

**Status**: `403 Forbidden`

```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "You do not have permission to view this feed item",
    "timestamp": "2026-03-19T10:31:30Z"
  }
}
```

### Validation

- [ ] Bob (friend) can access the FRIENDS_ONLY item
- [ ] Charlie (non-friend) receives `403 Forbidden`
- [ ] Error message is generic (no schema details leaked)

---

## Test Scenario 5: Authorization - Non-Owner Cannot Update

**Story**: Bob attempts to update Alice's feed item and is rejected.

### Request

```bash
TOKEN_BOB="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

curl -X PATCH http://localhost:8080/api/v1/feed/$ALICE_UUID \
  -H "Authorization: Bearer $TOKEN_BOB" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {
      "type": "markdown",
      "content": {
        "text": "Hacked by Bob!"
      }
    }
  }'
```

### Expected Response

**Status**: `403 Forbidden`

```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "You do not have permission to update this feed item",
    "timestamp": "2026-03-19T10:32:00Z"
  }
}
```

### Validation

- [ ] Non-owner receives `403 Forbidden`
- [ ] Item remains unchanged
- [ ] Error message does not expose internal details

---

## Test Scenario 6: Soft Delete

**Story**: Alice deletes her feed item; it's marked with `deletedAt` and excluded from queries.

### Request

```bash
TOKEN_ALICE="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

curl -X DELETE http://localhost:8080/api/v1/feed/$ALICE_UUID \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### Expected Response

**Status**: `204 No Content`

```
(empty body)
```

### Follow-up: Verify Item Is Hidden

```bash
TOKEN_BOB="<token_from_login>"
ALICE_UUID="550e8400-e29b-41d4-a716-446655440100"

# Bob tries to access deleted item
curl -X GET http://localhost:8080/api/v1/feed/$ALICE_UUID \
  -H "Authorization: Bearer $TOKEN_BOB"
```

**Expected**: `404 Not Found`

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Feed item not found",
    "timestamp": "2026-03-19T10:32:30Z"
  }
}
```

### Validation

- [ ] Delete request returns `204 No Content`
- [ ] Item no longer appears in general feed
- [ ] Item no longer appears in author's own feed
- [ ] Direct access by any user returns `404 Not Found`

---

## Test Scenario 7: Pagination

**Story**: Alice creates 25 feed items; querying with limit=10 returns paginated results.

### Setup

```bash
TOKEN_ALICE="<token_from_login>"

for i in {1..25}; do
  curl -X POST http://localhost:8080/api/v1/feed \
    -H "Authorization: Bearer $TOKEN_ALICE" \
    -H "Content-Type: application/json" \
    -d "{
      \"content\": {
        \"type\": \"plaintext\",
        \"content\": {\"text\": \"Item $i\"}
      },
      \"privacy\": \"PUBLIC\"
    }"
  sleep 0.1
done
```

### Request - Page 1

```bash
TOKEN_ALICE="<token_from_login>"

curl -X GET "http://localhost:8080/api/v1/feed/me?limit=10&offset=0" \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### Expected Response - Page 1

```json
{
  "items": [ ... 10 items ... ],
  "total": 25,
  "limit": 10,
  "offset": 0,
  "hasMore": true
}
```

### Request - Page 2

```bash
curl -X GET "http://localhost:8080/api/v1/feed/me?limit=10&offset=10" \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### Expected Response - Page 2

```json
{
  "items": [ ... 10 items ... ],
  "total": 25,
  "limit": 10,
  "offset": 10,
  "hasMore": true
}
```

### Request - Page 3

```bash
curl -X GET "http://localhost:8080/api/v1/feed/me?limit=10&offset=20" \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### Expected Response - Page 3

```json
{
  "items": [ ... 5 items ... ],
  "total": 25,
  "limit": 10,
  "offset": 20,
  "hasMore": false
}
```

### Validation

- [ ] Page 1 returns items 0-9 with `hasMore: true`
- [ ] Page 2 returns items 10-19 with `hasMore: true`
- [ ] Page 3 returns items 20-24 with `hasMore: false`
- [ ] Total count is 25 on all pages

---

## Test Scenario 8: Validation - Empty Content

**Story**: Alice attempts to create a feed item with empty content; validation fails.

### Request

```bash
TOKEN_ALICE="<token_from_login>"

curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {
      "type": "markdown",
      "content": {}
    },
    "privacy": "PUBLIC"
  }'
```

### Expected Response

**Status**: `400 Bad Request`

```json
{
  "error": {
    "code": "INVALID_CONTENT",
    "message": "Content cannot be empty",
    "timestamp": "2026-03-19T10:33:00Z"
  }
}
```

### Validation

- [ ] Status is `400 Bad Request`
- [ ] Error code is `INVALID_CONTENT`
- [ ] Item is not created
- [ ] Database remains unchanged

---

## Test Scenario 9: Category Filtering

**Story**: Query feed items filtered by specific categories.

### Setup

```bash
TOKEN_ALICE="<token_from_login>"

# Create item with MENTAL category
curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "content": {"type": "plaintext", "content": {"text": "Mental health today"}},
    "privacy": "PUBLIC",
    "categories": ["MENTAL"]
  }'

# Create item with PHYSICAL category
curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "content": {"type": "plaintext", "content": {"text": "Gym workout"}},
    "privacy": "PUBLIC",
    "categories": ["PHYSICAL"]
  }'
```

### Request - Filter by MENTAL

```bash
curl -X GET "http://localhost:8080/api/v1/feed?categories=MENTAL" \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### Expected Response

```json
{
  "items": [
    {
      "uuid": "...",
      "content": {"text": "Mental health today"},
      "categories": ["MENTAL"],
      ...
    }
  ],
  "total": 1,
  "limit": 20,
  "offset": 0,
  "hasMore": false
}
```

### Validation

- [ ] Query returns only items with MENTAL category
- [ ] Items with PHYSICAL category are excluded
- [ ] Total count reflects filtered results

---

## Test Scenario 10: User's Own Feed

**Story**: Alice queries her own feed and sees all her items (PUBLIC, FRIENDS_ONLY, PRIVATE).

### Setup

```bash
TOKEN_ALICE="<token_from_login>"

# Create PUBLIC item
curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "content": {"type": "plaintext", "content": {"text": "Public item"}},
    "privacy": "PUBLIC"
  }'

# Create FRIENDS_ONLY item
curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "content": {"type": "plaintext", "content": {"text": "Friends only item"}},
    "privacy": "FRIENDS_ONLY"
  }'

# Create PRIVATE item
curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "content": {"type": "plaintext", "content": {"text": "Private item"}},
    "privacy": "PRIVATE"
  }'
```

### Request

```bash
TOKEN_ALICE="<token_from_login>"

curl -X GET http://localhost:8080/api/v1/feed/me \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### Expected Response

```json
{
  "items": [
    { "uuid": "...", "privacy": "PRIVATE", ... },
    { "uuid": "...", "privacy": "FRIENDS_ONLY", ... },
    { "uuid": "...", "privacy": "PUBLIC", ... }
  ],
  "total": 3,
  "limit": 20,
  "offset": 0,
  "hasMore": false
}
```

### Validation

- [ ] Alice's own feed includes all three items (PUBLIC, FRIENDS_ONLY, PRIVATE)
- [ ] Total count is 3
- [ ] Items ordered by createdAt DESC (most recent first)

---

## Performance Validation (Optional)

### Test: Query Performance (< 500ms)

```bash
TOKEN_ALICE="<token_from_login>"

# Time the request
time curl -X GET http://localhost:8080/api/v1/feed/me?limit=20 \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

**Expected**: Response time < 500ms

### Test: Create Performance (< 2 seconds)

```bash
TOKEN_ALICE="<token_from_login>"

# Measure creation time
time curl -X POST http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "content": {"type": "markdown", "content": {"text": "Performance test"}},
    "privacy": "PUBLIC"
  }'
```

**Expected**: Response time < 2 seconds

---

## Cleanup

```bash
# Optional: Delete test database
dropdb vitamo_dev

# Rebuild from scratch
./gradlew server:build
```

---

## Next Steps

1. Implement routes, repositories, and use cases in code
2. Run these scenarios against implemented API
3. Add automated test suite based on these scenarios
4. Measure performance metrics (latency, database query times)
