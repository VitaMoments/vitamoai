package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.device.ClientContext
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException.InvalidCredentials
import eu.vitamo.app.exception.AuthException.EmailNotVerified
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.device.model.DeviceRecord
import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.security.PasswordHashService
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class LoginUseCase(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val passwordHashService: PasswordHashService,
    private val jwtService: JWTService,
    private val refreshTokenService: RefreshTokenService,
) {

    suspend fun login(
        request: LoginRequest,
    ): LoginSession {
        val email = normalizeEmail(
            request.email,
        )

        val credentials = when (
            val result =
                userRepository.findUserWithCredentials(email)
        ) {
            is RepositoryResult.Success ->
                result.data

            is RepositoryResult.Error ->
                when (result.error) {
                    is RepositoryError.NotFound ->
                        throw InvalidCredentials()

                    else ->
                        throw ApiException.Internal(
                            message = "Failed to retrieve user credentials.",
                        )
                }
        }

        val (user, passwordHash) =
            credentials

        val passwordIsValid =
            passwordHashService.verifyPassword(
                rawPassword = request.password,
                passwordHash = passwordHash,
            )

        if (!passwordIsValid) {
            throw InvalidCredentials()
        }

        if (user.emailVerifiedAt == null) {
            throw EmailNotVerified()
        }

        val device = getOrCreateDevice(
            userId = user.id,
            context = request.clientContext,
        )

        val accessToken =
            jwtService.generateAccessToken(
                userId = user.id,
                deviceId = device.id,
            )

        val refreshToken =
            jwtService.generateRefreshToken()

        refreshTokenService.create(
            authToken = refreshToken,
            userId = user.id,
            deviceId = device.id,
        )

        return LoginSession(
            user = user.toAuthenticatedUser(null),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private suspend fun getOrCreateDevice(
        userId: Uuid,
        context: ClientContext,
    ): DeviceRecord {
        return when (
            val result = deviceRepository.upsert(
                userId = userId,
                context = context,
            )
        ) {
            is RepositoryResult.Success ->
                result.data

            is RepositoryResult.Error ->
                throw ApiException.Internal(
                    message = "Failed to register client device.",
                )
        }
    }

    private fun normalizeEmail(
        email: String,
    ): String {
        return email
            .trim()
            .lowercase()
            .ifBlank {
                throw InvalidCredentials()
            }
    }
}