# Project instructions

This is a Kotlin Multiplatform project.

## Tech stack

- Kotlin Multiplatform
- Compose Multiplatform
- Kotlinx Serialization
- Ktor Client
- Koin for dependency injection
- Gradle Kotlin DSL
- Exposed for server Database access
- PostgreSQL for server database
- Ktor for server framework
- JWT for authentication
- Coroutines for asynchronous programming
- JUnit for testing

## Architecture

Use a clean feature-based structure.

Prefer this separation:
- UI / Composables: only presentation logic
- ViewModel: state handling and calling use cases
- UseCase: business rules and orchestration
- Repository: data access abstraction
- DataSource / Api: actual network, database, or platform implementation

## Security

- Never log sensitive data (e.g., tokens, passwords).
- Secret development keys must be stored in a .env file and not committed to version control.
- Use environment variables for production secrets.

## Kotlin style

- Prefer explicit, readable Kotlin.
- Avoid unnecessary abstraction.
- Prefer sealed interfaces/classes for typed results.
- Use coroutines correctly.
- Avoid blocking calls in shared code.
- Prefer immutable state where possible.

## KMP rules

- Put shared logic in commonMain.
- Put Android-specific code in androidMain.
- Put iOS-specific code in iosMain.
- Do not use Android APIs in commonMain.
- Use expect/actual only when platform-specific behavior is needed.

## Testing

- Add tests for business logic in commonTest where possible.
- Prefer small focused tests.
- Do not skip tests without explaining why.

## When changing code

- Explain what changed.
- Keep changes small and focused.
- Do not rewrite unrelated files.
- Do not introduce new libraries without explaining why.