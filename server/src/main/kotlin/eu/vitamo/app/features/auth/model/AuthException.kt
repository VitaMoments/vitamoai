package eu.vitamo.app.features.auth.model

import eu.vitamo.app.api.contracts.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.EMAIL_NOT_VERIFIED_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.EMAIL_VERIFICATION_FAILED_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.FORGOT_PASSWORD_FAILED_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.INVALID_ACCESS_TOKEN_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.INVALID_CREDENTIALS_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.INVALID_PASSWORD_RESET_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.INVALID_REFRESH_TOKEN_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.INVALID_VERIFICATION_CODE
import eu.vitamo.app.api.contracts.auth.AuthErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED_CODE
import eu.vitamo.app.api.contracts.common.BaseErrorCode.BAD_REQUEST_CODE
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

    data class InvalidRefreshToken(
        override val message: String = "Invalid refresh token.",
    ) : AuthException(
        code = INVALID_REFRESH_TOKEN_CODE,
        message = message,
        status = HttpStatusCode.Unauthorized,
    )

    data class InvalidAccessTokenException(
        override val message: String = "Invalid access token.",
    ) : AuthException(
        code = INVALID_ACCESS_TOKEN_CODE,
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

    data class InvalidPasswordResetToken(
        override val message: String = "Invalid password reset token.",
    ) : AuthException(
        code = INVALID_PASSWORD_RESET_CODE,
        message = message,
        status = HttpStatusCode.Unauthorized,
    )

    data class ForgotPasswordFailed(
        override val message: String = "Failed to send password reset email.",
    ) : AuthException(
        code = FORGOT_PASSWORD_FAILED_CODE,
        message = message,
        status = HttpStatusCode.InternalServerError,
    )

    data class BadRequest(
        override val code: String = BAD_REQUEST_CODE,
        override val message: String = "Bad request.",
    ) : AuthException(
        code = code,
        message = message,
        status = HttpStatusCode.BadRequest,
    )
}
