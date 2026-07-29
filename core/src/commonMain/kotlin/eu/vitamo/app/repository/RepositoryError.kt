package eu.vitamo.app.repository

import eu.vitamo.app.api.result.ErrorCode

sealed interface RepositoryError {
    val message: String

    data class Api(
        val code: ErrorCode,
        override val message: String,
        val fieldErrors: List<FieldError> = emptyList(),
        val traceId: String? = null,
        val status: Int,
    ) : RepositoryError

    data class Network(
        override val message: String = "Geen internetverbinding.",
        val cause: Throwable? = null,
    ) : RepositoryError

    data class Serialization(
        override val message: String =
            "De server gaf een onverwacht antwoord terug.",
        val cause: Throwable? = null,
    ) : RepositoryError

    data class Unknown(
        override val message: String = "Er ging iets mis.",
        val cause: Throwable? = null,
    ) : RepositoryError

    data class RequestLimitReached(
        override val message: String = "Too many requests",
    ) : RepositoryError

    data class Unauthorized(
        override val message: String = "Unauthorized",
    ) : RepositoryError

    data class Forbidden(
        override val message: String = "Forbidden",
    ) : RepositoryError

    data class NotFound(
        override val message: String = "Not found",
    ) : RepositoryError

    data class Validation(
        override val errors: List<FieldError>,
        override val message: String = "Invalid data",
    ) : RepositoryError, HasFieldErrors

    data class Conflict(
        override val errors: List<FieldError> = emptyList(),
        override val message: String = "Conflict",
    ) : RepositoryError, HasFieldErrors

    data class BadRequest(
        override val errors: List<FieldError> = emptyList(),
        override val message: String = "Bad request",
    ) : RepositoryError, HasFieldErrors

    data class Internal(
        override val message: String =
            "Something went wrong on our server",
        val cause: Throwable? = null,
    ) : RepositoryError
}

interface HasFieldErrors {
    val errors: List<FieldError>
}

data class FieldError(
    val field: String,
    val message: String,
)