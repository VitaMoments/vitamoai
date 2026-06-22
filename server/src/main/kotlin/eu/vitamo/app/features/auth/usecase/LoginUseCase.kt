package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.infrastructure.security.PasswordHashService
import eu.vitamo.app.features.user.repository.UserRepository
import io.ktor.http.HttpStatusCode

class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService,
    private val jwtService: JWTService,
    private val refreshTokenService: RefreshTokenService,
) {
    fun login(request: LoginRequest): LoginSession {
        val email = normalizeEmail(request.email)
        val user = userRepository.findByEmail(email)
            ?: throw AuthException.InvalidCredentials()

        if (!passwordHashService.verifyPassword(request.password, user.hashedPassword)) {
            throw AuthException.InvalidCredentials()
        }

        if (user.emailVerifiedAt == null) {
            throw AuthException.EmailNotVerified()
        }

        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = jwtService.generateRefreshToken()
        refreshTokenService.create(refreshToken, user.id, request.clientContext)

        return LoginSession(
            user = user,
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase().ifBlank {
            throw AuthException.InvalidCredentials()
        }
    }
}
