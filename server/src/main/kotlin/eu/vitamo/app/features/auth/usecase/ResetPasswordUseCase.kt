package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.ResetPasswordRequest
import eu.vitamo.app.api.contracts.auth.ResetPasswordResponse
import eu.vitamo.app.database.helpers.kotlinUuid
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.features.auth.repository.PasswordResetTokenRepository
import eu.vitamo.app.features.auth.service.PasswordResetTokenService
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.security.PasswordHashService
import eu.vitamo.app.validation.EmailValidator
import eu.vitamo.app.validation.PasswordValidator
import kotlin.time.Clock

class ResetPasswordUseCase(
    val tokenRepository: PasswordResetTokenRepository,
    val userRepository: UserRepository,
    val tokenService: PasswordResetTokenService,
    val passwordHashService: PasswordHashService
) {
    suspend operator fun invoke(request: ResetPasswordRequest) : ResetPasswordResponse {
        if (request.token.isBlank()) {
            throw AuthException.InvalidPasswordResetToken()
        }
        EmailValidator.normalizeOrThrow(request.email) {
            throw ApiException.BadRequest(
                message = "Email is invalid."
            )
        }
        PasswordValidator.validateOrThrow(request.newPassword) {
            throw ApiException.BadRequest(
                message = "Password is invalid: $it"
            )
        }

        val userRecord = userRepository.findByEmail(request.email) ?: throw AuthException.InvalidPasswordResetToken()

        val now = Clock.System.now()
        val tokenHash = tokenService.hashToken(request.token)
        val resetToken = tokenRepository.findByTokenHash(tokenHash) ?: throw AuthException.InvalidPasswordResetToken()

        if (resetToken.userId != userRecord.id) {
            tokenRepository.incrementAttempts(
                tokenId = resetToken.id,
                attemptedAt = now,
            )
            throw AuthException.InvalidPasswordResetToken()
        }

        if (resetToken.consumedAt != null || resetToken.attempts >= 5) {
            throw AuthException.InvalidPasswordResetToken()
        }
        if (resetToken.expiresAt < now) {
            tokenRepository.consumeIfActive(
                tokenId = resetToken.id,
                consumedAt = now,
            )
            throw AuthException.InvalidPasswordResetToken()
        }

        val consumed = tokenRepository.consumeIfActive(
            tokenId = resetToken.id,
            consumedAt = now,
        )

        if (!consumed) {
            throw AuthException.InvalidPasswordResetToken()
        }

        val newPassword = passwordHashService.hashPassword(request.newPassword)

        userRepository.updatePassword(userid = userRecord.id, hashedPassword = newPassword)

        tokenRepository.consumeActiveForUser(
            userId = userRecord.id,
            consumedAt = now,
        )

        return ResetPasswordResponse(
            message = "Password has been reset successfully.",
            passwordChanged = true
        )
    }
}