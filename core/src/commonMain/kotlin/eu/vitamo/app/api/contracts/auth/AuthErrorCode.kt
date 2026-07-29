package eu.vitamo.app.api.contracts.auth

import eu.vitamo.app.api.result.ErrorCode
import eu.vitamo.app.api.result.ErrorCodeDefinition

enum class AuthErrorCode(
    override val code: ErrorCode,
) : ErrorCodeDefinition {
    INVALID_CREDENTIALS(
        ErrorCode("INVALID_CREDENTIALS"),
    ),
    INVALID_REFRESH_TOKEN(
        ErrorCode("INVALID_REFRESH_TOKEN"),
    ),
    INVALID_ACCESS_TOKEN(
        ErrorCode("INVALID_ACCESS_TOKEN"),
    ),
    EMAIL_NOT_VERIFIED(
        ErrorCode("EMAIL_NOT_VERIFIED"),
    ),
    EMAIL_ALREADY_EXISTS(
        ErrorCode("EMAIL_ALREADY_EXISTS"),
    ),
    EMAIL_VERIFICATION_FAILED(
        ErrorCode("EMAIL_VERIFICATION_EMAIL_FAILED"),
    ),
    VERIFICATION_ATTEMPTS_EXCEEDED(
        ErrorCode("VERIFICATION_ATTEMPTS_EXCEEDED"),
    ),
    INVALID_VERIFICATION_CODE(
        ErrorCode("INVALID_VERIFICATION_CODE"),
    ),
    INVALID_PASSWORD_RESET_TOKEN(
        ErrorCode("INVALID_PASSWORD_RESET_CODE"),
    ),
    FORGOT_PASSWORD_FAILED(
        ErrorCode("FORGOT_PASSWORD_FAILED_CODE"),
    ),
    INVALID_PASSWORD(
        ErrorCode("INVALID_PASSWORD")
    ),
    INVALID_EMAIL(
        ErrorCode("INVALID_EMAIL")
    )
    ;

    companion object {
        private val byCode = entries.associateBy { it.code }

        fun from(code: ErrorCode): AuthErrorCode? =
            byCode[code]
    }
}