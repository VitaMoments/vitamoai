package eu.vitamo.app.api.contracts.auth

import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.auth.ClientContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val alias: String? = null,
    val birthDate: LocalDate? = null,
)

@Serializable
data class RegisterResponse(
    val message: String,
    val emailVerificationRequired: Boolean,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val clientContext: ClientContext? = null,
)

@Serializable
data class LoginResponse(
    val user: AuthenticatedUser
)

@Serializable
data class VerifyEmailRequest(
    val email: String,
    val code: String,
)

@Serializable
data class VerifyEmailResponse(
    val user: AuthenticatedUser,
    val message: String,
    val verified: Boolean,
)

@Serializable
data class ResendEmailVerificationRequest(
    val email: String,
)

@Serializable
data class ResendEmailVerificationResponse(
    val message: String,
)

@Serializable
data class SessionResponse(
    val user: AuthenticatedUser,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class ForgotPasswordResponse(
    val message: String = "If an account exists for this email, a password reset link has been sent."
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val newPassword: String,
)

@Serializable
data class ResetPasswordResponse(
    val message: String,
    val passwordChanged: Boolean,
)