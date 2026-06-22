package eu.vitamo.app.auth.repository

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.network.AuthCookieStorage

class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val cookieStorage: AuthCookieStorage,
) : AuthRepository {
    override suspend fun register(request: RegisterRequest): RegisterResponse {
        return authApi.register(request)
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        return authApi.login(request)
    }

    override suspend fun verifyEmail(request: VerifyEmailRequest): VerifyEmailResponse {
        return authApi.verifyEmail(request)
    }

    override suspend fun resendEmailVerification(
        request: ResendEmailVerificationRequest,
    ): ResendEmailVerificationResponse {
        return authApi.resendEmailVerification(request)
    }

    override suspend fun currentSessionUser(): AuthenticatedUser? {
        return authApi.session()
    }

    override suspend fun logout() {
        try {
            authApi.logout()
        } finally {
            cookieStorage.clearAuthCookies()
        }
    }
}
