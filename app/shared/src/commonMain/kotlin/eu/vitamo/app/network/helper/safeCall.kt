package eu.vitamo.app.network.helper

import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeApiCall(
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    return try {
        val response = block()

        if (response.status.isSuccess()) {
            ApiResult.Success(response.body<T>())
        } else {
            val error = try {
                response.body<ApiError>()
            } catch (_: Exception) {
                ApiError(
                    code = "UNKNOWN_API_ERROR",
                    message = "Er ging iets mis.",
                    status = response.status.value,
                )
            }

            ApiResult.Error(error)
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

suspend inline fun safeApiUnitCall(
    crossinline block: suspend () -> HttpResponse,
): ApiResult<Unit> {
    return try {
        val response = block()

        if (response.status.isSuccess()) {
            ApiResult.Success(Unit)
        } else {
            val error = try {
                response.body<ApiError>()
            } catch (_: Exception) {
                ApiError(
                    code = "UNKNOWN_API_ERROR",
                    message = "Er ging iets mis.",
                    status = response.status.value,
                )
            }
            ApiResult.Error(error)
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