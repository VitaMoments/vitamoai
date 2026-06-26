package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.EmailVerificationCodeService
import eu.vitamo.app.features.auth.service.AuthMailSender
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.features.user.repository.UserRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ResendEmailVerificationUseCase(
    private val userRepository: UserRepository,
    private val challengeRepository: EmailVerificationChallengeRepository,
    private val codeService: EmailVerificationCodeService,
    private val mailSender: AuthMailSender,
    private val tokenHashService: TokenHashService,
) {
    suspend fun resend(request: ResendEmailVerificationRequest): ResendEmailVerificationResponse {
        val email = normalizeEmail(request.email)
        val user = userRepository.findByEmail(email) ?: return genericResponse()

        if (user.emailVerifiedAt != null) {
            return genericResponse()
        }

        val now = Clock.System.now()
        val latestChallenge = challengeRepository.findLatestByEmailAndPurpose(
            email = email,
            purpose = EmailVerificationPurpose.REGISTER_EMAIL_VERIFY,
        )
        if (latestChallenge != null && latestChallenge.createdAt >= now - RESEND_COOLDOWN) {
            return genericResponse()
        }

        val code = codeService.generateCode()
        challengeRepository.consumeActiveForUserAndPurpose(
            userId = user.id,
            purpose = EmailVerificationPurpose.REGISTER_EMAIL_VERIFY,
            consumedAt = now,
        )

        val challenge = challengeRepository.create(
            userId = user.id,
            email = email,
            codeHash = tokenHashService.hash(code),
            purpose = EmailVerificationPurpose.REGISTER_EMAIL_VERIFY,
            createdAt = now,
            expiresAt = now + VERIFICATION_CODE_TTL,
        )

        try {
            mailSender.sendVerificationEmail(
                email = email,
                displayName = user.displayName,
                code = code,
                expiresInMinutes = VERIFICATION_CODE_TTL.inWholeMinutes.toInt(),
            )
        } catch (exception: Exception) {
            challengeRepository.deleteById(challenge.id)
            throw AuthException.EmailVerificationFailed()
        }

        return genericResponse()
    }

    private fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !EMAIL_REGEX.matches(normalized)) {
            throw AuthException.BadRequest(
                message = "Email is invalid."
            )
        }
        return normalized
    }

    private fun genericResponse(): ResendEmailVerificationResponse {
        return ResendEmailVerificationResponse(
            message = GENERIC_MESSAGE,
        )
    }

    private companion object {
        val VERIFICATION_CODE_TTL = 15.minutes
        val RESEND_COOLDOWN = 60.seconds
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        const val GENERIC_MESSAGE = "If an account exists and requires verification, a new verification email has been sent."
    }
}
