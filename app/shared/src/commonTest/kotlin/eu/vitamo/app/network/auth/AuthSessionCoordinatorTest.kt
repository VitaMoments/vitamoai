package eu.vitamo.app.network.auth

import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.SessionResponse
import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.auth.api.AuthApiConfig
import eu.vitamo.app.network.ClearableCookieStorage
import eu.vitamo.app.testsupport.sessionResponse
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class AuthSessionCoordinatorTest {
    @Test
    fun bootstrap_refreshesWhenSessionIsUnauthorized() = runBlocking {
        val api = FakeAuthApi(
            sessionResult = ApiResult.Error(ApiError(code = "INVALID_CREDENTIALS", message = "nope", status = 401)),
            refreshResult = ApiResult.Success(sessionResponse("session@example.com")),
        )
        val storage = ClearableCookieStorage()
        storage.addCookie(Url("https://example.com"), Cookie(name = "access_token", value = "access"))
        storage.addCookie(Url("https://example.com"), Cookie(name = "refresh_token", value = "refresh"))
        val coordinator = AuthSessionCoordinator(api, AuthApiConfig(baseUrl = "https://example.com/api/auth"), storage)

        coordinator.bootstrap()

        assertEquals(1, api.sessionCalls)
        assertEquals(1, api.refreshCalls)
        assertEquals(AuthStatus.Authenticated, coordinator.state.value)
    }

    @Test
    fun bootstrap_signsOutWhenRefreshFails() = runBlocking {
        val api = FakeAuthApi(
            sessionResult = ApiResult.Error(ApiError(code = "INVALID_CREDENTIALS", message = "nope", status = 401)),
            refreshResult = ApiResult.Error(ApiError(code = "INVALID_REFRESH_TOKEN", message = "bad", status = 401)),
        )
        val storage = ClearableCookieStorage()
        storage.addCookie(Url("https://example.com"), Cookie(name = "access_token", value = "access"))
        storage.addCookie(Url("https://example.com"), Cookie(name = "refresh_token", value = "refresh"))
        val coordinator = AuthSessionCoordinator(api, AuthApiConfig(baseUrl = "https://example.com/api/auth"), storage)

        coordinator.bootstrap()

        assertEquals(1, api.sessionCalls)
        assertEquals(1, api.refreshCalls)
        assertEquals(0, storage.get(Url("https://example.com/")).size)
        assertEquals(AuthStatus.Unauthenticated, coordinator.state.value)
    }

    private class FakeAuthApi(
        private val sessionResult: ApiResult<SessionResponse>,
        private val refreshResult: ApiResult<SessionResponse>,
    ) : AuthApi {
        var sessionCalls = 0
        var refreshCalls = 0

        override suspend fun register(request: eu.vitamo.app.api.contracts.auth.RegisterRequest) =
            ApiResult.Success(eu.vitamo.app.api.contracts.auth.RegisterResponse("ok", true))

        override suspend fun login(request: eu.vitamo.app.api.contracts.auth.LoginRequest) =
            ApiResult.Success(LoginResponse(user = sessionResponse(request.email).user))

        override suspend fun verifyEmail(request: eu.vitamo.app.api.contracts.auth.VerifyEmailRequest) =
            ApiResult.Success(eu.vitamo.app.api.contracts.auth.VerifyEmailResponse(user = sessionResponse(request.email).user, message = "ok", verified = true))

        override suspend fun resendEmailVerification(request: eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest) =
            ApiResult.Success(eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse(message = "ok"))

        override suspend fun session(): ApiResult<SessionResponse> {
            sessionCalls += 1
            return sessionResult
        }

        override suspend fun refreshSession(): ApiResult<SessionResponse> {
            refreshCalls += 1
            return refreshResult
        }

        override suspend fun logout(): ApiResult<Unit> = ApiResult.Success(Unit)

        override suspend fun forgotPassword(request: eu.vitamo.app.api.contracts.auth.ForgotPasswordRequest) =
            ApiResult.Success(eu.vitamo.app.api.contracts.auth.ForgotPasswordResponse())

        override suspend fun resetPassword(request: eu.vitamo.app.api.contracts.auth.ResetPasswordRequest) =
            ApiResult.Success(eu.vitamo.app.api.contracts.auth.ResetPasswordResponse(message = "ok", passwordChanged = true))
    }
}
