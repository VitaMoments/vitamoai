# Project instructions

This is a Kotlin Multiplatform project that started as a practice project, but it is intended to grow into a real production-ready project.

## Project structure

The project uses this module structure:

* `app`

    * `androidApp`
    * `iosApp`
    * `shared`
* `core`
* `server`

## Module responsibilities

### `core`

`core` contains pure shared Kotlin code that can be used by both the app and the server.

Allowed in `core`:

* Shared API contracts
* `@Serializable` request models
* `@Serializable` response models
* Shared serializers
* Shared result/error models
* Pure Kotlin utilities

API contracts are the serializable request and response models used between the app and the server.

Do not create DTOs by default. In this project, API contracts are serializable request and response models. Use DTOs only when a separate representation is explicitly needed.

DTOs may only be introduced when there is a clear reason, such as:

* External API integration models
* Persistence mapping models
* Temporary migration models
* Separating unsafe internal data from public API responses
* Mapping between a third-party API and the internal app/server contract

Not allowed in `core`:

* Ktor server routes
* Ktor server plugins
* Ktor client setup
* Exposed
* Flyway
* HikariCP
* Database access
* Android Context
* Compose UI
* ViewModels
* Platform-specific APIs
* Server-only logic
* App-only logic
* Server repositories
* App repositories
* Server use cases
* App use cases

### `app/shared`

`app/shared` contains shared KMP app logic.

Allowed in `app/shared`:

* Shared app logic
* Compose Multiplatform UI when appropriate
* UI state
* ViewModels
* App use cases
* App-side repositories
* Ktor Client API calls
* App-side Koin modules
* Platform-independent app logic

Not allowed in `app/shared`:

* Ktor server plugins
* Server routes
* Exposed tables
* Flyway migrations
* HikariCP
* PostgreSQL-specific access
* Server-only authentication logic
* Server repositories
* Server use cases
* Database infrastructure

### `app/androidApp`

`app/androidApp` contains Android-specific app startup and Android platform wiring.

Allowed in `app/androidApp`:

* Android MainActivity
* Android Application class
* Android permissions
* Android-specific platform wiring
* Android-specific Koin startup

Not allowed in `app/androidApp`:

* Shared business logic that belongs in `app/shared`
* Server logic
* Database logic
* API contracts that belong in `core`

### `app/iosApp`

`app/iosApp` contains iOS-specific app startup and iOS platform wiring.

Allowed in `app/iosApp`:

* iOS application startup
* SwiftUI/UIKit entrypoints
* iOS-specific platform wiring
* iOS-specific Koin startup

Not allowed in `app/iosApp`:

* Shared business logic that belongs in `app/shared`
* Server logic
* Database logic
* API contracts that belong in `core`

### `server`

`server` contains server-side application code.

Allowed in `server`:

* Ktor server setup
* Routes
* Server-side Koin modules
* PostgreSQL database setup
* Flyway migrations
* Exposed tables/entities
* Server repositories
* Server use cases
* JWT authentication
* HTTP/security configuration
* Ktor server serialization setup
* Server-side validation
* Server-side authorization

Not allowed in `server`:

* Compose UI
* Android-specific APIs
* iOS-specific APIs
* App ViewModels
* App navigation
* Client-side UI state
* Client-side repositories

## Tech stack

* Kotlin Multiplatform
* Compose Multiplatform
* Kotlinx Serialization
* Ktor Client
* Ktor Server
* Koin for dependency injection
* Gradle Kotlin DSL
* Exposed for server database access
* PostgreSQL for server database
* Flyway for server database migrations
* HikariCP for server connection pooling
* JWT for authentication
* Coroutines for asynchronous programming
* JUnit / kotlin-test for testing

## Architecture

Use a clean feature-based structure.

Prefer this separation on the app side:

* UI / Composables: presentation only
* ViewModel: state handling and calling use cases
* UseCase: business rules and orchestration
* Repository: app-side data access abstraction
* Api / DataSource: actual network, database, or platform implementation

Prefer this separation on the server side:

* Route: HTTP request/response handling only
* UseCase: business rules and orchestration
* Repository: data access abstraction
* Exposed table/entity: database mapping
* API contract: shared `@Serializable` request/response model in `core` when used by app and server
* Server-only request/response model: only in `server` when it is not shared with the app

Avoid separate DTOs unless a distinct mapping layer is intentionally needed.

## SIMPLE method

All agents must follow the SIMPLE method.

### S — Small scope

Build only what is requested.

Do not add extra features, extra abstractions, extra libraries, or unrelated refactors.

### I — Inspect first

Before changing code, inspect the relevant files and understand the current structure.

Do not guess module names, package names, existing patterns, Gradle setup, Koin setup, or source set structure.

### M — Minimal change

Make the smallest useful change that solves the task.

Prefer updating existing files over creating duplicate systems.

Do not rewrite unrelated files.

### P — Preserve boundaries

Respect module boundaries:

* `core` is pure shared Kotlin
* `app/shared` is KMP app logic
* `app/androidApp` is Android app startup/platform wiring
* `app/iosApp` is iOS app startup/platform wiring
* `server` is server-side logic

Do not move code across modules unless explicitly asked.

### L — Leave notes

After changes, explain:

* What changed
* Which files were changed
* Why the change was needed
* Which Gradle task should be run
* Any assumptions made

### E — Explicit tests

Add or update tests only when useful and within scope.

Prefer small focused tests.

If tests are not added, explain why.

## Security

* Never log sensitive data.
* Never log tokens.
* Never log passwords.
* Never expose stacktraces or internal exception details to clients.
* Development secrets must be stored in `.env` or environment variables.
* Production secrets must use environment variables.
* Do not commit real credentials.
* Do not use wildcard CORS origins with credentials enabled.
* Use explicit allowed origins for CORS.
* Keep authentication and authorization server-side.
* Do not expose database entities directly as API responses.
* Do not return password hashes, refresh tokens, verification codes, or internal auth state in responses.
* Use safe error responses for clients and detailed logs only on the server side.

## Kotlin style

* Prefer explicit, readable Kotlin.
* Avoid unnecessary abstraction.
* Prefer sealed interfaces/classes for typed results.
* Use coroutines correctly.
* Avoid blocking calls in shared code.
* Prefer immutable state where possible.
* Prefer small files with clear responsibility.
* Prefer clear names over clever names.
* Avoid premature generalization.
* Avoid large “god” classes.
* Keep functions small and focused.
* Prefer constructor injection.

## KMP rules

* Put shared logic in `commonMain`.
* Put Android-specific code in `androidMain`.
* Put iOS-specific code in `iosMain`.
* Do not use Android APIs in `commonMain`.
* Do not use iOS APIs in `commonMain`.
* Use `expect/actual` only when platform-specific behavior is actually needed.
* Keep `core` platform-neutral in code, even if it has Android/iOS/JVM targets.
* Do not add platform dependencies to `core` unless explicitly needed.
* Do not add server dependencies to `app/shared`.
* Do not add app dependencies to `server`.

## API contract rules

* Shared API contracts belong in `core`.
* API contracts are `@Serializable` request and response models used between the app and the server.
* Prefer names such as `LoginRequest`, `LoginResponse`, `UserResponse`, `ApiErrorResponse`.
* Do not create `LoginDto`, `UserDto`, or `SomethingDto` unless a separate mapping layer is explicitly needed.
* Do not duplicate contracts between `server` and `app/shared`.
* Do not leak server database models into API contracts.
* Do not expose sensitive fields in responses.
* Use request models for incoming API payloads.
* Use response models for outgoing API payloads.
* Use domain/server models internally when needed, separate from API contracts.

## Serialization rules

* Shared API contracts and shared serializers belong in `core`.
* Ktor server `ContentNegotiation` belongs in `server`.
* Ktor client serialization setup belongs in `app/shared`.
* Use `classDiscriminator = "type"` for sealed class serialization.
* Use `ignoreUnknownKeys = true` for API compatibility.
* Use string representation for `Uuid`.
* Use ISO-8601 string representation for `Instant`.
* Prefer `Instant` for exact timestamps such as `createdAt`, `updatedAt`, `expiresAt`, `deletedAt`, `lastLoginAt`.
* Prefer Kotlin Clock.System for getting the current time instead of `Instant.now()` directly in business logic.
* Use `LocalDate` for calendar dates.
* Use `LocalTime` for time-of-day.
* Use `LocalDateTime` only when a local date and time without timezone is truly intended.
* Do not serialize `LocalDateTime` as a long unless the meaning of the long is explicitly documented.
* Avoid registering serializers that are not functionally used.

## Database rules

* Database infrastructure belongs only in `server`.
* PostgreSQL, Exposed, Flyway and HikariCP must not be added to `core` or `app/shared`.
* Flyway migrations belong in `server/src/main/resources/db/migration`.
* Exposed tables/entities belong in `server`.
* Repository interfaces may be in `server` unless they are truly shared contracts.
* Do not create database tables without a Flyway migration.
* Do not hardcode production database credentials.
* Do not log database credentials.
* Use environment variables for database configuration.
* Run Flyway migrations before repository/database usage.
* Do not create a new datasource per request.
* Prefer HikariCP as a singleton datasource.
* Keep Exposed transaction logic inside repositories or database helpers.

## Koin rules

* App Koin and server Koin are separate.
* Do not create one global DI container for app and server.
* App DI belongs in `app/shared` and platform app modules.
* Server DI belongs in `server`.
* `core` should normally not contain Koin modules.
* Register dependencies in Koin modules, but do not start expensive services while loading the module unless explicitly intended.
* Prefer explicit startup functions for database initialization, migrations and server bootstrapping.
* Prefer constructor injection.
* Do not hide complex initialization inside Koin declarations if it makes tests harder.

## HTTP/server rules

* Ktor server configuration belongs only in `server`.
* Install Ktor plugins only once.
* Keep HTTP configuration separate from routing, database and serialization when possible.
* CORS must use explicit hosts when credentials are enabled.
* Do not use `allowHost("*")` with credentials.
* Do not expose internal errors to clients.
* Map invalid JSON to HTTP 400 with a safe message.
* Do not expose stacktraces in API responses.
* Keep routes thin.
* Put business logic in use cases.
* Put authorization checks on the server side.

## Testing

* Add tests for business logic where useful.
* Use `commonTest` for shared KMP logic.
* Use server tests for server logic.
* Prefer small focused tests.
* Do not add heavy test dependencies unless explicitly needed.
* Do not skip tests without explaining why.
* Do not require a real database for simple unit tests.
* If database tests are needed later, prefer explicit integration tests.

## When changing code

* First inspect relevant files.
* Explain the plan briefly.
* Make the smallest useful change.
* Keep changes focused.
* Do not rewrite unrelated files.
* Do not introduce new libraries without explaining why.
* Respect existing package names and module structure.
* Summarize changed files after the work.
* Suggest the relevant Gradle task to run.
* Mention assumptions clearly.
