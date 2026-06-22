package eu.vitamo.app.features.auth.model

import io.ktor.http.HttpStatusCode

sealed class AuthException(
    open val code: String,
    override val message: String,
    open val status: HttpStatusCode,
) : RuntimeException(message) {

    data class InvalidCredentials(
        override val message: String = "Invalid email or password.",
    ) : AuthException(
        code = INVALID_CREDENTIALS_CODE,
        message = message,
        status = HttpStatusCode.Unauthorized,
    )

    data class EmailNotVerified(
        override val message: String = "Email not verified.",
    ) : AuthException(
        code = EMAIL_NOT_VERIFIED_CODE,
        message = message,
        status = HttpStatusCode.Forbidden,
    )

    data class EmailAlreadyExists(
        override val message: String = "Email is already registered.",
    ) : AuthException(
        code = EMAIL_ALREADY_EXISTS_CODE,
        message = message,
        status = HttpStatusCode.Conflict,
    )

    data class EmailVerificationFailed(
        override val message: String = "Failed to send verification email.",
    ) : AuthException(
        code = EMAIL_VERIFICATION_FAILED_CODE,
        message = message,
        status = HttpStatusCode.InternalServerError,
    )

    data class VerificationAttemptsExceeded(
        override val message: String = "Verification attempts exceeded.",
    ) : AuthException(
        code = VERIFICATION_ATTEMPTS_EXCEEDED_CODE,
        message = message,
        status = HttpStatusCode.TooManyRequests,
    )

    data class InvalidVerificationCode(
        override val message: String = "Invalid verification code.",
    ) : AuthException(
        code = INVALID_VERIFICATION_CODE,
        message = message,
        status = HttpStatusCode.Unauthorized,
    )

    data class BadRequest(
        override val code: String = BAD_REQUEST_CODE,
        override val message: String = "Bad request.",
    ) : AuthException(
        code = code,
        message = message,
        status = HttpStatusCode.BadRequest,
    )

    companion object {
        const val BAD_REQUEST_CODE = "BAD_REQUEST"
        const val INVALID_EMAIL_CODE = "INVALID_EMAIL"
        const val INVALID_CREDENTIALS_CODE = "INVALID_CREDENTIALS"
        const val EMAIL_NOT_VERIFIED_CODE = "EMAIL_NOT_VERIFIED"
        const val EMAIL_ALREADY_EXISTS_CODE = "EMAIL_ALREADY_EXISTS"
        const val VERIFICATION_ATTEMPTS_EXCEEDED_CODE = "VERIFICATION_ATTEMPTS_EXCEEDED"
        const val EMAIL_VERIFICATION_FAILED_CODE = "EMAIL_VERIFICATION_EMAIL_FAILED"
        const val INVALID_VERIFICATION_CODE = "INVALID_VERIFICATION_CODE"
    }
}
