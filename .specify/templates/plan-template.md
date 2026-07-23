# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]  
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled by `/speckit.plan`. The Constitution Check is a hard gate
before Phase 0 research and MUST be re-evaluated after Phase 1 design.

## Summary

[Primary requirement, affected product surfaces, and concise technical approach.]

## Technical Context

**Kotlin / KMP Version**: [Version from version catalog/build files or NEEDS CLARIFICATION]  
**Targets**: [Android / iOS / JVM server / other applicable targets]  
**Primary Dependencies**: [Ktor, Koin, Kotlin Serialization, Exposed, Flyway, and feature-specific existing dependencies]  
**Storage**: [PostgreSQL / local storage / none / N/A]  
**Testing**: [commonTest, Android tests, iOS tests, server tests, migration/contract tests as applicable]  
**Target Platforms**: [Supported Android/iOS/server environments]  
**Performance Goals**: [Feature-specific measurable targets or N/A with reason]  
**Constraints**: [Offline behavior, compatibility, privacy, latency, memory, rollout, or N/A]  
**Scale / Scope**: [Expected users, records, request rate, screens, or bounded feature scope]  
**LSP Availability**: [Available / unavailable; Gradle remains the source of truth]

## Feature Scope and Module Impact

| Module / Consumer | In Scope? | Planned Responsibility | Dependency / Compatibility Impact |
|-------------------|-----------|------------------------|-----------------------------------|
| `core` | [Yes/No] | [Contracts/domain/value objects/pure rules] | [Impact] |
| `app/shared` | [Yes/No] | [UI/ViewModel/use case/repository/client behavior] | [Impact] |
| `app/androidApp` | [Yes/No] | [Entry point/platform wiring only] | [Impact] |
| `app/iosApp` | [Yes/No] | [Entry point/platform wiring only] | [Impact] |
| `server` | [Yes/No] | [Routes/use cases/repositories/persistence/infrastructure] | [Impact] |
| `webApp` | [No unless explicitly specified] | [No modification by default] | [Compatibility obligation] |
| Other consumers | [Yes/No/N/A] | [Responsibility] | [Impact] |

### Allowed Dependency Flow

```text
app/androidApp -> app/shared -> core
app/iosApp     -> app/shared -> core
server         -> core
```

[Explain how this feature preserves the allowed flow. Identify any proposed exception.]

## Constitution Check

**Gate status before Phase 0**: [PASS / FAIL / EXCEPTION REQUIRED]  
**Gate status after Phase 1 design**: [PASS / FAIL / EXCEPTION REQUIRED]

| # | Constitutional Gate | Status | Evidence / Design Response |
|---|---------------------|--------|----------------------------|
| 1 | Established module ownership is preserved. | [PASS/N/A/FAIL] | [Evidence] |
| 2 | `core` remains platform-neutral and framework-independent. | [PASS/N/A/FAIL] | [Evidence] |
| 3 | Compile-time dependencies follow the allowed direction. | [PASS/N/A/FAIL] | [Evidence] |
| 4 | Shared application behavior remains in `app/shared`. | [PASS/N/A/FAIL] | [Evidence] |
| 5 | Android and iOS app modules remain thin entry points. | [PASS/N/A/FAIL] | [Evidence] |
| 6 | `webApp` is unchanged unless explicitly included. | [PASS/N/A/FAIL] | [Evidence] |
| 7 | Existing consumers are protected from incompatible contract changes. | [PASS/N/A/FAIL] | [Evidence] |
| 8 | Exposed and persistence types remain inside `server`. | [PASS/N/A/FAIL] | [Evidence] |
| 9 | Every schema change has a new Flyway migration. | [PASS/N/A/FAIL] | [Evidence] |
| 10 | Existing applied migrations remain immutable. | [PASS/N/A/FAIL] | [Evidence] |
| 11 | Authentication, authorization, ownership, and privacy are enforced server-side. | [PASS/N/A/FAIL] | [Evidence] |
| 12 | Sensitive values are excluded from logs and responses. | [PASS/N/A/FAIL] | [Evidence] |
| 13 | Dependencies use constructor injection and the correct Koin composition root. | [PASS/N/A/FAIL] | [Evidence] |
| 14 | Architecture layers are proportional to actual behavior. | [PASS/N/A/FAIL] | [Evidence] |
| 15 | Required tests and Gradle verification are planned. | [PASS/N/A/FAIL] | [Evidence] |

A `FAIL` blocks design or implementation. Use the Governance Exception section only for a
narrow, temporary, explicitly approved exception.

## Phase 0: Research and Decisions

<!-- Resolve all material technical unknowns. Prefer existing project patterns. -->

### Decisions Required

- [Decision about existing implementation pattern]
- [Contract or compatibility decision]
- [Persistence/migration decision]
- [Security/authorization decision]
- [Testing/verification decision]

### Research Output

Document decisions in `research.md` using:

- **Decision**
- **Rationale**
- **Alternatives considered**
- **Why the simpler or existing pattern is insufficient**, when introducing something new
- **Constitution impact**

## Phase 1: Design

### Architecture and Layering

**App flow**: [UI -> ViewModel -> UseCase -> Repository -> API/DataSource, with justified omissions]  
**Server flow**: [Route -> UseCase/Service -> Repository -> persistence mapping, with justified omissions]  
**Composition roots**: [App Koin modules / server Koin modules]  
**Platform divergence**: [None or justified `expect`/`actual` / platform wiring]

### Contracts and Compatibility

- Canonical contract files in `core`: [paths or N/A]
- Added fields and defaults: [details or N/A]
- Removed/renamed/retyped fields: [none or breaking-change plan]
- Enum/sealed compatibility: [fallback/versioning approach]
- Generated sources affected: [generation source and task, not generated-file edits]
- `webApp` compatibility: [why unaffected or explicit migration plan]
- Rollout order: [server/app/other consumer sequence]

### Security and Privacy

- Authentication context: [source]
- Authorization and ownership rules: [rules and enforcement layer]
- Security-sensitive validation: [rules]
- Sensitive logging/response risks: [mitigations]
- Safe error model: [contract/behavior]
- Abuse/rate considerations: [approach or N/A]

### Data and Migration

- Data model changes: [summary or N/A]
- New Flyway migration: [planned filename or N/A]
- Existing migrations modified: **No**
- Backfill/default strategy: [details or N/A]
- Clean-database validation: [approach]
- Previous-schema upgrade validation: [approach]
- Rollback or forward-fix strategy: [details]
- Transaction boundaries: [details]

### Testing Strategy

| Area | Required? | Planned Coverage | Planned Location |
|------|-----------|------------------|------------------|
| Pure shared logic | [Yes/No] | [Coverage] | `commonTest` or N/A |
| Serialization / contracts | [Yes/No] | [Coverage] | [Path] |
| App ViewModel / use case | [Yes/No] | [Coverage] | [Path/source set] |
| Android-specific behavior | [Yes/No] | [Coverage] | [Path/source set] |
| iOS-specific behavior | [Yes/No] | [Coverage] | [Path/source set] |
| Server use case / route | [Yes/No] | [Coverage] | [Path] |
| Authorization / security | [Yes/No] | [Coverage] | [Path] |
| Repository / persistence | [Yes/No] | [Coverage] | [Path] |
| Flyway migration | [Yes/No] | [Clean + previous schema] | [Path/setup] |
| Regression test | [Yes/No] | [Failure reproduced] | [Path] |

When a constitution-triggered test is omitted, document the exact reason, residual risk,
alternative verification, and follow-up task.

### Verification Commands

Use actual Gradle module paths discovered from `settings.gradle.kts` and build files.

```bash
# Shared/core tests
[./gradlew :actual-module:test-task]

# Server tests
[./gradlew :server:test]

# Android compilation/tests
[./gradlew :actual-android-module:assembleDebug]
[./gradlew :actual-android-module:test-task]

# iOS/shared compilation/tests where supported
[./gradlew :actual-shared-module:ios-test-or-compile-task]

# Contract generation/verification where applicable
[./gradlew :actual-generation-task]
```

## Project Structure

### Documentation for This Feature

```text
specs/[###-feature-name]/
├── spec.md
├── plan.md
├── research.md
├── data-model.md          # when data is involved
├── quickstart.md
├── contracts/             # when contracts are involved
├── checklists/
└── tasks.md               # generated by /speckit.tasks
```

### Repository Source Structure

<!--
Replace the tree with actual paths discovered in the repository. Remove paths not touched by
the feature. Do not use generic Python, frontend/backend, api/, ios/, or android/ placeholders.
-->

```text
core/
└── src/
    ├── commonMain/
    └── commonTest/

app/
├── shared/
│   └── src/
│       ├── commonMain/
│       ├── commonTest/
│       ├── androidMain/
│       ├── androidTest/
│       ├── iosMain/
│       └── iosTest/
├── androidApp/
└── iosApp/

server/
├── src/main/
├── src/test/
└── src/main/resources/
    └── db/migration/

webApp/                    # no modifications unless explicitly in scope
```

**Structure Decision**: [List the exact existing directories and files affected by this feature.]

## Post-Design Constitution Re-check

[Repeat the gate result after contracts, data model, security design, testing strategy, and
source paths are known. Record any change from the pre-research check.]

## Complexity Tracking

> Fill only when the design adds new libraries, broad abstractions, duplicate representations,
> new cross-cutting infrastructure, or another material complexity increase.

| Added Complexity | Concrete Need | Existing / Simpler Alternative | Why Rejected | Removal or Containment |
|------------------|---------------|--------------------------------|--------------|------------------------|
| [Item] | [Need] | [Alternative] | [Reason] | [How scope stays bounded] |

## Governance Exception

> Fill only when a constitutional rule cannot be met. An unapproved exception does not turn a
> failed gate into a pass.

- **Rule violated**: [Exact constitution rule]
- **Why compliance is currently impractical**: [Concrete reason]
- **Affected modules and files**: [Paths]
- **Security risks**: [Risks]
- **Compatibility risks**: [Risks]
- **Maintenance risks**: [Risks]
- **Approving reviewer**: [Name/role]
- **Expiration date or removal condition**: [Condition]
- **Remediation issue/task**: [Reference]
- **Expected compliant end state**: [State]