---
description: "Constitution-aligned task list template for KMP feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`  
**Required prerequisites**: `spec.md`, `plan.md`  
**Conditional prerequisites**: `research.md`, `data-model.md`, `contracts/`, `quickstart.md`  
**Constitution version**: [CURRENT VERSION]

## Task Generation Rules

Tasks MUST:

- be grouped into independently deliverable user-story slices;
- include exact existing repository file paths;
- preserve established module ownership and dependency direction;
- include tests whenever required by the constitution;
- include Flyway work for every schema change;
- include compatibility work for contract changes;
- leave `webApp` unchanged unless the specification explicitly includes it;
- include final Gradle verification and delivery evidence.

Tests are NOT optional merely because the feature request did not mention them. Test tasks
MUST be generated for business-critical logic, non-trivial business rules, contracts,
serialization, authorization, security-sensitive validation, persistence mappings, database
migrations, and reproducible bug fixes.

A required test may be omitted only when `plan.md` documents the exact reason, uncovered
behavior, residual risk, alternative verification, and follow-up task.

Do not generate:

- generic project-initialization tasks for infrastructure that already exists;
- speculative abstractions or new libraries not justified in `plan.md`;
- direct edits to generated files when a canonical source/generation task exists;
- edits to historical Flyway migrations;
- `webApp` implementation tasks unless explicitly in scope;
- commit, push, merge, rebase, reset, clean, or stash tasks unless explicitly requested.

## Format: `[ID] [P?] [Story?] Description with exact path`

- **[P]**: May run in parallel because it touches different files and has no unmet dependency.
- **[US#]**: User story traceability label.
- Setup, foundation, migration, and final verification tasks may omit a story label.
- Use sequential identifiers: `T001`, `T002`, and so on.

## KMP Path Conventions

Use actual paths from the repository. Typical ownership is:

```text
core/src/commonMain/           # contracts, shared domain/value objects, pure rules
core/src/commonTest/           # tests for platform-neutral core behavior

app/shared/src/commonMain/     # shared UI, ViewModels, use cases, repositories, Ktor client
app/shared/src/commonTest/     # shared app tests
app/shared/src/androidMain/    # genuine Android implementations
app/shared/src/androidTest/    # Android-specific shared-module tests
app/shared/src/iosMain/        # genuine iOS implementations
app/shared/src/iosTest/        # iOS-specific shared-module tests

app/androidApp/                # thin Android entry point and platform configuration
app/iosApp/                    # thin iOS entry point and platform configuration

server/src/main/               # routes, use cases/services, repositories, persistence
server/src/test/               # server tests
server/src/main/resources/db/migration/  # new Flyway migrations

webApp/                        # frozen unless explicitly in specification scope
```

## Phase 1: Inspection and Feature Preparation

**Purpose**: Confirm scope and existing patterns before editing.

- [ ] T001 Inspect repository instructions, `settings.gradle.kts`, affected build files, and current Git state
- [ ] T002 Inspect existing implementations, tests, contracts, and migrations related to the feature
- [ ] T003 Confirm affected modules and allowed dependency flow from `plan.md`
- [ ] T004 Confirm `webApp` is out of scope or cite the explicit specification requirement that includes it
- [ ] T005 Validate the pre-implementation Constitution Check and resolve every `FAIL`

**Checkpoint**: Scope, ownership, compatibility obligations, and existing patterns are known.

---

## Phase 2: Shared Contracts and Data Foundations *(include only when required)*

**Purpose**: Establish canonical boundaries before dependent implementation.

### Contracts and Shared Models

- [ ] T006 [P] Add or update canonical `@Serializable` contract in `core/src/commonMain/[exact-path].kt`
- [ ] T007 [P] Add or update shared domain/value object/pure validation in `core/src/commonMain/[exact-path].kt`
- [ ] T008 [P] Add serialization and backward-compatibility tests in `core/src/commonTest/[exact-path].kt`
- [ ] T009 Update canonical generation source and run generation task when generated consumers are affected

### Database and Migration

- [ ] T010 Add a new immutable Flyway migration in `server/src/main/resources/db/migration/[version]__[description].sql`
- [ ] T011 Add or update Exposed table/entity mapping in `server/src/main/[exact-path].kt`
- [ ] T012 Add migration verification for a clean database and the previously supported schema state
- [ ] T013 Document backfill, rollout, transaction, and rollback/forward-fix behavior in `specs/[###-feature-name]/quickstart.md`

**Checkpoint**: Contracts and schema foundations are backward-compatible and independently verified.

---

## Phase 3: User Story 1 - [TITLE] (Priority: P1) 🎯 MVP

**Goal**: [User-visible value delivered by this story.]  
**Independent Test**: [Specific action and observable result.]

### Tests for User Story 1

<!-- Include all constitution-triggered tests. They may be implemented before or alongside code,
but the story is not complete until they pass. -->

- [ ] T014 [P] [US1] Add shared logic/ViewModel/use-case test in `[exact-test-path]`
- [ ] T015 [P] [US1] Add server route/use-case/authorization test in `[exact-test-path]`
- [ ] T016 [P] [US1] Add contract or serialization test in `[exact-test-path]`
- [ ] T017 [P] [US1] Add regression test reproducing `[bug]` in `[exact-test-path]` *(bug fixes only)*

### Implementation for User Story 1

- [ ] T018 [P] [US1] Implement shared app behavior in `app/shared/src/commonMain/[exact-path].kt`
- [ ] T019 [P] [US1] Implement server use case/service in `server/src/main/[exact-path].kt`
- [ ] T020 [US1] Implement server repository/persistence mapping in `server/src/main/[exact-path].kt`
- [ ] T021 [US1] Add thin Ktor route mapping in `server/src/main/[exact-path].kt`
- [ ] T022 [US1] Add Android/iOS platform wiring only where genuine divergence exists in `[exact-paths]`
- [ ] T023 [US1] Verify authorization, privacy, safe errors, and sensitive-data logging behavior
- [ ] T024 [US1] Run story-specific tests and record results

**Checkpoint**: User Story 1 works and can be demonstrated independently.

---

## Phase 4: User Story 2 - [TITLE] (Priority: P2)

**Goal**: [User-visible value.]  
**Independent Test**: [Specific action and observable result.]

### Tests for User Story 2

- [ ] T025 [P] [US2] Add required shared/app tests in `[exact-test-path]`
- [ ] T026 [P] [US2] Add required server/security/persistence tests in `[exact-test-path]`
- [ ] T027 [P] [US2] Add required contract/compatibility tests in `[exact-test-path]`

### Implementation for User Story 2

- [ ] T028 [P] [US2] Implement shared app behavior in `[exact-path]`
- [ ] T029 [P] [US2] Implement server business behavior in `[exact-path]`
- [ ] T030 [US2] Implement persistence or integration behavior in `[exact-path]`
- [ ] T031 [US2] Add thin route/platform wiring in `[exact-path]`
- [ ] T032 [US2] Run story-specific tests and record results

**Checkpoint**: User Stories 1 and 2 remain independently testable.

---

## Phase 5: User Story 3 - [TITLE] (Priority: P3)

**Goal**: [User-visible value.]  
**Independent Test**: [Specific action and observable result.]

### Tests for User Story 3

- [ ] T033 [P] [US3] Add required shared/app tests in `[exact-test-path]`
- [ ] T034 [P] [US3] Add required server/security/persistence tests in `[exact-test-path]`
- [ ] T035 [P] [US3] Add required contract/compatibility tests in `[exact-test-path]`

### Implementation for User Story 3

- [ ] T036 [P] [US3] Implement shared app behavior in `[exact-path]`
- [ ] T037 [P] [US3] Implement server business behavior in `[exact-path]`
- [ ] T038 [US3] Implement persistence or integration behavior in `[exact-path]`
- [ ] T039 [US3] Add thin route/platform wiring in `[exact-path]`
- [ ] T040 [US3] Run story-specific tests and record results

**Checkpoint**: All selected stories are independently functional.

---

[Add or remove story phases and renumber tasks based on the actual specification.]

## Final Phase: Cross-Cutting Quality and Delivery

- [ ] TXXX Verify no forbidden module dependency or server/platform type leaked into `core`
- [ ] TXXX Verify `webApp` has no modifications unless explicitly included
- [ ] TXXX Verify active-client compatibility for contract, enum, sealed-type, and default-value changes
- [ ] TXXX Verify no historical Flyway migration was edited
- [ ] TXXX Run clean-database and previous-schema migration checks where applicable
- [ ] TXXX Run affected `core` and `app/shared` tests
- [ ] TXXX Run affected server tests
- [ ] TXXX Run Android compilation/tests
- [ ] TXXX Run supported iOS/shared compilation/tests
- [ ] TXXX Run contract generation/verification tasks where applicable
- [ ] TXXX Remove temporary logging, debug output, and dead code
- [ ] TXXX Update `quickstart.md`, contracts, and architecture documentation where behavior changed
- [ ] TXXX Complete the post-design/final Constitution Check
- [ ] TXXX Record changed files, rationale, assumptions, commands, results, skipped checks, and residual risks

## Dependencies and Execution Order

### Phase Dependencies

- **Inspection and Preparation** starts first and blocks implementation.
- **Contracts/Data Foundations** run before stories that depend on them.
- **User stories** may run in parallel only when they touch different files and do not share an
  unfinished contract, migration, or foundational dependency.
- **Final Quality and Delivery** runs after all selected stories.

### Within a User Story

- Canonical contracts and required migrations precede dependent implementation.
- Pure/domain rules precede adapters that call them.
- Use cases/services precede thin routes and platform wiring.
- Tests may be written before or alongside implementation, but all required tests MUST pass
  before the story checkpoint.
- A task marked `[P]` MUST not edit the same file as another concurrently executed task.

## Implementation Strategy

### MVP First

1. Complete inspection and required foundations.
2. Implement User Story 1.
3. Run all User Story 1 tests and applicable compilation.
4. Validate User Story 1 independently.
5. Continue only when the next story is desired.

### Incremental Delivery

Each added user story MUST:

- add independently demonstrable value;
- preserve earlier stories;
- preserve active-client compatibility;
- pass its tests and affected builds;
- avoid unrelated refactors.

## Notes

- Replace every placeholder with feature-specific content and exact paths.
- Remove unused conditional sections and sample tasks.
- Do not claim a check passed without evidence.
- Do not use editor/LSP feedback as a substitute for Gradle verification.
- Do not create governance exceptions implicitly; they must already be documented and approved.