# Implementation Plan: User Feed Items

**Branch**: `001-feed-items` | **Date**: 2026-03-19 | **Spec**: [specs/001-feed-items/spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-feed-items/spec.md`

**Status**: Phase 1 Design Complete (research.md, data-model.md, contracts/, quickstart.md generated)

## Summary

Implement full CRUD operations for feed items in a Kotlin Multiplatform app + Ktor backend stack. Users can create, read, update, and soft-delete feed items with rich text content, privacy controls (PUBLIC, FRIENDS_ONLY, PRIVATE), and wellness categories. Privacy rules are enforced server-side; soft-delete is timestamp-based. Persistence strategy is UUID-first: **all Exposed tables inherit `UuidTable`**, all primary keys use **`kotlin.uuid.Uuid`**, and all relationships use UUID foreign keys (no numeric auto-increment IDs). API contracts stay canonical in `core`; server entities remain isolated to `server`.

## Technical Context

**Language/Version**: Kotlin 2.0+ | Kotlin Multiplatform (KMP) with Android (Jetpack Compose) primary, iOS secondary

**Primary Dependencies**: 
- Ktor 2.3+ (REST API framework, server-side routing, serialization)
- Ktor Client 2.3+ (app-side HTTP client)
- Exposed 0.41+ (PostgreSQL ORM, SQL DSL)
- kotlinx.serialization 1.6+ (core contracts, serialization)
- Jetpack Compose 1.6+ (Android UI)
- Koin 3.5+ (DI framework, shared across app & server)

**Storage**: PostgreSQL 14+ with Flyway migrations (schema versioning)

**Testing**: 
- Shared tests: `commonTest` (serialization, models)
- Server tests: `server/src/test/kotlin` (routes, repositories, authorization)
- Unit tests: Business logic (UseCase layer)
- Integration tests: End-to-end API flows

**Target Platform**: 
- Server: JVM (Ktor server)
- App: Android primary via Jetpack Compose; iOS secondary via KMP
- Shared: Kotlin Multiplatform (commonMain, androidMain, iosMain)

**Project Type**: Kotlin Multiplatform application with backend REST API + mobile clients

**Performance Goals**: 
- Feed item creation: < 2 seconds (from submission to confirmation)
- Paginated query (20 items): < 500ms response time
- Authorization checks: sub-millisecond overhead

**Constraints**: 
- Content size: ≤ 10 MB per item
- Character limit: ≤ 500,000 per item
- Rich text type validation: Must be one of (markdown, plaintext, document, html)
- Privacy enforcement: 100% server-side (no client-side trust)
- Persistence hard constraint: all database tables inherit Exposed `UuidTable`; PK type is `kotlin.uuid.Uuid`; no `IntIdTable`/`LongIdTable`/auto-increment IDs
- No `webApp` changes
- KMP platform divergence must use `expect/actual` in `app/shared`
- Secrets are provided via environment variables and local `.env` only

**Scale/Scope**: 
- 4 user stories (create, read, update, soft-delete)
- 1000+ feed items baseline
- Multiple privacy levels (3 enum values)
- 13 category options
- Pagination support (offset-based, default 20 items/page)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Boundary Integrity**: `BaseFeedItem` sealed interface and `FeedItem` concrete class defined in `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/`. Server persistence entities (FeedItemEntity, FeedItemCategoryEntity, FeedItemMediaAssetEntity) isolated in `server/src/main/kotlin/eu/vitamo/app/database/entities/`. No cross-module leakage; server internals never exposed in API responses.

- [x] **Shared Contract Canonicality**: Single canonical `@Serializable FeedItem` in `core` module; no DTO duplication. Server uses this contract directly for API responses. Exposed persistence uses `UuidTable` with `kotlin.uuid.Uuid` identifiers end-to-end; no numeric IDs are introduced or exposed.

- [x] **SIMPLE Delivery Discipline**: Scope is minimal (4 user stories: create, read, update, delete). Existing Kotlin Multiplatform patterns reused. Change summary provided below. Verification: Build passes (`./gradlew build`); migrations apply successfully; Gradle test suites run without error.

- [x] **Security-First Enforcement**: All privacy/ownership checks performed server-side before data fetch or modification. Privacy filter applied at query time (Exposed DSL). Authorization checks verify ownership before update/delete operations. Client-side UI filtering is UX-only; server is source of truth. Error responses omit internal schema details, stack traces, database structure.

- [x] **Testable Incremental Quality**: Each user story independently testable (create item, read with privacy, update, delete). Test approach: Unit tests for business logic (UseCase layer); integration tests for API contracts (routes + repositories). Shared tests for serialization (core). Coverage target: ≥80% for critical paths. Skipped tests documented with reason.

✅ **Gate Status**: PASS (all five principles satisfied; design aligns with Constitution 1.0.0)

## Project Structure

### Documentation (this feature)

```text
specs/001-feed-items/
├── plan.md                    # This file (implementation plan)
├── research.md                # Phase 0 output (technology decisions, dependencies, patterns)
├── data-model.md              # Phase 1 output (entity definitions, relationships, validation)
├── quickstart.md              # Phase 1 output (end-to-end validation scenarios)
├── spec.md                    # Original feature specification
├── contracts/
│   ├── api-routes.md          # Phase 1 output (REST routes, request/response schemas)
│   └── serialization.md       # Phase 1 output (JSON format, validation rules)
└── tasks.md                   # Phase 2 output (task-generation; NOT created by /speckit.plan)
```

### Source Code (Kotlin Multiplatform Project)

```text
vitamo/VitamoAI/ (repository root)

# Shared Contracts & Models (Platform-Neutral)
core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/
├── BaseFeedItem.kt            # Sealed interface; canonical contract
├── FeedItem.kt                # Concrete implementation (DTO for API)
├── RichTextDocument.kt        # Content representation
├── PrivacyStatus.kt           # Enum: PUBLIC, FRIENDS_ONLY, PRIVATE
├── FeedCategory.kt            # Enum: 13 wellness categories
└── MediaAsset.kt              # Reference to attached media

# Serialization (Existing, Enhanced)
core/src/commonMain/kotlin/eu/vitamo/app/serialization/
├── AppJson.kt                 # Canonical serialization instance (updated)
├── UuidSerializer.kt          # Existing
└── InstantSerializer.kt       # Existing

# App-Side Implementation (Kotlin Multiplatform)
app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/
├── ui/
│   ├── FeedScreen.kt          # Main feed UI (Compose Multiplatform)
│   ├── FeedItemCreateScreen.kt # Create form
│   ├── FeedItemDetailScreen.kt # Single item view
│   └── FeedViewModel.kt        # State management (MVVM)
├── repository/
│   └── FeedRepository.kt       # API calls + local caching
├── usecase/
│   ├── CreateFeedItemUseCase.kt
│   ├── ReadFeedItemUseCase.kt
│   ├── UpdateFeedItemUseCase.kt
│   └── DeleteFeedItemUseCase.kt
└── di/
    └── FeedModule.kt          # Koin DI setup (shared)

# Android-Specific Wiring (if needed)
app/androidApp/src/main/kotlin/eu/vitamo/app/
├── features/feed/
│   └── FeedWiring.kt          # Android-specific initialization (empty if KMP handles all)
└── di/
    └── AndroidFeedModule.kt   # Android DI bindings (if needed)

# iOS-Specific Wiring (if needed)
app/iosApp/src/main/kotlin/eu/vitamo/app/
└── features/feed/
    └── FeedWiring.kt          # iOS-specific initialization (if needed)

# Server-Side Implementation
server/src/main/kotlin/eu/vitamo/app/
├── features/feed/
│   ├── routes/
│   │   └── FeedRoutes.kt      # Ktor routes: POST/GET/PATCH/DELETE /api/v1/feed
│   ├── repository/
│   │   └── FeedRepository.kt  # Exposed queries + entity mapping
│   ├── usecase/
│   │   ├── CreateFeedItemUseCase.kt
│   │   ├── ReadFeedItemUseCase.kt
│   │   ├── UpdateFeedItemUseCase.kt
│   │   └── DeleteFeedItemUseCase.kt
│   ├── service/
│   │   └── FeedAuthorizationService.kt # Privacy/ownership checks
│   └── di/
│       └── FeedModule.kt      # Koin DI setup (server)
├── database/
│   ├── entities/
│   │   ├── FeedItemEntity.kt  # Exposed table: feed_items
│   │   ├── FeedItemCategoryEntity.kt # Junction: feed_item_categories
│   │   └── FeedItemMediaAssetEntity.kt # Junction: feed_item_media_assets
│   └── migration/ (Flyway)
│       ├── V001__CreateFeedItemsSchema.sql
│       └── V002__CreateIndexes.sql (if needed)
└── di/
    └── ServerFeedModule.kt    # Server Koin setup

# Tests (Shared & Server)
core/src/commonTest/kotlin/eu/vitamo/app/api/contracts/feed/
├── RichTextDocumentSerializationTest.kt
├── FeedItemSerializationTest.kt
└── PrivacyStatusSerializationTest.kt

server/src/test/kotlin/eu/vitamo/app/features/feed/
├── routes/
│   └── FeedRoutesTest.kt      # API contract tests
├── repository/
│   └── FeedRepositoryTest.kt  # Data access tests
├── usecase/
│   ├── CreateFeedItemUseCaseTest.kt
│   ├── ReadFeedItemUseCaseTest.kt
│   ├── UpdateFeedItemUseCaseTest.kt
│   └── DeleteFeedItemUseCaseTest.kt
└── service/
    └── FeedAuthorizationServiceTest.kt # Privacy/authorization tests

# Gradle Build Files (No changes to structure; new tasks may be added)
build.gradle.kts                # Root (no changes)
core/build.gradle.kts           # May add commonTest dependency
app/shared/build.gradle.kts     # May add Compose dependencies (likely already present)
server/build.gradle.kts         # May add Ktor routing dependencies (likely already present)
```

**Structure Decision**: Kotlin Multiplatform (KMP) with modular separation:
- `core`: Platform-neutral serializable contracts only
- `app/shared`: Shared app logic (ViewModels, Repositories, UseCases, Compose UI)
- `app/androidApp`, `app/iosApp`: Platform startup and wiring only
- `server`: REST API routes, persistence, authorization, server-specific business logic
- Each feature isolated in `features/` subdirectory; DI via Koin

**Rationale**: 
- Maximizes code reuse (single ViewModel, single Repository for both Android/iOS)
- Preserves boundary integrity (server logic never leaks to app; app concerns stay off server)
- Aligns with VitamoAI Constitution (module ownership, shared contracts)
- Supports future web client without duplicate server code

## Complexity Tracking

> Complexity violations and justifications (if Constitution Check issues require exceptions)

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|-----------|-------------------------------------|
| None | N/A | N/A |

✅ **Design Status**: No violations; all Constitution principles satisfied without exceptions.

## Implementation alignment update (2026-07-01)

- ✅ Module boundary integrity preserved (`core` contracts, `server` persistence/routes/use-cases, `app/shared` UI/API/repository)
- ✅ Canonical shared contracts implemented under `core/api/contracts/feed`
- ✅ UUID-only Exposed `UuidTable` persistence implemented for all feed feature tables
- ✅ `webApp/` left unchanged by implementation scope
- ⚠ Friendship graph is not yet implemented in the codebase; `FRIENDS_ONLY` currently falls back to owner-or-friendship-service behavior with a noop friendship service

---

## Architectural Decisions & Touchpoints

### 1. API Routes (Server: Ktor)

**File**: `server/src/main/kotlin/eu/vitamo/app/features/feed/routes/FeedRoutes.kt`

**Routes**:
- `POST /api/v1/feed` → Create feed item
- `GET /api/v1/feed/{uuid}` → Get single item (privacy-enforced)
- `GET /api/v1/feed/me` → User's own feed (paginated)
- `GET /api/v1/feed` → General feed (PUBLIC + privacy-filtered, paginated)
- `PATCH /api/v1/feed/{uuid}` → Update item (ownership check)
- `DELETE /api/v1/feed/{uuid}` → Soft delete (ownership check)

**Middleware Applied**: Authentication (Bearer token extraction), authorization (user context passing)

**Error Handling**: Standard ApiErrorResponse format (code, message, timestamp; no leakage)

### 2. Database Schema (PostgreSQL + Flyway)

**Migration File**: `server/src/main/resources/db/migration/V001__CreateFeedItemsSchema.sql`

**Schema Strategy (Hard Constraint)**:
- Every Exposed table in this feature inherits `UuidTable`
- Every table primary key is `id: kotlin.uuid.Uuid`
- All references are UUID foreign keys (`reference(..., ..., onDelete = ...)` to `UuidTable.id`)
- Explicitly prohibited: `IntIdTable`, `LongIdTable`, or auto-increment numeric keys

**Tables**:
- `feed_items` (`id` UUID PK, `author_id` UUID FK, content/privacy/timestamps)
- `feed_item_categories` (`id` UUID PK, `feed_item_id` UUID FK, `category`)
- `feed_item_media_assets` (`id` UUID PK, `feed_item_id` UUID FK, `media_asset_id` UUID)

**Indexes**:
- `(author_id, created_at DESC)` - User's own feed sorting
- `(author_id, deleted_at)` - Efficient soft-delete filtering
- `(id)` - UUID primary key

### 3. Server-Side Use Cases (Business Logic)

**Files**: `server/src/main/kotlin/eu/vitamo/app/features/feed/usecase/`

- `CreateFeedItemUseCase`: Validate content, assign UUID, set timestamps, insert into DB
- `ReadFeedItemUseCase`: Apply privacy filter, return to authorized user
- `UpdateFeedItemUseCase`: Ownership check, validate new content, update timestamps
- `DeleteFeedItemUseCase`: Ownership check, set deletedAt, soft-delete only

**Authorization Service**: `FeedAuthorizationService` - Centralized privacy/ownership validation

### 4. Server-Side Repository (Data Access)

**File**: `server/src/main/kotlin/eu/vitamo/app/features/feed/repository/FeedRepository.kt`

**Exposed ORM Usage**:
```kotlin
// Example: Query user's feed with soft-delete filter
FeedItems.selectAll()
    .andWhere { FeedItems.authorId eq userId }
    .andWhere { FeedItems.deletedAt.isNull() }
    .orderBy(FeedItems.createdAt to SortOrder.DESC)
    .limit(limit, offset)
    .map { mapRowToFeedItem(it) }
```

**Entity Mapping**: `UuidTable` rows (`kotlin.uuid.Uuid` IDs + UUID FKs) → `FeedItem` (core contract); never expose server internals.

### 5. App-Side ViewModel (State Management)

**Files**: `app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/FeedViewModel.kt`

**Pattern**: MVVM with Kotlin Flow for reactive state

**State**:
```kotlin
val feedItems: StateFlow<List<FeedItem>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>
val currentPage: StateFlow<Int>
```

**Functions**:
- `loadFeed(limit, offset)` - Fetch items from API via Repository
- `createItem(content, privacy, categories)` - Post new item
- `updateItem(uuid, content, privacy, categories)` - Patch existing
- `deleteItem(uuid)` - Soft delete
- `nextPage()` / `previousPage()` - Pagination

### 6. App-Side Repository (API Client)

**File**: `app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/FeedRepository.kt`

**Pattern**: Repository pattern with Ktor HTTP client

**Methods**:
- `createFeedItem(request)` - POST to `/api/v1/feed`
- `getFeedItem(uuid)` - GET from `/api/v1/feed/{uuid}`
- `getUserFeed(limit, offset)` - GET from `/api/v1/feed/me`
- `getGeneralFeed(limit, offset)` - GET from `/api/v1/feed`
- `updateFeedItem(uuid, request)` - PATCH to `/api/v1/feed/{uuid}`
- `deleteFeedItem(uuid)` - DELETE to `/api/v1/feed/{uuid}`

**HTTP Client**: Ktor Client with standard error handling (try/catch, ApiErrorResponse parsing)

### 7. App-Side UI (Jetpack Compose / Compose Multiplatform)

**Files**: `app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/`

**Screens**:
- `FeedScreen` - Main feed list (PUBLIC + FRIENDS_ONLY items for current user)
- `FeedItemDetailScreen` - Single item display
- `FeedItemCreateScreen` - Create/edit form (validation, submit)

**Composables**: `@Composable` functions that observe ViewModel state via `collectAsState()`

### 8. Serialization (Core Module)

**File**: `core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/`

**Serializers**:
- `@Serializable data class FeedItem` - Uses UuidSerializer, InstantSerializer
- `@Serializable enum class PrivacyStatus` - String values
- `@Serializable data class RichTextDocument` - JsonElement for flexible schema
- All models use `AppJson` instance for serialization

### 9. Dependency Injection (Koin)

**Server DI**: `server/src/main/kotlin/eu/vitamo/app/di/ServerFeedModule.kt`
- Koin module for FeedRepository, FeedAuthorizationService, UseCases, Routes

**App DI**: `app/shared/src/commonMain/kotlin/eu/vitamo/app/di/FeedModule.kt`
- Koin module for FeedRepository (HTTP client), FeedViewModel, UseCases

**Design**: Separate DI containers (server ≠ app); no shared startup side effects

### 10. Testing Strategy

**Unit Tests** (Business Logic):
- `FeedAuthorizationServiceTest` - Privacy rules (PUBLIC, FRIENDS_ONLY, PRIVATE)
- `*UseCaseTest` - Create, read, update, delete flows
- Location: `server/src/test/kotlin`

**Integration Tests** (API Contracts):
- `FeedRoutesTest` - HTTP endpoints, request/response validation
- `FeedRepositoryTest` - Database queries, entity mapping
- Location: `server/src/test/kotlin`

**Shared Tests** (Serialization):
- `FeedItemSerializationTest` - JSON roundtrip
- `RichTextDocumentTest` - Content validation
- Location: `core/src/commonTest/kotlin`

---

## Implementation Phases

### Phase 0: Research (COMPLETE ✅)
- [x] Dependency versions finalized (research.md)
- [x] Technology choices justified (Ktor, Exposed, KMP, etc.)
- [x] Patterns documented (soft-delete, privacy filtering, serialization)

### Phase 1: Design (COMPLETE ✅)
- [x] Entity definitions (data-model.md)
- [x] API contracts (contracts/api-routes.md)
- [x] Serialization rules (contracts/serialization.md)
- [x] Validation rules finalized
- [x] End-to-end scenarios (quickstart.md)
- [x] Constitution Check gates PASS ✅

### Phase 2: Task Generation (NEXT)
Run: `/speckit.tasks` to generate `tasks.md` with implementation tasks broken into dependencies

### Phase 3: Implementation (AFTER tasks.md)
Implement routes, repositories, use cases, UI following `tasks.md` task sequence

### Phase 4: Testing & Verification (FINAL)
- Run automated test suites
- Execute quickstart.md scenarios against live API
- Validate performance goals
- Gate check Constitution again before merge

---

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Privacy rules leak data | Server-side authorization enforced before query; client-side UX filtering not trusted |
| Soft-delete causes confusion | Clear documentation; `deletedAt` field explicit in model; tests verify exclusion |
| Serialization roundtrip errors | Shared tests in `commonTest`; strict validation on deserialization |
| Database migration failures | Flyway versioning; migrations tested in development; rollback plan documented |
| Performance regression | Indexes defined in migration; pagination defaults tested (20 items, <500ms target) |
| Cross-platform inconsistency | `expect/actual` for platform-specific code only; shared logic in commonMain |

---

## Verification Checklist

Before proceeding to Phase 2 (task generation):

- [x] All five Constitution principles satisfied
- [x] Boundary integrity maintained (no module leakage)
- [x] Shared contracts canonical (one source of truth)
- [x] Security enforced server-side
- [x] Testing strategy defined
- [x] Project structure documented
- [x] API contracts complete
- [x] Serialization rules specified
- [x] End-to-end scenarios documented
- [x] No NEEDS CLARIFICATION items remaining

✅ **Design Review Complete**: Ready to proceed to Phase 2 task generation.
