package eu.vitamo.app.exception

import eu.vitamo.app.api.contracts.errorcodes.AuthErrorCode
import eu.vitamo.app.api.result.ErrorCode
import io.ktor.http.HttpStatusCode

sealed class AuthException(
    val authErrorCode: AuthErrorCode,
    override val message: String,
    val status: HttpStatusCode,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {

    val code: ErrorCode
        get() = authErrorCode.code

    class InvalidCredentials(
        message: String = "Invalid email or password.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.INVALID_CREDENTIALS,
        message = message,
        status = HttpStatusCode.Unauthorized,
        cause = cause,
    )

    class InvalidRefreshToken(
        message: String = "Invalid refresh token.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.INVALID_REFRESH_TOKEN,
        message = message,
        status = HttpStatusCode.Unauthorized,
        cause = cause,
    )

    class InvalidAccessToken(
        message: String = "Invalid access token.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.INVALID_ACCESS_TOKEN,
        message = message,
        status = HttpStatusCode.Unauthorized,
        cause = cause,
    )

    class EmailNotVerified(
        message: String = "Email not verified.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.EMAIL_NOT_VERIFIED,
        message = message,
        status = HttpStatusCode.Forbidden,
        cause = cause,
    )

    class EmailAlreadyExists(
        message: String = "Email is already registered.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.EMAIL_ALREADY_EXISTS,
        message = message,
        status = HttpStatusCode.Conflict,
        cause = cause,
    )

    class EmailVerificationFailed(
        message: String = "Failed to send verification email.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.EMAIL_VERIFICATION_FAILED,
        message = message,
        status = HttpStatusCode.InternalServerError,
        cause = cause,
    )

    class VerificationAttemptsExceeded(
        message: String = "Verification attempts exceeded.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED,
        message = message,
        status = HttpStatusCode.TooManyRequests,
        cause = cause,
    )

    class InvalidVerificationCode(
        message: String = "Invalid verification code.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.INVALID_VERIFICATION_CODE,
        message = message,
        status = HttpStatusCode.Unauthorized,
        cause = cause,
    )

    class InvalidPasswordResetToken(
        message: String = "Invalid password reset token.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.INVALID_PASSWORD_RESET_TOKEN,
        message = message,
        status = HttpStatusCode.Unauthorized,
        cause = cause,
    )

    class ForgotPasswordFailed(
        message: String = "Failed to send password reset email.",
        cause: Throwable? = null,
    ) : AuthException(
        authErrorCode = AuthErrorCode.FORGOT_PASSWORD_FAILED,
        message = message,
        status = HttpStatusCode.InternalServerError,
        cause = cause,
    )

    class InvalidPassword(
        message: String = "Password is not valid",
        cause: Throwable? = null
    ): AuthException(
        authErrorCode = AuthErrorCode.INVALID_PASSWORD,
        message = message,
        status = HttpStatusCode.BadRequest,
        cause = cause,
    )

    class InvalidEmail(
        message: String = "Email address is not valid",
        cause: Throwable? = null
    ): AuthException(
        authErrorCode = AuthErrorCode.INVALID_EMAIL,
        message = message,
        status = HttpStatusCode.BadRequest,
        cause = cause,
    )

}