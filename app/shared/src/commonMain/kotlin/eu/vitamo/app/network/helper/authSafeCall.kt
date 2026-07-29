package eu.vitamo.app.network.helper

import eu.vitamo.app.api.contracts.auth.AuthErrorCode
import eu.vitamo.app.api.result.ApiFailure
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> safeAuthenticatedApiCall(
    authSessionCoordinator: AuthSessionCoordinator,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    return executeAuthenticatedCall(
        authSessionCoordinator = authSessionCoordinator,
        request = {
            safeApiCall<T>(block)
        },
    )
}

suspend inline fun safeAuthenticatedUnitCall(
    authSessionCoordinator: AuthSessionCoordinator,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<Unit> {
    return executeAuthenticatedCall(
        authSessionCoordinator = authSessionCoordinator,
        request = {
            safeApiUnitCall(block)
        },
    )
}

@PublishedApi
internal suspend inline fun <T> executeAuthenticatedCall(
    authSessionCoordinator: AuthSessionCoordinator,
    crossinline request: suspend () -> ApiResult<T>,
): ApiResult<T> {
    val firstResult = request()

    if (!firstResult.requiresAccessTokenRefresh()) {
        return firstResult
    }

    val refreshed = authSessionCoordinator.refreshSession()

    if (!refreshed) {
        return firstResult
    }

    val retryResult = request()

    if (retryResult.requiresAccessTokenRefresh()) {
        authSessionCoordinator.signOut()
    }

    return retryResult
}

@PublishedApi
internal fun ApiResult<*>.requiresAccessTokenRefresh(): Boolean {
    val failure = (this as? ApiResult.Error)?.error
    val serverFailure = failure as? ApiFailure.Server
        ?: return false

    return AuthErrorCode.from(serverFailure.apiError.code) ==
            AuthErrorCode.INVALID_ACCESS_TOKEN
}