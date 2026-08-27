package eu.vitamo.app.repository

inline fun <T, R> RepositoryResult<T>.flatMap(
    transform: (T) -> RepositoryResult<R>,
): RepositoryResult<R> =
    when (this) {
        is RepositoryResult.Success ->
            transform(data)

        is RepositoryResult.Error ->
            this
    }

suspend inline fun <T, R> RepositoryResult<T>.flatMapSuspend(
    crossinline transform: suspend (T) -> RepositoryResult<R>,
): RepositoryResult<R> =
    when (this) {
        is RepositoryResult.Success ->
            transform(data)

        is RepositoryResult.Error ->
            this
    }

suspend inline fun <T, R> RepositoryResult<T>.mapSuspend(
    crossinline transform: suspend (T) -> R,
): RepositoryResult<R> =
    when (this) {
        is RepositoryResult.Success ->
            RepositoryResult.Success(
                transform(data),
            )

        is RepositoryResult.Error ->
            this
    }