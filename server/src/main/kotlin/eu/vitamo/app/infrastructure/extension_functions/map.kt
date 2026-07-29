package eu.vitamo.app.infrastructure.extension_functions

import eu.vitamo.app.repository.RepositoryResult

suspend inline fun <T, R> RepositoryResult<T>.mapSuspend(
    crossinline transform: suspend (T) -> R,
): RepositoryResult<R> =
    when (this) {
        is RepositoryResult.Success ->
            RepositoryResult.Success(
                data = transform(data),
            )

        is RepositoryResult.Error ->
            this
    }