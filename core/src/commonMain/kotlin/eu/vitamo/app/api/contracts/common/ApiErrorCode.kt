package eu.vitamo.app.api.contracts.common

import eu.vitamo.app.api.result.ErrorCode
import eu.vitamo.app.api.result.ErrorCodeDefinition

enum class ApiErrorCode(
    override val code: ErrorCode,
) : ErrorCodeDefinition {
    BAD_REQUEST(ErrorCode("BAD_REQUEST")),
    UNAUTHORIZED(ErrorCode("UNAUTHORIZED")),
    FORBIDDEN(ErrorCode("FORBIDDEN")),
    NOT_FOUND(ErrorCode("NOT_FOUND")),
    CONFLICT(ErrorCode("CONFLICT")),
    VALIDATION_ERROR(ErrorCode("VALIDATION_ERROR")),
    RATE_LIMIT(ErrorCode("RATE_LIMIT")),
    INTERNAL(ErrorCode("INTERNAL")),

    // Fouten die lokaal in de API-client kunnen ontstaan.
    NETWORK(ErrorCode("NETWORK")),
    SERIALIZATION(ErrorCode("SERIALIZATION")),
    CLIENT(ErrorCode("CLIENT")),

    UNKNOWN(ErrorCode("UNKNOWN"));

    companion object {
        private val byCode: Map<ErrorCode, ApiErrorCode> =
            entries.associateBy { it.code }

        fun from(code: ErrorCode): ApiErrorCode? {
            return byCode[code]
        }
    }
}