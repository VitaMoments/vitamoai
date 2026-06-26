package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.repository.EmailVerificationChallengeRepository
import eu.vitamo.app.features.auth.service.EmailVerificationCodeService
import eu.vitamo.app.features.auth.service.AuthMailSender
import eu.vitamo.app.features.auth.service.TokenHashService
import eu.vitamo.app.infrastructure.security.PasswordHashService
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.validation.EmailValidator
import eu.vitamo.app.validation.PasswordValidator
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class RegisterUseCase(
    private val userRepository: UserRepository,
    private val challengeRepository: EmailVerificationChallengeRepository,
    private val codeService: EmailVerificationCodeService,
    private val mailSender: AuthMailSender,
    private val tokenHashService: TokenHashService,
    private val passwordHashService: PasswordHashService,
) {
    suspend fun register(request: RegisterRequest): RegisterResponse {
        val email = EmailValidator.normalizeOrThrow(request.email) {
            throw AuthException.BadRequest(
                message = "Email is invalid.",
            )
        }
        if (userRepository.findByEmail(email) != null) {
            throw AuthException.EmailAlreadyExists()
        }

        PasswordValidator.validateOrThrow(request.password) {
            throw AuthException.BadRequest(
                message = "Password is invalid: $it",
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
            throw AuthException.EmailVerificationFailed()
        }

        return RegisterResponse(
            message = "Registration successful. Please verify your email.",
            emailVerificationRequired = true,
        )
    }

    private companion object {
        val VERIFICATION_CODE_TTL = 15.minutes
    }
}
