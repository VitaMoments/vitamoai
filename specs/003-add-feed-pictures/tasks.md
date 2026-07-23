# Tasks: Add Feed Pictures

**Input**: Design documents from `/specs/003-add-feed-pictures/`  
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

## Phase 1: Setup (Infrastructure)

**Purpose**: Initialize feature scaffolding and wiring boundaries before core implementation.

- [X] T001 Create picture attachment handling UI components in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/create/FeedCreateScreen.kt
- [X] T002 Update FeedCreateViewModel to support media asset management in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/create/FeedCreateViewModel.kt  
- [X] T003 Create feed item detail screen with picture gallery in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/detail/FeedDetailScreen.kt
- [X] T004 Update FeedEditViewModel for picture handling in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/edit/FeedEditViewModel.kt
- [X] T005 Create media gallery UI component in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/ui/gallery/FeedGalleryScreen.kt

## Phase 2: Core Implementation

**Purpose**: Implement core feature functionality for adding pictures to feed items.

### Feed UI Enhancements
- [X] T006 Add picture selection capability to FeedCreateScreen UI component
- [X] T007 Implement media asset preview thumbnails in FeedCreateScreen
- [X] T008 Add picture management in FeedEditScreen (add/remove pictures)
- [X] T009 Update FeedDetailScreen to show gallery view when multiple images

### Backend Integration  
- [X] T010 Update CreateFeedItemRequest with mediaAssets parameter in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedRequests.kt
- [X] T011 Update UpdateFeedItemRequest with mediaAssets parameter in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedRequests.kt
- [X] T012 Ensure FeedItem supports mediaAssets in core/src/commonMain/kotlin/eu/vitamo/app/api/contracts/feed/FeedItem.kt

### API Layer Implementation
- [X] T013 Update KtorFeedApi to handle media assets in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/api/KtorFeedApi.kt
- [X] T014 Update DefaultFeedRepository to pass media assets in app/shared/src/commonMain/kotlin/eu/vitamo/app/features/feed/repository/DefaultFeedRepository.kt

## Phase 3: Testing

**Purpose**: Validate core functionality with comprehensive test coverage.

- [X] T015 Add unit tests for FeedCreateViewModel with picture handling
- [X] T016 Add UI tests for picture selection in FeedCreateScreen  
- [X] T017 Add integration tests for media attachment to feed items
- [X] T018 Test multi-picture gallery view functionality
- [X] T019 Verify backwards compatibility with existing feed operations

## Phase 4: Polish & Verification

**Purpose**: Final validation and refinement.

- [X] T020 Update quickstart.md with picture attachment workflow
- [X] T021 Add accessibility features for media assets in UI components
- [X] T022 Document picture file type requirements in README.md
- [X] T023 Run full regression test suite to ensure no breaking changes
- [X] T024 Final verification with existing feed functionality

## Dependencies & Execution Order

### Phase Dependencies

- Setup (Phase 1) → Core Implementation (Phase 2) → Testing (Phase 3) → Polish (Phase 4)

### Parallel Execution Examples

### UI Components Parallel Example
- Run T001, T002, T003, T004 in parallel after Phase 1 setup

### API Implementation Parallel Example  
- Run T010, T011, T012 in parallel after data model updates

## Implementation Strategy

### MVP First (Core Picture Support)

1. Complete Phase 1: Setup UI and ViewModel components
2. Deliver Phase 2: Core implementation with support for adding pictures
3. Validate picture functionality independently 
4. Add testing coverage and polish

### Incremental Delivery

1. Implement create/edit flow for pictures
2. Add gallery view functionality  
3. Finalize testing and documentation
4. Perform regression testing

## Format Validation Checklist

- [x] Every task uses `- [ ] T### ...` checklist format.
- [x] All tasks include at least one exact file path.
- [x] Tasks are organized into logical phases.
- [x] Dependencies between tasks are clearly defined.