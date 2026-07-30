package eu.vitamo.app.exception

import eu.vitamo.app.api.contracts.errorcodes.ApiErrorCode
import eu.vitamo.app.api.result.ErrorCode
import io.ktor.http.HttpStatusCode

sealed class ApiException(
    val apiErrorCode: ApiErrorCode,
    override val message: String,
    val status: HttpStatusCode,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {

    val code: ErrorCode
        get() = apiErrorCode.code

    class BadRequest(
        message: String = "Bad request.",
        cause: Throwable? = null,
    ) : ApiException(
        apiErrorCode = ApiErrorCode.BAD_REQUEST,
        message = message,
        status = HttpStatusCode.BadRequest,
        cause = cause,
    )

    class Unauthorized(
        message: String = "Unauthorized.",
        cause: Throwable? = null,
    ) : ApiException(
        apiErrorCode = ApiErrorCode.UNAUTHORIZED,
        message = message,
        status = HttpStatusCode.Unauthorized,
        cause = cause,
    )

    class Forbidden(
        message: String = "Forbidden.",
        cause: Throwable? = null,
    ) : ApiException(
        apiErrorCode = ApiErrorCode.FORBIDDEN,
        message = message,
        status = HttpStatusCode.Forbidden,
        cause = cause,
    )

    class NotFound(
        message: String = "Resource not found.",
        cause: Throwable? = null,
    ) : ApiException(
        apiErrorCode = ApiErrorCode.NOT_FOUND,
        message = message,
        status = HttpStatusCode.NotFound,
        cause = cause,
    )

    class Conflict(
        message: String = "Conflict.",
        cause: Throwable? = null,
    ) : ApiException(
        apiErrorCode = ApiErrorCode.CONFLICT,
        message = message,
        status = HttpStatusCode.Conflict,
        cause = cause,
    )

    class Internal(
        message: String = "Internal server error.",
        cause: Throwable? = null,
    ) : ApiException(
        apiErrorCode = ApiErrorCode.INTERNAL,
        message = message,
        status = HttpStatusCode.InternalServerError,
        cause = cause,
    )
}