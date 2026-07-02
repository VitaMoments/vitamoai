# Research: User Feed Items Feature

**Phase 0 Outcome**: Technical unknowns resolved and best practices documented.

## Technology Decisions

### 0. UUID Persistence Strategy (Exposed)

**Decision**: All database tables for this feature inherit Exposed `UuidTable`; all primary keys use `kotlin.uuid.Uuid`; all relations use UUID foreign keys.  
**Rationale**:
- Satisfies repository hard constraint for consistent ID strategy
- Avoids mixed identifier models and migration complexity
- Matches canonical API contract UUID semantics end-to-end
**Alternatives considered**:
- `IntIdTable` / auto-increment IDs — rejected (violates hard constraint)
- Mixed UUID + numeric surrogate IDs — rejected (inconsistent and unnecessary)

### 1. Kotlin Multiplatform Project Structure

**Decision**: Kotlin Multiplatform (KMP) with Android (Jetpack Compose) primary, iOS secondary  
**Rationale**: 
- Enables code sharing across Android and iOS via `app/shared` module
- Aligns with existing VitamoAI architecture (core, app/shared, app/androidApp, app/iosApp, server)
- Jetpack Compose provides modern declarative UI framework for Android
- Kotlin expect/actual enables platform-specific implementations when needed
**Verified**: Project uses Kotlin Multiplatform plugin; core/build.gradle.kts confirms this setup

### 2. Ktor REST API Framework (Server-Side)

**Decision**: Ktor 2.3+ for REST API with routing, plugins, and middleware  
**Rationale**:
- Lightweight, async-first Kotlin framework perfect for microservices
- Built-in support for content negotiation, validation, and auth middleware
- Compose-style routing API aligns with app-side Jetpack Compose patterns
- Strong integration with Exposed ORM for PostgreSQL persistence
**Implementation approach**: 
- Routes in `server/src/main/kotlin/eu/vitamo/app/features/feed/`
- UseCases for business logic (create, read, update, delete)
- Repository pattern for data access
- Middleware for auth, error handling, logging

### 3. PostgreSQL with Exposed ORM

**Decision**: PostgreSQL 14+ with Exposed ORM (Kotlin SQL framework)  
**Rationale**:
- Exposed provides type-safe, DSL-based database queries in Kotlin
- Nullable columns for `deletedAt` soft-delete tracking
- Full-text search capabilities if needed for content search later
- Flyway migrations ensure schema versioning and rollback safety
**Schema approach**:
- Table: `feed_items` (`id: kotlin.uuid.Uuid`, `author_id: kotlin.uuid.Uuid`, content_json, privacy, created_at, updated_at, deleted_at)
- Table: `feed_item_categories` (`id: kotlin.uuid.Uuid`, `feed_item_id: kotlin.uuid.Uuid`, category) for feed/category mapping
- Table: `feed_item_media_assets` (`id: kotlin.uuid.Uuid`, `feed_item_id: kotlin.uuid.Uuid`, `media_asset_id: kotlin.uuid.Uuid`) for media references
- Indexes on (author_id, created_at), (author_id, deleted_at) for query performance

### 4. Serialization: kotlinx.serialization with Custom Serializers

**Decision**: `@Serializable` models with `AppJson` instance in `core` for canonical contracts  
**Rationale**:
- Aligns with existing VitamoAI patterns (UuidSerializer, InstantSerializer already in core)
- Minimal dependencies compared to Gson/Jackson
- Compile-time code generation ensures safety
- Sealed classes with discriminator (`"type"`) enable polymorphic deserialization
**Implementation**:
- Define `BaseFeedItem` interface in `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/`
- Define `FeedItem` (concrete) and `RichTextDocument` with `@Serializable`
- Use `AppJson.encodeToString()` for serialization, `AppJson.decodeFromString()` for deserialization
- Exclude `deletedAt` from JSON responses unless explicitly requested (admin/debug endpoints only)

### 5. App-Side Architecture: ViewModel + Repository + UseCase

**Decision**: MVVM pattern with Repository pattern for API calls  
**Rationale**:
- ViewModel provides state management and lifecycle awareness
- Repository abstracts API, local cache, and data transformations
- UseCase encapsulates business logic (fetch user feed, create item, validate ownership)
- Compose's state collection (`collectAsState()`) binds ViewModel state to UI
**Implementation**:
- `app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/FeedViewModel.kt`
- `app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/FeedRepository.kt`
- `app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/usecase/FeedUseCases.kt`

### 6. Privacy Enforcement: Server-Side Filters with Query Guards

**Decision**: All privacy/ownership checks executed server-side before data fetch  
**Rationale**:
- Centralized enforcement prevents authorization bypasses
- Client-side filtering is UX-only; server is source of truth
- Filters applied in Repository queries (Exposed DSL) to exclude unauthorized items at database layer
**Implementation**:
- Route middleware checks authentication token, extracts user_id
- Repository query applies privacy filter: `where { privacy eq PUBLIC or (privacy eq FRIENDS_ONLY and author_id in friendsOf(currentUser)) or (privacy eq PRIVATE and author_id eq currentUser) }`
- Update/delete routes check ownership: `if (item.author_id != currentUser.id) return 403 Forbidden`

### 7. Soft-Delete Strategy: Timestamp Marker

**Decision**: Mark deleted items with `deletedAt: Instant` (nullable); hard delete via admin batch job (not MVP)  
**Rationale**:
- Preserves referential integrity (comments, likes still reference deleted items)
- Enables audit trails and compliance queries
- Reversible (can set `deletedAt` to null if recovery needed)
- Simple to implement: filter `where deletedAt.isNull()` in all queries
**Testing**: Verify that deleted items don't appear in user queries; verify that soft-deleted items can be queried via admin endpoints if implemented later

### 8. Pagination: Offset-Based with Defaults

**Decision**: Offset-based pagination (limit, offset) with 20 items default  
**Rationale**:
- Simpler to implement than cursor-based for MVP
- Works well with typical SQL `LIMIT offset, limit` clauses
- Offset supports random access (jump to page 5)
- Default limit of 20 prevents large response payloads
**Future optimization**: Cursor-based pagination when dataset grows >10k items

### 9. Media Assets: Reference-Based (Storage Out-of-Scope)

**Decision**: `MediaAsset` objects are references; upload/storage handled by separate service  
**Rationale**:
- Keeps feed items feature focused on content management
- Media storage is orthogonal (CDN, S3, local filesystem—interchangeable)
- Separate service can scale independently
**Implementation**:
- `MediaAsset` has `id` (UUID), `url` (String), `type` (IMAGE, VIDEO, DOCUMENT)
- Feed items store list of media asset IDs
- App calls separate media service for upload; receives ID to attach to feed item
- Feed API returns media asset IDs; app hydrates with URLs from media service cache

### 10. Testing Strategy: Unit + Integration

**Decision**: 
- **Unit tests** for business logic (UseCase layer)
- **Integration tests** for API contracts (routes + repositories)
- **Shared tests** for serialization (core module)
**Rationale**:
- Unit tests verify privacy logic, validation, timestamps independently
- Integration tests verify end-to-end flows (create → read with privacy enforcement)
- Shared tests ensure serialization roundtrips work across platforms
**Coverage target**: ≥80% for critical paths (create, read, update, delete, privacy)

## Pattern Decisions

### Rich Text Document Storage

**Decision**: Store as JSON in PostgreSQL JSONB column; validate schema server-side  
**Rationale**:
- JSONB enables efficient queries and updates without denormalization
- Flexible schema supports markdown, plaintext, HTML, or custom markup
- Server validation ensures data consistency
**Implementation**:
- Kotlin model: `data class RichTextDocument(val type: String, val content: JsonElement)`
- PostgreSQL: column `content JSONB DEFAULT '{}'`
- Flyway migration: ADD CONSTRAINT `check_content_not_empty`
- Server-side validation: `if (doc.content.isEmpty()) throw ValidationException()`

### Privacy Levels Enum

**Decision**: Use sealed enum with String serialization  
**Rationale**:
- Sealed enum enables exhaustive when() checks
- String values (PUBLIC, FRIENDS_ONLY, PRIVATE) are human-readable in logs/API
**Implementation**: 
```kotlin
@Serializable
enum class PrivacyStatus(val value: String) {
    PUBLIC("PUBLIC"),
    FRIENDS_ONLY("FRIENDS_ONLY"),
    PRIVATE("PRIVATE")
}
```

### Feed Categories Enum

**Decision**: Sealed enum with 13 values as specified  
**Rationale**:
- Compile-time safety prevents invalid categories
- No separate table needed; stored as list of enum strings
**Implementation**: 
```kotlin
@Serializable
enum class FeedCategory(val value: String) {
    MENTAL("MENTAL"), PHYSICAL("PHYSICAL"), FOOD("FOOD"), LIFESTYLE("LIFESTYLE"),
    MINDFULNESS("MINDFULNESS"), HABITS("HABITS"), SLEEP("SLEEP"), ENERGY("ENERGY"),
    RELATIONSHIPS("RELATIONSHIPS"), COMMUNITY("COMMUNITY"), PURPOSE("PURPOSE"),
    PERSONAL_GROWTH("PERSONAL_GROWTH"), REFLECTION("REFLECTION")
}
```

## Dependency Versions

Based on inspection of existing build files and standard KMP practices:

| Dependency | Version | Reason |
|-----------|---------|--------|
| Kotlin | 2.0+ | Latest stable; multiplatform support |
| Ktor (server) | 2.3+ | REST API, serialization, auth middleware |
| Ktor (client) | 2.3+ | App-side HTTP calls |
| Exposed ORM | 0.41+ | PostgreSQL support, DSL queries |
| kotlinx.serialization | 1.6+ | `@Serializable`, custom serializers |
| Jetpack Compose | 1.6+ | Android UI framework |
| Koin | 3.5+ | DI framework (app & server) |
| PostgreSQL Driver (JDBC) | 42.7+ | Database connectivity |
| Flyway | 10.+ | Schema migrations |

## Constitution Alignment ✅

- **Boundary Integrity**: `BaseFeedItem` and `FeedItem` defined in `core`; server entities isolated in `server` module; no cross-module leakage
- **Shared Contract Canonicality**: Single `@Serializable` model in `core`; no DTOs duplicated; server internal models remain internal
- **Security Enforcement**: All privacy/ownership checks server-side before query execution; client validates for UX only
- **SIMPLE Delivery**: Minimal scope (CRUD only); existing Exposed patterns reused; change summary provided below
- **Incremental Testability**: Each user story independently testable; test approach defined for create/read/update/delete flows

## Next Steps

1. **Design Phase 1**: Generate data-model.md with entity definitions and relationships
2. **Contracts Phase 1**: Define API routes, request/response schemas in contracts/
3. **Quickstart Phase 1**: Document end-to-end validation scenarios
4. **Plan Phase 1**: Fill plan.md with project structure and architectural decisions
5. **Gate Check**: Re-evaluate Constitution Check after Phase 1; proceed if all passed
