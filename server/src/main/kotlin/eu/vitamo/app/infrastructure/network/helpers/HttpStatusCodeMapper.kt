package eu.vitamo.app.infrastructure.network.helpers

import eu.vitamo.app.api.contracts.errorcodes.ApiErrorCode
import eu.vitamo.app.api.result.ApiFieldError
import eu.vitamo.app.api.result.ErrorCode
import eu.vitamo.app.repository.HasFieldErrors
import eu.vitamo.app.repository.RepositoryError
import io.ktor.http.HttpStatusCode

fun RepositoryError.statusCode(): HttpStatusCode =
    when (this) {
        is RepositoryError.Api ->
            status.toHttpStatusCodeOrInternalError()

        is RepositoryError.RequestLimitReached ->
            HttpStatusCode.TooManyRequests

        is RepositoryError.Unauthorized ->
            HttpStatusCode.Unauthorized

        is RepositoryError.Forbidden ->
            HttpStatusCode.Forbidden

        is RepositoryError.NotFound ->
            HttpStatusCode.NotFound

        is RepositoryError.Validation ->
            HttpStatusCode.BadRequest

        is RepositoryError.Conflict ->
            HttpStatusCode.Conflict

        is RepositoryError.BadRequest ->
            HttpStatusCode.BadRequest

        is RepositoryError.Network ->
            HttpStatusCode.ServiceUnavailable

        is RepositoryError.Serialization ->
            HttpStatusCode.BadGateway

        is RepositoryError.Unknown,
        is RepositoryError.Internal,
            ->
            HttpStatusCode.InternalServerError
    }


fun RepositoryError.errorCode(): ErrorCode =
    when (this) {
        is RepositoryError.Api ->
            code

        is RepositoryError.RequestLimitReached ->
            ApiErrorCode.RATE_LIMIT.code

        is RepositoryError.Unauthorized ->
            ApiErrorCode.UNAUTHORIZED.code

        is RepositoryError.Forbidden ->
            ApiErrorCode.FORBIDDEN.code

        is RepositoryError.NotFound ->
            ApiErrorCode.NOT_FOUND.code

        is RepositoryError.Validation ->
            ApiErrorCode.VALIDATION_ERROR.code

        is RepositoryError.Conflict ->
            ApiErrorCode.CONFLICT.code

        is RepositoryError.BadRequest ->
            ApiErrorCode.BAD_REQUEST.code

        is RepositoryError.Network,
        is RepositoryError.Serialization,
        is RepositoryError.Unknown,
        is RepositoryError.Internal,
            ->
            ApiErrorCode.INTERNAL.code
    }


fun RepositoryError.apiFieldErrors(): List<ApiFieldError> =
    when (this) {
        is RepositoryError.Api ->
            fieldErrors.map { error ->
                ApiFieldError(
                    field = error.field,
                    message = error.message,
                )
            }

        is HasFieldErrors ->
            errors.map { error ->
                ApiFieldError(
                    field = error.field,
                    message = error.message,
                )
            }

        else ->
            emptyList()
    }





private fun Int.toHttpStatusCodeOrInternalError(): HttpStatusCode =
    if (this in 100..599) {
        HttpStatusCode.fromValue(this)
    } else {
        HttpStatusCode.InternalServerError
    }