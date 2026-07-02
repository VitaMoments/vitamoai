package eu.vitamo.app.auth.repository

import eu.vitamo.app.api.contracts.auth.ForgotPasswordRequest
import eu.vitamo.app.api.contracts.auth.ForgotPasswordResponse
import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.api.contracts.auth.ResetPasswordRequest
import eu.vitamo.app.api.contracts.auth.ResetPasswordResponse
import eu.vitamo.app.api.contracts.auth.SessionResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.auth.api.AuthApiConfig
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.ClearableCookieStorage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.auth.AuthStatus
import eu.vitamo.app.testsupport.fakeUser
import eu.vitamo.app.testsupport.sessionResponse
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import kotlinx.coroutines.runBlocking

class DefaultAuthRepositoryTest {
    @Test
    fun login_marksSessionAuthenticated() = runBlocking {
        val api = FakeAuthApi()
        val cookieStorage = ClearableCookieStorage()
        val coordinator = createCoordinator(api, cookieStorage)
        val repository = DefaultAuthRepository(api, coordinator)

        val response = repository.login("test@example.com", "secret")

        assertEquals("test@example.com", (response as eu.vitamo.app.repository.RepositoryResult.Success).data.email)
        assertEquals(1, api.loginCalls)
        assertEquals(AuthStatus.Authenticated, coordinator.state.value)
    }

    @Test
    fun currentSessionUser_usesSessionEndpoint() = runBlocking {
        val api = FakeAuthApi().apply {
            sessionResult = ApiResult.Success(sessionResponse("session@example.com"))
        }
        val repository = DefaultAuthRepository(api, createCoordinator(api, ClearableCookieStorage()))

        val user = repository.currentSessionUser()

        assertEquals("session@example.com", (user as eu.vitamo.app.repository.RepositoryResult.Success).data.email)
        assertEquals(1, api.sessionCalls)
    }

    @Test
    fun logout_signsOutAndClearsCookies() = runBlocking {
        val api = FakeAuthApi()
        val cookieStorage = ClearableCookieStorage()
        cookieStorage.addCookie(
            Url("https://example.com"),
            Cookie(name = "access_token", value = "cookie"),
        )
        val coordinator = createCoordinator(api, cookieStorage)
        val repository = DefaultAuthRepository(api, coordinator)

        repository.logout()

        assertEquals(1, api.logoutCalls)
        assertEquals(0, cookieStorage.get(Url("https://example.com/")).size)
        assertEquals(AuthStatus.Unauthenticated, coordinator.state.value)
    }

    @Test
    fun logout_clearsCookiesWhenServerLogoutFails() = runBlocking {
        val api = FakeAuthApi().apply { logoutThrowable = IllegalStateException("server failed") }
        val coordinator = createCoordinator(api, ClearableCookieStorage())
        val repository = DefaultAuthRepository(api, coordinator)

        assertFailsWith<IllegalStateException> {
            repository.logout()
        }

        assertEquals(1, api.logoutCalls)
        assertEquals(AuthStatus.Unauthenticated, coordinator.state.value)
    }

    private fun createCoordinator(api: FakeAuthApi, cookieStorage: AuthCookieStorage) =
        AuthSessionCoordinator(api, AuthApiConfig(baseUrl = "https://example.com"), cookieStorage)

    private class FakeAuthApi : AuthApi {
        var sessionResult: ApiResult<SessionResponse> = ApiResult.Success(sessionResponse("session@example.com"))
        var loginCalls: Int = 0
        var sessionCalls: Int = 0
        var logoutCalls: Int = 0
        var logoutThrowable: Throwable? = null

        override suspend fun register(request: RegisterRequest): ApiResult<RegisterResponse> {
            return ApiResult.Success(RegisterResponse("ok", true))
        }

        override suspend fun login(request: LoginRequest): ApiResult<LoginResponse> {
            loginCalls += 1
            return ApiResult.Success(
                LoginResponse(
                    user = fakeUser(request.email),
                )
            )
        }

        override suspend fun verifyEmail(request: VerifyEmailRequest): ApiResult<VerifyEmailResponse> {
            return ApiResult.Success(
                VerifyEmailResponse(
                    user = fakeUser(request.email),
                    message = "ok",
                    verified = true,
                )
            )
        }

        override suspend fun resendEmailVerification(
            request: ResendEmailVerificationRequest,
        ): ApiResult<ResendEmailVerificationResponse> {
            return ApiResult.Success(ResendEmailVerificationResponse(message = "ok"))
        }

        override suspend fun session(): ApiResult<SessionResponse> {
            sessionCalls += 1
            return sessionResult
        }

        override suspend fun refreshSession(): ApiResult<SessionResponse> {
            return sessionResult
        }

        override suspend fun logout(): ApiResult<Unit> {
            logoutCalls += 1
            logoutThrowable?.let { throw it }
            return ApiResult.Success(Unit)
        }

        override suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResult<ForgotPasswordResponse> {
            return ApiResult.Success(ForgotPasswordResponse())
        }

        override suspend fun resetPassword(request: ResetPasswordRequest): ApiResult<ResetPasswordResponse> {
            return ApiResult.Success(ResetPasswordResponse(message = "ok", passwordChanged = true))
        }
    }

}
