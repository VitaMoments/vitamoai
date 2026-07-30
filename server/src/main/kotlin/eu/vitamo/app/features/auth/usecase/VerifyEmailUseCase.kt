package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import eu.vitamo.app.features.user.repository.UserRepository
import kotlin.time.Clock

class VerifyEmailUseCase(
    private val userRepository: UserRepository,
    private val challengeRepository: EmailVerificationChallengeRepository,
    private val tokenHashService: TokenHashService,
) {
    suspend fun verify(request: VerifyEmailRequest): VerifyEmailResponse {
        val email = normalizeEmail(request.email)
        val now = Clock.System.now()
        val user = userRepository.findByEmail(email)
            ?: throw invalidCode()

//        for now, verifying email is done on register. so profile image is always null
        if (user.emailVerifiedAt != null) {
            return VerifyEmailResponse(
                user = user.toAuthenticatedUser(null),
                message = "Email already verified.",
                verified = true,
            )
        }

        val challenge = challengeRepository.findLatestActive(
            email = email,
            purpose = EmailVerificationPurpose.REGISTER_EMAIL_VERIFY,
            now = now,
        ) ?: throw invalidCode()

        if (challenge.attempts >= MAX_ATTEMPTS) {
            throw AuthException.VerificationAttemptsExceeded()
        }

        if (!tokenHashService.matches(request.code.trim(), challenge.codeHash)) {
            val nextAttempt = challenge.attempts + 1
            challengeRepository.incrementAttempts(challenge.id, now)
            if (nextAttempt >= MAX_ATTEMPTS) {
                throw AuthException.VerificationAttemptsExceeded()
            }
            throw invalidCode()
        }

        challengeRepository.markConsumed(challenge.id, now)
        userRepository.markEmailVerified(user.id, now, now.epochSeconds)

//        for now, verifying email is done on register. so profile image is always null
        return VerifyEmailResponse(
            user = user.toAuthenticatedUser(null),
            message = "Email verified successfully.",
            verified = true,
        )
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase().ifBlank { throw invalidCode() }
    }

    private fun invalidCode(): AuthException {
        return AuthException.InvalidVerificationCode()
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
