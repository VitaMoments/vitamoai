package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.api.contracts.user.capabilities.UserCapabilities
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.features.user.context.UserCapabilitiesProvider
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlin.time.Clock
import kotlin.uuid.Uuid

class VerifyEmailUseCase(
    private val userRepository: UserRepository,
    private val challengeRepository: EmailVerificationChallengeRepository,
    private val tokenHashService: TokenHashService,
    private val userCapabilitiesProvider: UserCapabilitiesProvider,
) {

    suspend fun verify(
        request: VerifyEmailRequest,
    ): VerifyEmailResponse {
        val email =
            normalizeEmail(
                request.email,
            )

        val now =
            Clock.System.now()

        val user =
            userRepository.findByEmail(email)
                ?: throw invalidCode()

        /*
         * Voor nu wordt email alleen tijdens
         * registratie geverifieerd.
         * Daarom is profileImage nog null.
         */
        if (user.emailVerifiedAt != null) {
            return createResponse(
                user = user,
                message = "Email already verified.",
            )
        }

        val challenge =
            challengeRepository.findLatestActive(
                email = email,
                purpose =
                    EmailVerificationPurpose
                        .REGISTER_EMAIL_VERIFY,
                now = now,
            ) ?: throw invalidCode()

        if (
            challenge.attempts >=
            MAX_ATTEMPTS
        ) {
            throw AuthException
                .VerificationAttemptsExceeded()
        }

        if (
            !tokenHashService.matches(
                request.code.trim(),
                challenge.codeHash,
            )
        ) {
            val nextAttempt =
                challenge.attempts + 1

            challengeRepository.incrementAttempts(
                challenge.id,
                now,
            )

            if (nextAttempt >= MAX_ATTEMPTS) {
                throw AuthException
                    .VerificationAttemptsExceeded()
            }

            throw invalidCode()
        }

        challengeRepository.markConsumed(
            challenge.id,
            now,
        )

        userRepository.markEmailVerified(
            user.id,
            now,
            now.epochSeconds,
        )

        return createResponse(
            user = user,
            message = "Email verified successfully.",
        )
    }

    private suspend fun createResponse(
        user: UserRecord,
        message: String,
    ): VerifyEmailResponse {
        val capabilities =
            resolveCapabilities(
                userId = user.id,
            )

        return VerifyEmailResponse(
            user = user.toAuthenticatedUser(
                profileImage = null,
                capabilities = capabilities,
            ),
            message = message,
            verified = true,
        )
    }

    private suspend fun resolveCapabilities(
        userId: Uuid,
    ): UserCapabilities =
        when (
            val result =
                userCapabilitiesProvider.resolve(
                    userId = userId,
                )
        ) {
            is RepositoryResult.Success -> {
                result.data
            }

            is RepositoryResult.Error -> {
                throw ApiException.Internal(
                    message =
                        "Failed to resolve user capabilities while verifying email.",
                )
            }
        }

    private fun normalizeEmail(
        email: String,
    ): String =
        email
            .trim()
            .lowercase()
            .ifBlank {
                throw invalidCode()
            }

    private fun invalidCode(): AuthException =
        AuthException
            .InvalidVerificationCode()

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}