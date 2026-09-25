package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.user.capabilities.UserCapabilities
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.user.context.UserCapabilitiesProvider
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class RefreshSessionUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val jwtService: JWTService,
    private val userCapabilitiesProvider: UserCapabilitiesProvider,
) {

    suspend fun refresh(
        refreshToken: String,
    ): LoginSession {
        val currentToken =
            refreshTokenService.findValid(refreshToken)
                ?: throw AuthException.InvalidRefreshToken()

        val userRecord =
            findUser(
                userId = currentToken.userId.value,
                onNotFound = {
                    AuthException.InvalidRefreshToken()
                },
            )

        val deviceId =
            currentToken.deviceId.value

        /*
         * Eerst oude token revoken.
         *
         * Daarna wordt de nieuwe refresh token
         * aan exact hetzelfde device gekoppeld.
         */
        refreshTokenService.revoke(
            currentToken.id,
        )

        return createLoginSession(
            userRecord = userRecord,
            deviceId = deviceId,
        )
    }

    suspend fun createSessionByUserId(
        userId: Uuid,
        deviceId: Uuid,
    ): LoginSession {
        val userRecord =
            findUser(
                userId = userId,
                onNotFound = {
                    AuthException.InvalidAccessToken()
                },
            )

        return createLoginSession(
            userRecord = userRecord,
            deviceId = deviceId,
        )
    }

    private suspend fun findUser(
        userId: Uuid,
        onNotFound: () -> RuntimeException,
    ): UserRecord =
        when (
            val result =
                userRepository.findById(userId)
        ) {
            is RepositoryResult.Success ->
                result.data

            is RepositoryResult.Error ->
                when (result.error) {
                    is RepositoryError.NotFound ->
                        throw onNotFound()

                    else ->
                        throw ApiException.Internal(
                            message =
                                "Failed to retrieve user while creating a login session.",
                        )
                }
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
            is RepositoryResult.Success ->
                result.data

            is RepositoryResult.Error ->
                throw ApiException.Internal(
                    message =
                        "Failed to resolve user capabilities while creating a login session.",
                )
        }

    private suspend fun createLoginSession(
        userRecord: UserRecord,
        deviceId: Uuid,
    ): LoginSession {
        /*
         * Altijd opnieuw resolven.
         *
         * Hierdoor wordt bijvoorbeeld een wijziging
         * van BASIC -> PLUS bij een session refresh
         * direct meegenomen.
         */
        val capabilities =
            resolveCapabilities(
                userId = userRecord.id,
            )

        val accessToken =
            jwtService.generateAccessToken(
                userId = userRecord.id,
                deviceId = deviceId,
            )

        val refreshToken =
            jwtService.generateRefreshToken()

        refreshTokenService.create(
            authToken = refreshToken,
            userId = userRecord.id,
            deviceId = deviceId,
        )

        return LoginSession(
            user = userRecord.toAuthenticatedUser(
                profileImage = null,
                capabilities = capabilities,
            ),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}