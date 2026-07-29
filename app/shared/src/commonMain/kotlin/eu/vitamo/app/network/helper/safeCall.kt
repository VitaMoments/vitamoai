package eu.vitamo.app.network.helper

import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiFailure
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.api.result.ErrorCode
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    return executeApiCall(
        block = block,
        readSuccess = { response ->
            response.body<T>()
        },
    )
}

suspend inline fun safeApiUnitCall(
    crossinline block: suspend () -> HttpResponse,
): ApiResult<Unit> {
    return executeApiCall(
        block = block,
        readSuccess = {
            Unit
        },
    )
}

@PublishedApi
internal suspend inline fun <T> executeApiCall(
    crossinline block: suspend () -> HttpResponse,
    crossinline readSuccess: suspend (HttpResponse) -> T,
): ApiResult<T> {
    return try {
        val response = block()

        if (response.status.isSuccess()) {
            ApiResult.Success(
                data = readSuccess(response),
            )
        } else {
            ApiResult.Error(
                error = ApiFailure.Server(
                    apiError = response.toApiError(),
                ),
            )
        }
    } catch (cause: CancellationException) {
        throw cause
    } catch (cause: IOException) {
        ApiResult.Error(
            error = ApiFailure.Network(
                cause = cause,
            ),
        )
    } catch (cause: SerializationException) {
        ApiResult.Error(
            error = ApiFailure.Serialization(
                cause = cause,
            ),
        )
    } catch (cause: Exception) {
        ApiResult.Error(
            error = ApiFailure.Unknown(
                message = cause.message ?: "Er ging iets mis.",
                cause = cause,
            ),
        )
    }
}

@PublishedApi
internal suspend fun HttpResponse.toApiError(): ApiError {
    return try {
        body<ApiError>()
    } catch (cause: CancellationException) {
        throw cause
    } catch (_: Exception) {
        ApiError(
            code = ErrorCode.http(status.value),
            message = "De server kon de aanvraag niet verwerken.",
            status = status.value,
        )
    }
}