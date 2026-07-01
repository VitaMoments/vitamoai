<!--
Sync Impact Report
- Version change: unversioned -> 1.0.0
- Modified principles:
  - Template Principle 1 -> I. Module Boundary Integrity
  - Template Principle 2 -> II. Shared Contract Canonicality
  - Template Principle 3 -> III. SIMPLE Delivery Discipline
  - Template Principle 4 -> IV. Security-First Server Enforcement
  - Template Principle 5 -> V. Testable Incremental Quality
- Added sections:
  - Technical & Architecture Guardrails
  - Workflow and Review Standards
- Removed sections:
  - None
- Templates requiring updates:
  - ✅ .specify/templates/plan-template.md
  - ✅ .specify/templates/spec-template.md
  - ✅ .specify/templates/tasks-template.md
  - ⚠ pending .specify/templates/commands/*.md (directory not present)
- Follow-up TODOs:
  - None
-->
# VitamoAI Constitution

## Core Principles

### I. Module Boundary Integrity
Code placement MUST respect module ownership. `core` MUST contain only platform-neutral,
shared Kotlin code such as serializable API contracts, serializers, shared error/result
models, and pure utilities. `app/shared` MUST hold shared app logic and client behavior.
`app/androidApp` and `app/iosApp` MUST contain only platform startup and wiring.
`server` MUST contain server runtime concerns such as routes, auth, persistence, and
infrastructure. Cross-module leakage is forbidden unless explicitly approved as a documented, 
time-bounded exception in the implementation plan or pull request.

Rationale: strict boundaries preserve maintainability, testability, and KMP correctness.

### II. Shared Contract Canonicality
App-server request and response contracts MUST be canonical `@Serializable` models in
`core` when shared by both sides. DTOs MUST NOT be introduced unless a distinct mapping
need is explicitly documented (e.g., third-party API mapping, persistence boundaries, or
migration compatibility). Server database entities and sensitive internals MUST NEVER be
exposed directly as API responses. Removing, renaming, or changing the meaning of serialized fields 
is a breaking change and MUST be documented with migration behavior.

Rationale: one source of truth avoids drift, unsafe exposure, and integration defects.

### III. SIMPLE Delivery Discipline
All implementation work MUST follow SIMPLE: Small scope, Inspect first, Minimal change,
Preserve boundaries, Leave notes, Explicit tests. Contributors MUST inspect existing files
before editing, avoid speculative abstractions, and keep changes tightly scoped to the
requested outcome. Summaries of changed files, rationale, assumptions, and recommended
Gradle verification command MUST be provided with each delivery.

Rationale: disciplined iteration reduces regressions and keeps velocity predictable.

### IV. Security-First Server Enforcement
Authentication, authorization, and authoritative security-sensitive validation MUST remain 
server-side. Client-side validation MAY be used for user experience, but MUST NOT be trusted
for security decisions.. Secrets
MUST be supplied via environment variables (or local `.env` for development) and MUST NOT
be committed. Logs and responses MUST exclude tokens, passwords, password hashes, refresh
tokens, verification codes, and internal stack traces. CORS with credentials MUST use
explicit allowed origins and MUST NOT use wildcard hosts.

Rationale: centralized enforcement and safe handling prevent data leakage and abuse.

### V. Testable Incremental Quality
Tests MUST be added for business-critical logic, contract changes, authorization behavior, and 
non-trivial business rules. When tests are skipped, the reason MUST be documented. Shared logic 
tests belong in `commonTest`; server behavior tests belong in server test suites. Skipped tests 
MUST include an explicit reason.

Rationale: incremental quality gates support safe MVP-first delivery and reliable evolution.

## Technical & Architecture Guardrails

- Kotlin Multiplatform source-set rules are mandatory: shared code in `commonMain`,
  platform-specific code in `androidMain`/`iosMain` (or equivalent), and `expect/actual`
  only for real platform divergence.
- `core` MUST remain free of server-only or platform-only dependencies.
- Server-only infrastructure (PostgreSQL, Exposed, Flyway, HikariCP, Ktor server setup)
  MUST remain in `server`.
- App-side Ktor client configuration MUST remain in `app/shared`; Ktor server
  configuration MUST remain in `server`.
- Serialization conventions MUST remain consistent:
  - sealed class discriminator: `"type"`
  - unknown-key tolerance for API compatibility
  - UUID serialized as string
  - temporal values use ISO-8601 and appropriate temporal type (`Instant`, `LocalDate`,
    `LocalTime`) based on semantics.
- Koin containers for app and server MUST remain separate; expensive startup side effects
  MUST NOT be hidden inside DI declarations.
- Architecture layering is required:
  - app side: UI -> ViewModel -> UseCase -> Repository -> API/DataSource
  - server side: Route -> UseCase -> Repository -> Exposed/entity mapping

## Workflow and Review Standards

- Start by inspecting relevant files and preserving established package/module structure.
- Keep routes/controllers thin; place business logic in use cases/services.
- Do not introduce new libraries or broad refactors without explicit justification.
- Every database schema change MUST include a Flyway migration before runtime use.
- Destructive migrations or data backfills MUST include rollback or mitigation notes.
- Every pull request or feature plan MUST include a constitution check against the five core
  principles and this section's guardrails.
- Reviews MUST reject non-compliant changes unless a documented governance exception exists.

## Governance

This constitution is the authoritative engineering policy for VitamoAI and supersedes
conflicting local conventions or ad-hoc practices.

Amendment process:
- Amendments MUST include a clear rationale, impacted sections, and migration guidance for
  affected templates or workflows.
- Amendments MUST be approved through repository review before merge.
- Each amendment MUST update the Sync Impact Report at the top of this file.

Versioning policy:
- Semantic versioning is mandatory for this constitution.
- MAJOR: backward-incompatible governance changes or principle removals/redefinitions.
- MINOR: new principle/section or materially expanded mandatory guidance.
- PATCH: clarifications, wording improvements, and non-semantic edits.

Compliance review expectations:
- Every implementation plan MUST pass constitution gates before design starts and after
  design completion.
- Every task list MUST reflect boundary, security, and test obligations from this
  constitution.
- Runtime guidance files and templates MUST be synchronized when constitutional policy
  changes.

**Version**: 1.0.0 | **Ratified**: 2026-07-01 | **Last Amended**: 2026-07-01
