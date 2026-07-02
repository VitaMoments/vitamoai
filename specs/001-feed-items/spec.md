# Feature Specification: User Feed Items

**Feature Branch**: `001-feed-items`

**Created**: 2026-03-19

**Status**: Draft

**Input**: User description: "A user must be able to create, read, update and soft-delete new feeditems. Feed items should have a BaseFeedItem interface with content."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Feed Item (Priority: P1)

A user can create a new feed item with rich text content, specify privacy settings, and optionally categorize the item. This is the core capability that enables users to share content within the platform.

**Why this priority**: Content creation is the fundamental value proposition of the feed system. Without this, users cannot share experiences, thoughts, or wellness updates.

**Independent Test**: Can be fully tested by creating a new feed item with required fields (content, privacy) and verifying it appears in the user's feed with correct metadata.

**Acceptance Scenarios**:

1. **Given** an authenticated user is on the feed creation screen, **When** they enter rich text content and select a privacy level, **Then** the system creates a new feed item with a unique UUID, sets the author to the current user, assigns current timestamp to `createdAt`, and saves the item.

2. **Given** a user creates a feed item with categories (e.g., MENTAL, FITNESS), **When** the item is saved, **Then** the system persists the selected categories and they are queryable as part of the feed item.

3. **Given** a user attempts to create a feed item without content, **When** they submit the form, **Then** the system displays a validation error and prevents submission.

---

### User Story 2 - Read/Query Feed Items (Priority: P1)

A user can retrieve and view feed items created by themselves and other users according to privacy permissions. This enables browsing, discovering, and engaging with content on the platform.

**Why this priority**: Reading/browsing is equally critical as creation—users must be able to view feed content to derive value from the system.

**Independent Test**: Can be fully tested by retrieving feed items for the authenticated user (respecting privacy boundaries) and verifying correct data structure, filtering, and pagination.

**Acceptance Scenarios**:

1. **Given** an authenticated user queries their own feed, **When** the system returns all non-deleted feed items where `deletedAt` is null and authored by the user, **Then** results include all required fields (uuid, author, content, privacy, timestamps, categories).

2. **Given** a user queries the general feed with `FRIENDS_ONLY` privacy items, **When** the user is not in the author's friend list, **Then** those items are excluded from results.

3. **Given** a user queries the feed, **When** the system returns results, **Then** results are paginated with a default limit (e.g., 20 items) and support cursor or offset-based pagination.

4. **Given** a feed item has `deletedAt` set to a non-null timestamp, **When** a user queries the feed, **Then** that item is excluded from all results (soft-delete is respected).

---

### User Story 3 - Update Feed Item Content (Priority: P1)

A user can edit their own feed item's content, privacy level, categories, and media assets after creation. This allows users to correct mistakes or evolve their shared content.

**Why this priority**: The ability to edit is essential for content accuracy and user trust. Users must be able to fix typos, update information, or change privacy settings.

**Independent Test**: Can be fully tested by updating an existing feed item (content/privacy/categories), verifying the update persists, and confirming `updatedAt` timestamp changes while `createdAt` and `uuid` remain constant.

**Acceptance Scenarios**:

1. **Given** an authenticated user owns a feed item, **When** they update the content or categories, **Then** the system updates the item, sets `updatedAt` to the current timestamp, and preserves `uuid`, author, and `createdAt`.

2. **Given** a user attempts to update a feed item they do not own, **When** they submit an update request, **Then** the system rejects the request with a 403 Forbidden response.

3. **Given** a user updates their feed item's privacy from `PUBLIC` to `FRIENDS_ONLY`, **When** the change is saved, **Then** the system immediately enforces the new privacy level on subsequent queries.

4. **Given** a user updates a feed item with invalid content, **When** they submit, **Then** the system returns a validation error and does not apply the update.

---

### User Story 4 - Soft-Delete Feed Item (Priority: P1)

A user can delete their own feed items by marking them with a `deletedAt` timestamp. Deleted items remain in the database but are excluded from all user-facing queries. This preserves referential integrity and audit trails.

**Why this priority**: Deletion capability is critical for privacy and content management. Soft-delete preserves data consistency and enables potential recovery or compliance audits.

**Independent Test**: Can be fully tested by deleting a feed item, verifying `deletedAt` is set to a non-null value, confirming the item no longer appears in queries, and checking that other users' queries also exclude the deleted item.

**Acceptance Scenarios**:

1. **Given** an authenticated user owns a feed item, **When** they delete it, **Then** the system sets `deletedAt` to the current timestamp and preserves all other fields (uuid, content, author, etc.).

2. **Given** a feed item has been soft-deleted, **When** any user queries the feed, **Then** the deleted item is excluded from results.

3. **Given** a user attempts to delete a feed item they do not own, **When** they submit a delete request, **Then** the system rejects the request with a 403 Forbidden response.

4. **Given** a user deletes their own feed item, **When** they attempt to re-query their feed history, **Then** the deleted item does not appear in any user-facing results.

---

### Edge Cases

- What happens when a user attempts to create a feed item with an extremely large rich text document (e.g., 10 MB)?
- How does the system handle concurrent updates to the same feed item (e.g., two clients updating simultaneously)?
- Can a user permanently delete their feed item from the database, or is soft-delete the only deletion mechanism?
- What happens if a user updates a feed item immediately after creating it (within milliseconds)?
- How are feed items handled when a user account is deleted—are they preserved, anonymized, or hard-deleted?

## Scope

This feature updates only the Kotlin Multiplatform app and the server.

In scope:
- `core`: shared `@Serializable` API contracts used by app and server
- `server`: routes, use cases, repositories, authorization, persistence, Flyway migrations, and server tests
- `app/shared`: KMP app-side state, use cases, repositories/API calls, ViewModels, and Compose Multiplatform UI where needed
- `app/androidApp`: Android-specific wiring only if required
- `app/iosApp`: iOS-specific wiring only if required

Out of scope:
- `webApp`
- Web UI changes
- React/Vite/TypeScript implementation changes
- Web-specific routing, pages, components, hooks, or styling
- Comment/reply functionality
- Feed item recovery UI
- Rate limiting
- Media upload/storage implementation

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow authenticated users to create a new feed item with rich text content, privacy level, and optional categories.
- **FR-002**: System MUST assign a unique UUID to each feed item at creation time that remains immutable throughout the item's lifecycle.
- **FR-003**: System MUST set `createdAt` and `updatedAt` timestamps in ISO-8601 format (using `Instant`) at creation and on every modification.
- **FR-004**: System MUST allow feed items to have zero or more associated categories from the `FeedCategory` enum (MENTAL, PHYSICAL, FOOD, LIFESTYLE, MINDFULNESS, HABITS, SLEEP, ENERGY, RELATIONSHIPS, COMMUNITY, PURPOSE, PERSONAL_GROWTH, REFLECTION).
- **FR-005**: System MUST allow feed items to have zero or more media assets (images, videos, documents) attached and queryable as part of the item.
- **FR-006**: System MUST enforce privacy permissions on read operations: `PUBLIC` items are visible to all authenticated users, `FRIENDS_ONLY` items are visible only to the author's friends, and `PRIVATE` items are visible only to the author.
- **FR-007**: System MUST allow authenticated users to update their own feed items (content, privacy, categories, media assets) and reject updates by non-owners with 403 Forbidden.
- **FR-008**: System MUST allow authenticated users to soft-delete their own feed items by setting `deletedAt` to the current timestamp.
- **FR-009**: System MUST exclude all feed items where `deletedAt` is not null from all user-facing queries, ensuring soft-deleted items are invisible.
- **FR-010**: System MUST validate rich text content to ensure it conforms to the `RichTextDocument` schema (type and content fields).
- **FR-011**: System MUST support pagination on feed queries (e.g., offset or cursor-based) with reasonable defaults and configurable limits.
- **FR-012**: System MUST preserve audit trails for all feed item operations (create, update, soft-delete) via timestamp fields and avoid hard deletion unless explicitly required.
- **FR-013**: System MUST serialize feed items to JSON using the `BaseFeedItem` interface contract in `core` for all API responses, ensuring schema consistency.

### Constitution Alignment *(mandatory)*

- **CA-001 Boundary Integrity**: `BaseFeedItem` and public `FeedItem` response contracts MUST be defined in `core` as `@Serializable` models. Server database entities and persistence models MUST remain in `server`.
- **CA-002 Shared Contracts**: Feed item request/response DTOs MUST NOT be created; `BaseFeedItem` and `FeedItem` from `core` MUST be canonical. Any server-only internal fields (e.g., database-specific surrogate keys) MUST NOT leak into API responses.
- **CA-003 Security Enforcement**: Authorization checks for read, update, and delete operations MUST be performed server-side. Privacy validation and ownership verification MUST happen before any operation. Client-side UI may filter visibility for UX, but server MUST enforce all security rules. Error responses MUST NOT expose internal details.
- **CA-004 Incremental Testability**: Each user story above defines an independently testable flow. Tests for create, read, update, and delete MUST be added to the server test suite. Authorization boundary tests MUST verify that non-owners cannot modify or delete items, and privacy rules are enforced.
- **CA-005 WebApp Exclusion**: This feature MUST NOT modify `webApp` source files. Any TypeScript contract generation that happens as part of the existing build pipeline is allowed only as generated output, not as manual web implementation work.
- 
### Key Entities

- **BaseFeedItem**: Sealed interface defining the contract for all feed item types. Immutable attributes: uuid (unique identifier), author (User object), content (RichTextDocument), privacy (PrivacyStatus), createdAt (Instant), updatedAt (Instant), deletedAt (Instant or null). This is the canonical shared contract in `core`.
- **FeedItem**: Concrete implementation of `BaseFeedItem`. Additional attributes: categories (List of FeedCategory), mediaAssets (List of MediaAsset). Stored in server database and returned in API responses.
- **RichTextDocument**: Represents the content structure with type (document type hint) and content (serialized JSON element). Validated on server before persistence.
- **FeedCategory**: Enum categorizing feed items by wellness domain (MENTAL, PHYSICAL, FOOD, LIFESTYLE, MINDFULNESS, HABITS, SLEEP, ENERGY, RELATIONSHIPS, COMMUNITY, PURPOSE, PERSONAL_GROWTH, REFLECTION). Supports flexible tagging without strict hierarchy.
- **PrivacyStatus**: Enum controlling visibility (PUBLIC, FRIENDS_ONLY, PRIVATE, or similar variants per project requirements). Enforced on server at query and response time.
- **User**: Represents the author of a feed item. Embedded in FeedItem responses for context.
- **MediaAsset**: Represents attached media (images, videos, etc.). Association is optional and stored as a list on FeedItem.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a feed item with rich text and privacy settings in under 2 seconds from submission to confirmation.
- **SC-002**: System returns paginated feed queries (20 items) in under 500ms for typical user scenarios (1000+ items in database).
- **SC-003**: Authorization checks (ownership, privacy) are enforced server-side on 100% of protected operations (update, delete).
- **SC-004**: Soft-deleted feed items are excluded from 100% of user-facing queries and do not reappear until explicitly recovered (if recovery feature is implemented).
- **SC-005**: Feed item schema aligns with the `BaseFeedItem` contract in `core`; app and server share the same canonical model with zero drift.
- **SC-006**: All CRUD operations (create, read, update, soft-delete) include automated test coverage with at least one test per acceptance scenario.
- **SC-007**: Users can successfully manage their own feed items (edit, delete) and receive clear error messages when attempting unauthorized actions.

## Assumptions

- **User Authentication**: The platform has an existing authentication system (session-based or token-based) and the server can reliably identify the authenticated user for each request.
- **User Relationships**: If privacy level includes `FRIENDS_ONLY`, the platform has a friends/connections system that the server can query to validate visibility.
- **Rich Text Format**: The `RichTextDocument` structure with type and JSON content is flexible enough to support various content representations (markdown, plain text, HTML, etc.) without schema migration.
- **Media Handling**: If media assets are attached, the platform has a separate media storage/service (not part of this feature). This spec treats `MediaAsset` objects as references; upload and storage are out of scope.
- **Soft-Delete Only**: Hard deletion is not required for MVP. If compliance or data retention policies require hard deletion, that is a separate feature.
- **No Versioning**: Feed item edit history and version control are out of scope. Edits overwrite the previous state and only `updatedAt` timestamp marks the change.
- **Pagination Default**: If not specified by user, feed queries default to returning 20 items per page with offset-based or cursor-based pagination.
- **Timestamp Precision**: All timestamps use ISO-8601 format with `Instant` type for server-side consistency and millisecond precision.
- **Privacy Model**: The `PrivacyStatus` enum supports at least PUBLIC, FRIENDS_ONLY, and PRIVATE levels. Additional levels (FOLLOWERS, CUSTOM_LIST) can be added in future iterations.
- **Database Constraints**: The server database supports nullable timestamp columns for `deletedAt` and can efficiently filter on this field. Indexes on (author, createdAt) and (author, deletedAt) are recommended for query performance.

## Clarifications Resolved ✅

All ambiguities have been addressed and encoded:

### Clarification 1: Deletion Recovery
**Decision**: Permanent soft-delete (database backup only)  
**Rationale**: Aligns with typical social platform design (Facebook, Twitter). Simpler MVP implementation. Avoids cleanup complexity. Database/backup recovery remains available for compliance audits or accidental deletion scenarios. User/admin recovery can be added in future iterations if required.  
**Impact**: No additional recovery fields or endpoints needed. `deletedAt` timestamp is final; manual database restoration is the only recovery mechanism.

### Clarification 2: Nested Interactions (Comments/Replies)
**Decision**: Out of scope for MVP  
**Rationale**: Feed items CRUD is the core value proposition. Comments/replies are a separate engagement layer and introduce schema complexity (nested relationships, threading, pagination). Will be designed as a separate feature.  
**Impact**: `BaseFeedItem` and `FeedItem` do not include comment/reply fields. No comment endpoints or service logic in this feature scope.

### Clarification 3: Rate Limiting
**Decision**: No rate limits for MVP  
**Rationale**: Platform load characteristics unknown. Rate limiting can be added later based on production metrics. Initial implementation focuses on correctness and user experience without throttling.  
**Impact**: No rate limiting logic required. Future iteration: add token bucket or sliding window throttling when needed.

## Implementation scope compliance (2026-07-01)

- Verified: no manual changes were made in `webApp/`.
- Feed feature implementation is limited to `core`, `server`, `app/shared`, and feature docs.
