package eu.vitamo.app.error

import eu.vitamo.app.api.contracts.common.BaseErrorCode
import io.ktor.http.HttpStatusCode

sealed class ErrorResponse(
    open val code: String,
    override val message: String,
    open val status: HttpStatusCode,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {

    data class Forbidden(
        override val message: String = "Forbidden.",
    ) : ErrorResponse(
        code = BaseErrorCode.FORBIDDEN_CODE,
        message = message,
        status = HttpStatusCode.Forbidden,
    )

    data class BadRequest(
        override val message: String = "Bad request.",
    ) : ErrorResponse(
        code = BaseErrorCode.BAD_REQUEST_CODE,
        message = message,
        status = HttpStatusCode.BadRequest,
    )

    data class NotFound(
        override val message: String = "Not found.",
    ) : ErrorResponse(
        code = BaseErrorCode.NOT_FOUND_CODE,
        message = message,
        status = HttpStatusCode.NotFound,
    )

    data class Internal(
        override val message: String = "Internal server error.",
        override val cause: Throwable
    ) : ErrorResponse(
        code = BaseErrorCode.INTERNAL_CODE,
        message = message,
        status = HttpStatusCode.InternalServerError,
        cause = cause
    )
}