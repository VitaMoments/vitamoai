# Tasks: User Feed Items

**Input**: Design documents from `/specs/001-feed-items/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize feature scaffolding and wiring boundaries before core implementation.

- [X] T001 Add feed feature package scaffolding in server/src/main/kotlin/eu/vitamo/app/features/feed/ and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/
- [X] T002 Add feed route registration hook in server/src/main/kotlin/eu/vitamo/app/Application.kt
- [X] T003 [P] Add server feed module registration in server/src/main/kotlin/eu/vitamo/app/di/ServerKoin.kt
- [X] T004 [P] Add shared feed UI module registration in app/shared/src/commonMain/kotlin/eu/vitamo/app/di/modules/UiKoinModules.kt
- [X] T005 [P] Document feed-related local secret requirements in .env and README.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Build shared contracts, persistence foundation, and cross-platform primitives required by all stories.

**⚠️ CRITICAL**: Complete this phase before starting user stories.

- [X] T006 [P] Create BaseFeedItem contract in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/BaseFeedItem.kt
- [X] T007 [P] Create FeedItem model in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedItem.kt
- [X] T008 [P] Create RichTextDocument model in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/RichTextDocument.kt
- [X] T009 [P] Create PrivacyStatus enum in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/PrivacyStatus.kt
- [X] T010 [P] Create FeedCategory enum in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedCategory.kt
- [X] T011 [P] Create MediaAsset and MediaAssetType models in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/MediaAsset.kt
- [X] T012 Create feed request/response contracts in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedRequests.kt
- [X] T013 [P] Update shared JSON serialization settings for feed contracts in core/src/commonMain/kotlin/eu/vitamo/app/serialization/AppJson.kt
- [X] T014 [P] Add shared feed contract serialization tests in core/src/commonTest/kotlin/eu/vitamo/app/api/contracts/feed/FeedContractsSerializationTest.kt
- [X] T015 Create feed schema migration with UUID PK/FK columns only in server/src/main/resources/db/migration/V3__create_feed_tables.sql
- [X] T016 [P] Create Exposed UuidTable definitions for feed items in server/src/main/kotlin/eu/vitamo/app/features/feed/table/FeedItemsTable.kt
- [X] T017 [P] Create Exposed UuidTable definitions for feed categories in server/src/main/kotlin/eu/vitamo/app/features/feed/table/FeedItemCategoriesTable.kt
- [X] T018 [P] Create Exposed UuidTable definitions for feed media assets in server/src/main/kotlin/eu/vitamo/app/features/feed/table/FeedItemMediaAssetsTable.kt
- [X] T019 Create feed domain model and mapper in server/src/main/kotlin/eu/vitamo/app/features/feed/model/FeedItemRecord.kt and server/src/main/kotlin/eu/vitamo/app/features/feed/mapper/FeedMappers.kt
- [X] T020 Create feed repository contract in server/src/main/kotlin/eu/vitamo/app/features/feed/repository/FeedRepository.kt
- [X] T021 Create Exposed repository skeleton in server/src/main/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepository.kt
- [X] T022 [P] Create server feed DI module in server/src/main/kotlin/eu/vitamo/app/features/feed/di/FeedModule.kt
- [X] T023 [P] Create app feed API contract in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/FeedApi.kt
- [X] T024 [P] Create app feed API config and Ktor client skeleton in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/FeedApiConfig.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/KtorFeedApi.kt
- [X] T025 [P] Create app feed repository contract and default implementation skeleton in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/FeedRepository.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/DefaultFeedRepository.kt
- [X] T026 Create expect declaration for platform-specific feed behavior in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/platform/FeedPlatform.kt
- [X] T027 [P] Create Android actual implementation for feed platform behavior in app/shared/src/androidMain/kotlin/eu/vitamo/app/features/feed/platform/FeedPlatform.android.kt
- [X] T028 [P] Create iOS actual implementation for feed platform behavior in app/shared/src/iosMain/kotlin/eu/vitamo/app/features/feed/platform/FeedPlatform.ios.kt
- [X] T029 [P] Create JVM actual implementation for feed platform behavior in app/shared/src/jvmMain/kotlin/eu/vitamo/app/features/feed/platform/FeedPlatform.jvm.kt

**Checkpoint**: Foundational contracts, schema, and platform abstractions are ready.

---

## Phase 3: User Story 1 - Create Feed Item (Priority: P1) 🎯 MVP

**Goal**: Users can create feed items with rich content, privacy, optional categories, and media references.

**Independent Test**: Authenticated user creates a feed item and gets `201` with canonical `FeedItem` payload and valid UUID/timestamps.

### Tests for User Story 1

- [X] T030 [P] [US1] Add create feed route happy-path and validation tests in server/src/test/kotlin/eu/vitamo/app/features/feed/routes/FeedCreateRouteTest.kt
- [X] T031 [P] [US1] Add create feed use-case tests in server/src/test/kotlin/eu/vitamo/app/features/feed/usecase/CreateFeedItemUseCaseTest.kt

### Implementation for User Story 1

- [X] T032 [US1] Implement feed content/category/media validation logic in server/src/main/kotlin/eu/vitamo/app/features/feed/validation/FeedInputValidator.kt
- [X] T033 [US1] Implement create feed use case in server/src/main/kotlin/eu/vitamo/app/features/feed/usecase/CreateFeedItemUseCase.kt
- [X] T034 [US1] Implement repository create transaction for feed item and junction rows in server/src/main/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepository.kt
- [X] T035 [US1] Implement POST /api/v1/feed handler in server/src/main/kotlin/eu/vitamo/app/features/feed/routes/FeedRoutes.kt
- [X] T036 [US1] Wire create use case and route dependencies in server/src/main/kotlin/eu/vitamo/app/features/feed/di/FeedModule.kt
- [X] T037 [P] [US1] Implement app create-feed API and repository calls in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/KtorFeedApi.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/DefaultFeedRepository.kt
- [X] T038 [US1] Implement create feed ViewModel in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/create/FeedCreateViewModel.kt
- [X] T039 [US1] Implement create feed Compose screen in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/create/FeedCreateScreen.kt

**Checkpoint**: Create flow works end-to-end and is testable independently.

---

## Phase 4: User Story 2 - Read/Query Feed Items (Priority: P1)

**Goal**: Users can read paginated feed items with strict server-side privacy enforcement and soft-delete filtering.

**Independent Test**: Authenticated user queries `/api/v1/feed`, `/api/v1/feed/me`, and `/api/v1/feed/{uuid}` and sees only authorized, non-deleted items with pagination metadata.

### Tests for User Story 2

- [X] T040 [P] [US2] Add feed read and pagination route tests in server/src/test/kotlin/eu/vitamo/app/features/feed/routes/FeedReadRouteTest.kt
- [X] T041 [P] [US2] Add repository privacy filter tests in server/src/test/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepositoryPrivacyTest.kt

### Implementation for User Story 2

- [X] T042 [US2] Implement privacy-aware paginated read queries in server/src/main/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepository.kt
- [X] T043 [US2] Implement read feed use cases in server/src/main/kotlin/eu/vitamo/app/features/feed/usecase/GetFeedItemUseCase.kt and server/src/main/kotlin/eu/vitamo/app/features/feed/usecase/GetFeedPageUseCase.kt
- [X] T044 [US2] Implement GET /api/v1/feed, GET /api/v1/feed/me, and GET /api/v1/feed/{uuid} handlers in server/src/main/kotlin/eu/vitamo/app/features/feed/routes/FeedRoutes.kt
- [X] T045 [P] [US2] Implement app read-feed API and repository methods in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/KtorFeedApi.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/DefaultFeedRepository.kt
- [X] T046 [US2] Implement feed list ViewModel with pagination state in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/list/FeedListViewModel.kt
- [X] T047 [US2] Implement feed list Compose screen in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/list/FeedListScreen.kt
- [X] T048 [US2] Wire feed list navigation destination in app/shared/src/commonMain/kotlin/eu/vitamo/app/navigation/MainDestination.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/navigation/NavigationRoot.kt

**Checkpoint**: Read/query flow works independently with privacy and pagination guarantees.

---

## Phase 5: User Story 3 - Update Feed Item Content (Priority: P1)

**Goal**: Owners can update feed content/privacy/categories/media, while non-owners get `403`.

**Independent Test**: Owner updates an item and gets changed content + updatedAt; non-owner update attempt fails with `403`.

### Tests for User Story 3

- [X] T049 [P] [US3] Add update route authorization and validation tests in server/src/test/kotlin/eu/vitamo/app/features/feed/routes/FeedUpdateRouteTest.kt
- [X] T050 [P] [US3] Add update use-case tests in server/src/test/kotlin/eu/vitamo/app/features/feed/usecase/UpdateFeedItemUseCaseTest.kt

### Implementation for User Story 3

- [X] T051 [US3] Implement partial update persistence logic in server/src/main/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepository.kt
- [X] T052 [US3] Implement update feed use case with ownership checks in server/src/main/kotlin/eu/vitamo/app/features/feed/usecase/UpdateFeedItemUseCase.kt
- [X] T053 [US3] Implement PATCH /api/v1/feed/{uuid} handler in server/src/main/kotlin/eu/vitamo/app/features/feed/routes/FeedRoutes.kt
- [X] T054 [P] [US3] Implement app update-feed API and repository methods in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/KtorFeedApi.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/DefaultFeedRepository.kt
- [X] T055 [US3] Implement edit feed ViewModel in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/edit/FeedEditViewModel.kt
- [X] T056 [US3] Implement edit feed Compose screen in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/edit/FeedEditScreen.kt

**Checkpoint**: Update flow is independently complete and authorization-safe.

---

## Phase 6: User Story 4 - Soft-Delete Feed Item (Priority: P1)

**Goal**: Owners can soft-delete feed items via `deletedAt`, and deleted items disappear from all user-facing reads.

**Independent Test**: Owner delete returns `204`, then item no longer appears in list/detail endpoints for any user.

### Tests for User Story 4

- [X] T057 [P] [US4] Add delete route ownership and status-code tests in server/src/test/kotlin/eu/vitamo/app/features/feed/routes/FeedDeleteRouteTest.kt
- [X] T058 [P] [US4] Add soft-delete query exclusion regression tests in server/src/test/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepositorySoftDeleteTest.kt

### Implementation for User Story 4

- [X] T059 [US4] Implement soft-delete persistence logic in server/src/main/kotlin/eu/vitamo/app/features/feed/repository/ExposedFeedRepository.kt
- [X] T060 [US4] Implement delete feed use case with ownership checks in server/src/main/kotlin/eu/vitamo/app/features/feed/usecase/DeleteFeedItemUseCase.kt
- [X] T061 [US4] Implement DELETE /api/v1/feed/{uuid} handler in server/src/main/kotlin/eu/vitamo/app/features/feed/routes/FeedRoutes.kt
- [X] T062 [P] [US4] Implement app delete-feed API and repository methods in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/KtorFeedApi.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/DefaultFeedRepository.kt
- [X] T063 [US4] Add delete action handling in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/list/FeedListViewModel.kt
- [X] T064 [US4] Add delete confirmation and refresh UX in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/list/FeedListScreen.kt and app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/detail/FeedDetailScreen.kt

**Checkpoint**: Soft-delete behavior is independently complete and verified.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final hardening, performance, docs, and compliance checks across all stories.

- [X] T065 [P] Add feed endpoint integration suite covering create/read/update/delete lifecycle in server/src/test/kotlin/eu/vitamo/app/features/feed/routes/FeedFlowIntegrationTest.kt
- [X] T066 [P] Add feed query performance indexes migration in server/src/main/resources/db/migration/V4__feed_query_indexes.sql
- [X] T067 Harden feed error mapping to avoid internal leakage in server/src/main/kotlin/eu/vitamo/app/modules/StatusPagesModule.kt
- [X] T068 [P] Update feed quickstart validation steps in specs/001-feed-items/quickstart.md
- [X] T069 [P] Update feature plan constitution alignment notes after implementation in specs/001-feed-items/plan.md
- [X] T070 Verify no manual webApp changes and record scope compliance in specs/001-feed-items/spec.md

---

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1) → Foundational (Phase 2) → User Stories (Phases 3-6) → Polish (Phase 7)
- User story phases depend on Foundational completion.
- US3 depends on US1 + US2 behavior (item creation and read visibility).
- US4 depends on US2 read-query behavior (soft-delete exclusion verification).

### User Story Dependency Graph

- **US1 (Create)**: starts after Phase 2
- **US2 (Read/Query)**: starts after Phase 2 (can run in parallel with US1 once foundational contracts/schema are complete)
- **US3 (Update)**: depends on US1 + US2
- **US4 (Soft-Delete)**: depends on US2 (and uses US1-created data in tests)

---

## Parallel Execution Examples

### US1 Parallel Example

- Run T030 and T031 in parallel.
- Run T037 in parallel with T038 after T035 is in place.

### US2 Parallel Example

- Run T040 and T041 in parallel.
- Run T045 in parallel with T046 after T044 is merged.

### US3 Parallel Example

- Run T049 and T050 in parallel.
- Run T054 in parallel with T055 after T053 is merged.

### US4 Parallel Example

- Run T057 and T058 in parallel.
- Run T062 in parallel with T063 after T061 is merged.

---

## Implementation Strategy

### MVP First (US1)

1. Complete Phase 1 and Phase 2.
2. Deliver Phase 3 (US1 Create Feed Item).
3. Validate create flow independently (tests + quickstart scenario).
4. Demo/ship MVP increment.

### Incremental Delivery

1. Add US2 read/query and validate privacy + pagination.
2. Add US3 update and validate ownership enforcement.
3. Add US4 soft-delete and validate global exclusion behavior.
4. Finish with Phase 7 polish tasks.

### Team Parallelization

1. Team completes Setup + Foundational together.
2. Split after Phase 2:
   - Engineer A: US1
   - Engineer B: US2
3. After US1/US2:
   - Engineer A: US3
   - Engineer B: US4

---

## Format Validation Checklist

- [x] Every task uses `- [ ] T### ...` checklist format.
- [x] `[US#]` labels are used only in user story phases.
- [x] `[P]` markers are only applied to parallelizable tasks.
- [x] Every task description includes at least one exact file path.
