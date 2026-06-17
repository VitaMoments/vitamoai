package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.EmailVerificationCodeService
import eu.vitamo.app.features.auth.service.EmailVerificationMailSender
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.infrastructure.security.PasswordHashService
import eu.vitamo.app.features.user.repository.UserRepository
import io.ktor.http.HttpStatusCode
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class RegisterUseCase(
    private val userRepository: UserRepository,
    private val challengeRepository: EmailVerificationChallengeRepository,
    private val codeService: EmailVerificationCodeService,
    private val mailSender: EmailVerificationMailSender,
    private val tokenHashService: TokenHashService,
    private val passwordHashService: PasswordHashService,
) {
    suspend fun register(request: RegisterRequest): RegisterResponse {
        val email = normalizeEmail(request.email)
        if (userRepository.findByEmail(email) != null) {
            throw AuthException(
                code = "EMAIL_ALREADY_EXISTS",
                message = "Email is already registered.",
                status = HttpStatusCode.Conflict,
            )
        }

        val now = Clock.System.now()
        val createdAt = now.epochSeconds
        val user = userRepository.createUser(
            email = email,
            displayName = request.displayName.trim(),
            hashedPassword = passwordHashService.hashPassword(request.password),
            firstName = request.firstName?.trim()?.ifBlank { null },
            lastName = request.lastName?.trim()?.ifBlank { null },
            alias = request.alias?.trim()?.ifBlank { null },
            birthDate = request.birthDate,
            now = createdAt,
        )
        val code = codeService.generateCode()
        val expiresAt = now + VERIFICATION_CODE_TTL

        val challenge = challengeRepository.create(
            userId = user.id,
            email = email,
            codeHash = tokenHashService.hash(code),
            purpose = EmailVerificationPurpose.REGISTER_EMAIL_VERIFY,
            createdAt = now,
            expiresAt = expiresAt,
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
            userRepository.deleteById(user.id)
            throw AuthException(
                code = "EMAIL_VERIFICATION_EMAIL_FAILED",
                message = "Registration could not be completed.",
                status = HttpStatusCode.InternalServerError,
            )
        }

        return RegisterResponse(
            message = "Registration successful. Please verify your email.",
            emailVerificationRequired = true,
        )
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase().ifBlank {
            throw AuthException(
                code = "INVALID_EMAIL",
                message = "Email is required.",
                status = HttpStatusCode.BadRequest,
            )
        }
    }

    private companion object {
        val VERIFICATION_CODE_TTL = 15.minutes
    }
}
