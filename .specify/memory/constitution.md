<!--
Sync Impact Report
- Version change: 1.0.1 -> 1.1.0
- Modified principles:
  - I. Module Boundary Integrity
    - Clarified responsibilities of core, app/shared, androidApp, iosApp, server, and webApp.
    - Added explicit allowed dependency directions.
    - Added a temporary webApp implementation freeze while retaining compatibility obligations.
  - II. Shared Contract Canonicality
    - Expanded core ownership to include platform-neutral domain models, value objects,
      validation, business rules, and boundary interfaces.
    - Clarified the difference between canonical API contracts and separate boundary models.
    - Added backward-compatible API evolution requirements.
  - III. SIMPLE Delivery Discipline
    - Added mandatory reporting of executed and skipped verification commands.
    - Clarified when architectural layers may be omitted for trivial behavior.
  - IV. Security-First Server Enforcement
    - Added local environment file requirements.
    - Clarified server authority, safe error handling, and sensitive-data logging rules.
  - V. Testable Incremental Quality
    - Strengthened test omission requirements.
    - Added regression-test expectations for bug fixes.
- Added sections:
  - Explicit module dependency rules
  - API compatibility rules
  - Flyway migration immutability
  - Koin constructor-injection requirements
  - Governance exception requirements
  - webApp implementation freeze
- Removed sections:
  - None
- Templates requiring updates:
  - ⚠ .specify/templates/plan-template.md
  - ⚠ .specify/templates/spec-template.md
  - ⚠ .specify/templates/tasks-template.md
  - ⚠ .specify/templates/commands/*.md (directory may not exist)
- Follow-up TODOs:
  - Synchronize constitution checks in plan, specification, and task templates.
-->

# VitamoAI Constitution

## Core Principles

### I. Module Boundary Integrity

All code placement and dependencies MUST respect the established module ownership and
dependency direction.

`core` MUST contain only platform-neutral Kotlin code. Appropriate contents include:

* canonical serializable API request and response contracts;
* shared domain models and value objects;
* pure validation and platform-neutral business rules;
* shared error and result models;
* boundary interfaces shared across modules;
* serializers and serialization utilities;
* pure utilities without runtime or framework dependencies.

`core` MUST NOT depend on application modules, server modules, Android APIs, Apple APIs,
database frameworks, server frameworks, platform-specific storage, or platform-specific
runtime implementations.

`app/shared` MUST contain shared application behavior, including:

* shared user-interface code where applicable;
* ViewModels and presentation state;
* client-side use cases;
* repository abstractions and client repository implementations;
* Ktor client communication;
* shared local-data behavior;
* platform-neutral navigation and application orchestration;
* platform implementations in `androidMain`, `iosMain`, or equivalent source sets.

`app/androidApp` and `app/iosApp` MUST remain thin platform entry-point modules. They MAY
contain:

* application startup and lifecycle integration;
* dependency-injection bootstrap;
* platform configuration;
* manifests, entitlements, and platform metadata;
* platform-specific navigation entry points;
* notification, deep-link, and operating-system delegates;
* platform-specific wiring that cannot appropriately live in `app/shared` source sets.

Shared application behavior MUST NOT be duplicated in `app/androidApp` or `app/iosApp`.

`server` MUST contain all server-runtime responsibilities, including:

* Ktor server configuration and routes;
* authentication and authorization;
* server-side use cases and services;
* persistence implementations;
* Exposed tables, entities, queries, and mappings;
* Flyway migrations;
* database connection management;
* server-only infrastructure;
* integrations requiring server-side credentials.

The following compile-time dependency directions are allowed:

```text
app/androidApp -> app/shared -> core
app/iosApp     -> app/shared -> core
server         -> core
```

The following dependency directions are forbidden:

```text
core       -> app/shared
core       -> app/androidApp
core       -> app/iosApp
core       -> server
app/shared -> server
server     -> app/shared
server     -> app/androidApp
server     -> app/iosApp
```

Application modules MUST communicate with the server through explicit network contracts.
They MUST NOT compile against server implementation code.

The existing `webApp` is outside the active implementation scope. Contributors MUST NOT
modify `webApp`, its dependencies, generated sources, configuration, tests, or deployment
workflows unless a feature specification explicitly includes web application changes.

API and shared-contract changes MUST still consider compatibility with `webApp`, even when
implementation changes to `webApp` are out of scope. A feature MUST NOT knowingly break an
existing web client without documenting the compatibility impact and an approved migration
strategy.

Cross-module leakage is forbidden unless approved as a documented, narrow, and time-bounded
governance exception.

Rationale: strict and explicit boundaries preserve maintainability, testability, security,
and Kotlin Multiplatform correctness.

### II. Shared Contract and Domain Canonicality

API request and response models shared by the application and server MUST be defined once as
canonical `@Serializable` models in `core`.

Duplicate transport models representing the same API contract MUST NOT be introduced in
`app/shared`, `server`, `app/androidApp`, or `app/iosApp`.

Separate boundary models MAY be introduced when there is a material distinction, including:

* persistence models;
* Exposed entities or database rows;
* third-party API models;
* legacy compatibility models;
* security-filtered representations;
* internal domain representations with different invariants;
* migration or version-compatibility models.

When separate models exist, mapping MUST occur explicitly at the relevant architectural
boundary.

Server database entities, Exposed types, internal configuration, password data, security
claims, audit internals, and other sensitive implementation details MUST NEVER be exposed
directly as API responses.

Canonical contracts MUST use consistent serialization conventions:

* Kotlin Serialization is the canonical serialization mechanism;
* sealed hierarchies MUST use `"type"` as their discriminator unless a documented external
  contract requires another value;
* UUID values MUST be serialized as strings;
* temporal values MUST use ISO-8601;
* `Instant`, `LocalDate`, and `LocalTime` MUST be selected according to their actual
  semantics;
* clients and servers MUST tolerate unknown object keys where compatibility requires it;
* default values MUST be deliberate and safe for older clients.

Unknown-key tolerance does not make the following changes backward compatible:

* removing a field;
* renaming a field;
* changing the meaning of a field;
* changing a field type;
* making an optional field required;
* removing or renaming a serialized subtype;
* introducing enum or sealed values that existing clients cannot safely deserialize.

Backward-compatible API evolution SHOULD prefer:

* adding optional fields with safe defaults;
* preserving existing field names and meanings;
* additive response changes;
* staged deprecation before removal;
* versioned endpoints or migration adapters when compatibility cannot otherwise be
  preserved.

Removing, renaming, retyping, or semantically changing serialized fields is a breaking
change. Breaking changes MUST include:

* affected consumers;
* compatibility impact;
* migration behavior;
* rollout order;
* fallback or rollback considerations.

Rationale: canonical contracts and explicit mappings prevent drift, accidental data
exposure, and client-server integration defects.

### III. SIMPLE Delivery Discipline

All implementation work MUST follow SIMPLE:

* **Small scope**: implement the smallest coherent change that satisfies the requirement;
* **Inspect first**: read relevant source files, tests, configuration, and repository
  instructions before editing;
* **Minimal change**: avoid unrelated refactors, formatting churn, and speculative
  abstractions;
* **Preserve boundaries**: respect module ownership and established architecture;
* **Leave notes**: report assumptions, relevant decisions, risks, and follow-up work;
* **Explicit tests**: identify and perform the appropriate verification.

Contributors MUST preserve existing user changes and MUST NOT modify unrelated files.

New libraries, new infrastructure, or broad architectural abstractions MUST NOT be
introduced unless the existing solution is insufficient and the change is explicitly
justified.

Architecture layers MUST be introduced because they enforce a meaningful boundary or contain
non-trivial behavior, not solely to satisfy a naming pattern.

Non-trivial application and business flows MUST follow the established layering:

```text
App:
UI -> ViewModel -> UseCase -> Repository -> API/DataSource

Server:
Route -> UseCase/Service -> Repository -> Persistence mapping
```

Trivial pass-through behavior, platform wiring, static configuration, health endpoints, and
simple diagnostics MAY omit unnecessary layers when:

* no business rule is present;
* no reusable abstraction is being created;
* the simpler implementation remains testable;
* module and security boundaries remain intact;
* the decision does not cause framework or persistence concerns to leak into core logic.

Routes and controllers MUST remain thin. They SHOULD be responsible for:

* receiving and parsing requests;
* authentication context retrieval;
* request-to-command mapping;
* invoking a use case or service;
* mapping results to responses;
* selecting the correct HTTP status.

Business decisions, authorization policies, workflow transitions, and reusable validation
MUST NOT be embedded directly in routes.

Each delivery MUST include:

* files changed;
* reason for each change;
* assumptions made;
* relevant architecture decisions;
* verification commands executed;
* verification results;
* commands not executed;
* reasons commands were not executed;
* known residual risks.

Applicable Gradle compilation and test tasks MUST be executed before delivery when the
environment permits. IDE or editor diagnostics alone MUST NOT be treated as proof that the
project compiles.

Rationale: disciplined and evidence-based iteration reduces regressions, avoids
overengineering, and keeps delivery predictable.

### IV. Security-First Server Enforcement

The server is authoritative for:

* authentication;
* authorization;
* resource ownership;
* access control;
* privacy;
* account state;
* security-sensitive validation;
* data integrity;
* rate and abuse controls where applicable.

Client-side checks MAY improve user experience but MUST NOT be trusted for security
decisions. Hiding a client action, disabling a button, or validating a request in the app
does not replace server-side enforcement.

The server MUST derive authenticated identity from trusted authentication context. It MUST
NOT trust user identifiers, ownership claims, roles, or permission flags supplied by a
client without server-side verification.

Security-sensitive validation MUST occur before persistent state is changed.

Secrets MUST be supplied through environment variables or an approved secret-management
mechanism. Local `.env` files MAY be used during development but MUST be excluded from
version control.

An `.env.example` file MAY contain:

* environment-variable names;
* documentation;
* safe placeholder values.

It MUST NOT contain real credentials, tokens, private keys, production hosts with embedded
credentials, or other secrets.

The following values MUST NOT appear in logs, API responses, analytics, or unhandled error
messages:

* passwords;
* password hashes;
* access tokens;
* refresh tokens;
* session cookies;
* email-verification codes;
* password-reset codes;
* API keys;
* database credentials;
* private cryptographic keys;
* complete authentication headers;
* internal stack traces.

Errors returned to clients MUST use safe, intentional API error models. Internal exceptions
MUST be logged only at an appropriate server boundary and MUST be sanitized before being
returned.

CORS configurations using credentials MUST use explicit allowed origins. Wildcard origins or
wildcard hosts MUST NOT be combined with credentialed requests.

Security-related failures MUST default to denial. Missing, malformed, expired, revoked, or
unverifiable authentication state MUST NOT grant access.

Rationale: centralized enforcement and safe handling prevent privilege escalation, data
leakage, token theft, and abuse.

### V. Testable Incremental Quality

Tests MUST be added or updated for:

* business-critical logic;
* non-trivial business rules;
* contract behavior;
* serialization behavior;
* authorization and access-control behavior;
* security-sensitive validation;
* persistence mappings;
* database migrations;
* bug fixes with a reproducible failure mode.

Shared platform-neutral tests belong in `commonTest` or the appropriate shared test source
set.

Platform-specific behavior belongs in the matching platform test source set.

Server routes, use cases, authorization, repositories, and integrations belong in the
appropriate server test suites.

Contract changes MUST include serialization or compatibility verification where the change
could affect application, server, or web consumers.

Bug fixes SHOULD include a regression test that fails before the fix and passes after the
fix.

A required test MAY be omitted only when:

* it is technically infeasible in the current environment;
* it would be disproportionate to a genuinely trivial change;
* equivalent behavior is already covered by an existing test;
* the relevant platform or external dependency is unavailable.

When a test is omitted, the delivery MUST document:

* the exact reason;
* the uncovered behavior;
* the residual risk;
* alternative or manual verification performed;
* any follow-up task required.

The following are not valid reasons for omitting tests:

* tests were not explicitly requested;
* the change appears small without inspection;
* the contributor assumes compilation is sufficient;
* adding a test would take additional effort.

Skipped, disabled, flaky, or quarantined tests MUST include an explicit reason and a
remediation path. They MUST NOT be silently ignored.

A change is not complete until:

* affected modules compile;
* relevant tests pass;
* contract compatibility has been considered;
* required migrations are present;
* security-sensitive behavior is enforced server-side;
* verification evidence has been reported.

Rationale: incremental quality gates support safe delivery, reliable evolution, and
confidence across all supported targets.

## Technical and Architecture Guardrails

### Kotlin Multiplatform

Kotlin Multiplatform source-set rules are mandatory:

* platform-neutral shared code belongs in `commonMain`;
* Android-specific code belongs in `androidMain`, `app/androidApp`, or another explicitly
  Android-owned source set;
* iOS-specific code belongs in `iosMain`, `app/iosApp`, or another explicitly Apple-owned
  source set;
* shared test logic belongs in `commonTest`;
* platform-specific tests belong in their applicable platform source sets.

`expect` and `actual` MUST be used only for genuine platform divergence, such as:

* secure storage;
* device capabilities;
* operating-system lifecycle integration;
* Bluetooth or radio APIs;
* notifications;
* haptics;
* camera access;
* platform filesystem access.

`expect` and `actual` MUST NOT be used to hide inappropriate framework dependencies,
duplicate business logic, or avoid defining an explicit interface.

### Ktor

Ktor server configuration and plugins MUST remain in `server`.

Ktor client configuration MUST remain in `app/shared` or its platform source sets.

Pure domain models and business rules MUST NOT depend directly on:

* `HttpClient`;
* Ktor server calls;
* HTTP status codes;
* route parameters;
* request or response framework types.

Network communication SHOULD be exposed to application logic through repository or gateway
interfaces.

External HTTP calls MUST define appropriate:

* timeouts;
* failure mapping;
* cancellation behavior;
* retry behavior when safe;
* response validation.

Retries MUST NOT be applied blindly to non-idempotent operations.

### Koin

Application and server Koin containers MUST remain separate.

Constructor injection MUST be the default dependency mechanism.

Direct Koin lookups, `KoinComponent`, `inject()`, and global service-location MUST be limited
to:

* composition roots;
* application startup;
* platform bootstrap code;
* framework-controlled entry points where constructor injection is impractical.

Domain models, use cases, services, and repositories MUST NOT retrieve their own dependencies
from Koin.

Koin declarations MUST NOT hide expensive startup work, database migrations, network calls,
or other side effects. Expensive initialization MUST be explicit and occur at an appropriate
runtime boundary.

### Persistence and Exposed

All database access MUST remain in `server`.

Exposed tables, entities, result rows, query expressions, transactions, and database-specific
types MUST NOT cross the persistence boundary.

Repositories MUST map persistence representations to domain or API representations before
returning them to higher layers.

Routes MUST NOT contain non-trivial Exposed queries.

Database transactions MUST have an explicit and appropriate boundary.

Operations that must succeed or fail as one logical unit MUST execute within the same
transaction.

Database exceptions MUST NOT be returned directly to clients.

Queries MUST apply explicit pagination, limits, ordering, and ownership filters where
applicable.

### Flyway

Every database schema change MUST include a versioned Flyway migration before application
code relies on that schema.

Applied Flyway migrations MUST be treated as immutable. A migration that may have run in a
shared, test, staging, or production environment MUST NOT be edited, renamed, reordered, or
deleted.

Corrections to an applied migration MUST be delivered through a new forward migration.

Migration names MUST describe their intended change clearly.

Destructive migrations, constraint tightening, column removal, type conversion, or data
backfills MUST include:

* affected data;
* rollout order;
* compatibility window;
* rollback or forward-fix strategy;
* mitigation for partial failure;
* operational considerations.

Where practical, risky changes SHOULD be split into compatible stages, for example:

```text
1. Add nullable column.
2. Deploy compatible application code.
3. Backfill existing data.
4. Add constraints.
5. Remove deprecated usage in a later release.
```

Schema migrations MUST be validated against:

* a clean database;
* the previously supported schema state.

Runtime schema auto-generation MUST NOT replace Flyway migrations.

### Serialization and Compatibility

The application, server, and any active web consumers MUST use compatible serialization
configuration.

Adding a value to an enum or sealed hierarchy MUST be treated as a compatibility-sensitive
change. `ignoreUnknownKeys` does not make unknown enum values or unknown sealed subtypes safe
for older clients.

Where an extensible value set is expected, designs SHOULD prefer representations that allow
an explicit unknown or fallback value.

Generated contract sources MUST NOT be modified manually when they are generated from a
canonical source. The generating source and generation task MUST be updated instead.

## Workflow and Review Standards

Before implementation, contributors MUST:

* read repository-specific instructions;
* inspect the relevant project structure;
* inspect applicable specifications and plans;
* inspect current implementations and tests;
* inspect the current Git state when available;
* preserve existing user changes;
* identify affected modules and consumers.

Feature specifications MUST describe user-visible behavior and acceptance criteria without
embedding unnecessary implementation decisions.

Implementation plans MUST describe:

* affected modules;
* dependency direction;
* contract impact;
* persistence impact;
* migration requirements;
* security and authorization behavior;
* tests and verification;
* compatibility with active clients;
* whether `webApp` remains unaffected.

Task lists MUST include concrete tasks for:

* boundary-safe implementation;
* tests;
* Flyway migrations when required;
* contract updates;
* documentation or generated-source updates;
* build and verification.

Every implementation plan and pull request MUST contain a constitution check covering:

1. Are the established module boundaries preserved?
2. Does `core` remain platform-neutral and framework-independent?
3. Are compile-time dependency directions valid?
4. Is shared app behavior located in `app/shared`?
5. Are Android and iOS entry-point modules kept thin?
6. Is `webApp` left unchanged unless explicitly included?
7. Are active consumers protected from incompatible contract changes?
8. Are persistence and Exposed types confined to `server`?
9. Is every schema change represented by a new Flyway migration?
10. Are applied migrations left immutable?
11. Are authentication and authorization enforced server-side?
12. Are sensitive values excluded from logs and responses?
13. Are dependencies supplied through appropriate Koin composition roots?
14. Are architecture layers proportional to the behavior?
15. Are relevant tests and Gradle verification tasks included?

Reviews MUST reject non-compliant changes unless an approved governance exception exists.

## Definition of Done

A change is complete only when:

* the requested behavior is implemented;
* acceptance criteria are satisfied;
* module ownership is respected;
* forbidden module dependencies have not been introduced;
* shared behavior is not duplicated across platforms;
* `webApp` remains untouched unless explicitly in scope;
* active client compatibility has been evaluated;
* server-side security rules are enforced;
* persistence changes include valid Flyway migrations;
* applied migrations have not been modified;
* required mappings exist at architectural boundaries;
* relevant tests have been added or updated;
* affected targets compile;
* relevant tests pass;
* skipped verification is explicitly documented;
* temporary code and debugging output have been removed;
* documentation is updated when behavior, configuration, contracts, or architecture change;
* the delivery summary contains verification evidence and known residual risks.

## Governance

This constitution is the authoritative engineering policy for VitamoAI. It supersedes
conflicting local conventions, implementation preferences, generated plans, task lists, and
ad-hoc practices.

Specifications, plans, tasks, templates, agent instructions, and implementation changes MUST
remain consistent with this constitution.

### Amendment Process

Every amendment MUST include:

* a clear rationale;
* impacted principles and sections;
* compatibility impact;
* migration guidance;
* affected templates and runtime guidance;
* the proposed semantic version change;
* an updated Sync Impact Report.

Amendments MUST be approved through repository review before merge.

Architectural changes that conflict with this constitution MUST update the constitution
before or together with the conflicting implementation. An implementation MUST NOT silently
establish new constitutional policy.

### Governance Exceptions

A governance exception MUST document:

* the exact rule being violated;
* why the compliant solution is currently impractical;
* affected modules and files;
* security, compatibility, and maintenance risks;
* the approving reviewer;
* an expiration date or concrete removal condition;
* a remediation issue or follow-up task;
* the expected compliant end state.

Exceptions MUST be:

* narrow;
* temporary;
* explicitly approved;
* visible in the implementation plan or pull request;
* removed when their expiration condition is reached.

An exception MUST NOT silently establish architectural precedent.

Security controls protecting credentials, authentication, authorization, or sensitive data
MUST NOT be bypassed through a routine governance exception.

### Versioning Policy

Semantic versioning is mandatory for this constitution.

* **MAJOR**: backward-incompatible governance changes, principle removals, or fundamental
  redefinitions of existing architecture.
* **MINOR**: new principles, new mandatory sections, or material expansion of existing
  obligations.
* **PATCH**: clarifications, wording corrections, formatting improvements, and
  non-semantic edits.

### Compliance Reviews

Every implementation plan MUST pass constitution checks:

* before technical design begins;
* after technical design is complete;
* before implementation is considered complete.

Every task list MUST reflect the boundary, compatibility, security, persistence, migration,
and test obligations of this constitution.

Repository templates and runtime guidance MUST be synchronized whenever constitutional
policy changes.

Periodic architecture reviews SHOULD check for:

* module dependency drift;
* duplicated shared behavior;
* direct Koin service-location in business code;
* persistence leakage;
* edited historical migrations;
* undocumented governance exceptions;
* accidental `webApp` changes;
* incompatible API evolution;
* missing tests for critical behavior.

**Version**: 1.1.0
**Ratified**: 2026-07-01
**Last Amended**: 2026-07-15
