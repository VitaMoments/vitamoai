package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.ForgotPasswordRequest
import eu.vitamo.app.api.contracts.auth.ForgotPasswordResponse
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.database.helpers.kotlinUuid
import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.repository.PasswordResetTokenRepository
import eu.vitamo.app.features.auth.service.AuthMailSender
import eu.vitamo.app.features.auth.service.PasswordResetTokenService
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.validation.EmailValidator
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
    private val tokenRepository: PasswordResetTokenRepository,
    private val tokenService: PasswordResetTokenService,
    private val mailService: AuthMailSender,
) {
    suspend operator fun invoke(request: ForgotPasswordRequest) : ForgotPasswordResponse {
        val email = EmailValidator.normalizeOrThrow(request.email) {
            throw AuthException.BadRequest(
                message = "Email is invalid."
            )
        }
        val user = userRepository.findByEmail(email) ?: return ForgotPasswordResponse()

        val now = kotlin.time.Clock.System.now()
        val latestToken = tokenRepository.findLatestByUserId(user.id)
        if (latestToken != null && latestToken.createdAt >= now - RESEND_COOLDOWN) {
            return ForgotPasswordResponse()
        }
        val hashedToken = tokenService.generateHashedToken()
        val token = tokenRepository.create(
            userId = user.id,
            tokenHash = hashedToken.second,
            createdAt = now,
            expiresAt = now + VERIFICATION_CODE_TTL,
        )

        try {
            mailService.sendPasswordResetMail(
                email = email,
                displayName = user.displayName,
                resetToken = hashedToken.first
            )
        } catch (e: Exception) {
            tokenRepository.consumeIfActive(
                tokenId = token.id,
                consumedAt = now,
            )
            throw AuthException.ForgotPasswordFailed()
        }
        return ForgotPasswordResponse()
    }

    private companion object {
        val VERIFICATION_CODE_TTL = 15.minutes
        val RESEND_COOLDOWN = 60.seconds
    }

}