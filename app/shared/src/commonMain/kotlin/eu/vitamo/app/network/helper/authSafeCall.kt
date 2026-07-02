package eu.vitamo.app.network.helper

import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeAuthenticatedApiCall(
    authSessionCoordinator: AuthSessionCoordinator,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    val firstResponse = executeApiCall<T>(block)
    if (firstResponse is ApiResult.Success) {
        return firstResponse
    }
    if (firstResponse is ApiResult.Error && firstResponse.error.status == HttpStatusCode.Unauthorized.value) {
        if (authSessionCoordinator.refreshSession()) {
            val retryResponse = executeApiCall<T>(block)
            if (retryResponse is ApiResult.Error && retryResponse.error.status == HttpStatusCode.Unauthorized.value) {
                authSessionCoordinator.signOut()
            }
            return retryResponse
        }
        return firstResponse
    }
    return firstResponse
}

suspend inline fun <reified T> executeApiCall(
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    return try {
        val response = block()

        if (response.status.isSuccess()) {
            ApiResult.Success(response.body<T>())
        } else {
            ApiResult.Error(response.toApiError())
        }
    } catch (e: IOException) {
        println("❌ Network error: ${e.message}")
        ApiResult.Error(
            ApiError(
                code = "NETWORK_ERROR",
                message = "Geen internetverbinding.",
                status = null,
            )
        )
    } catch (_: SerializationException) {
        ApiResult.Error(
            ApiError(
                code = "SERIALIZATION_ERROR",
                message = "De server gaf een onverwacht antwoord terug.",
                status = null,
            )
        )
    } catch (cause: Exception) {
        ApiResult.Error(
            ApiError(
                code = "UNKNOWN_ERROR",
                message = cause.message ?: "Er ging iets mis.",
                status = null,
            )
        )
    }
}

suspend inline fun safeAuthenticatedUnitCall(
    authSessionCoordinator: AuthSessionCoordinator,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<Unit> {
    val firstResponse = executeUnitApiCall(block)
    if (firstResponse is ApiResult.Success) {
        return firstResponse
    }
    if (firstResponse is ApiResult.Error && firstResponse.error.status == HttpStatusCode.Unauthorized.value) {
        if (authSessionCoordinator.refreshSession()) {
            val retryResponse = executeUnitApiCall(block)
            if (retryResponse is ApiResult.Error && retryResponse.error.status == HttpStatusCode.Unauthorized.value) {
                authSessionCoordinator.signOut()
            }
            return retryResponse
        }
        return firstResponse
    }
    return firstResponse
}

suspend fun HttpResponse.toApiError(): ApiError {
    val error = try {
        body<ApiError>()
    } catch (_: Exception) {
        ApiError(
            code = "UNKNOWN_API_ERROR",
            message = "Er ging iets mis.",
            status = status.value,
        )
    }
    return error
}

suspend inline fun executeUnitApiCall(
    crossinline block: suspend () -> HttpResponse,
): ApiResult<Unit> {
    return try {
        val response = block()

        if (response.status.isSuccess()) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error(response.toApiError())
        }
    } catch (_: IOException) {
        ApiResult.Error(
            ApiError(
                code = "NETWORK_ERROR",
                message = "Geen internetverbinding.",
                status = null,
            )
        )
    } catch (_: SerializationException) {
        ApiResult.Error(
            ApiError(
                code = "SERIALIZATION_ERROR",
                message = "De server gaf een onverwacht antwoord terug.",
                status = null,
            )
        )
    } catch (cause: Exception) {
        ApiResult.Error(
            ApiError(
                code = "UNKNOWN_ERROR",
                message = cause.message ?: "Er ging iets mis.",
                status = null,
            )
        )
    }
}
