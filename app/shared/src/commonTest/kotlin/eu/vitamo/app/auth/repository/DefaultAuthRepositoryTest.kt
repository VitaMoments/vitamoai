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
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.ClearableCookieStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid
import kotlinx.coroutines.runBlocking

class DefaultAuthRepositoryTest {
    @Test
    fun login_doesNotStoreTokens() = runBlocking {
        val api = FakeAuthApi()
        val cookieStorage = ClearableCookieStorage()
        val repository = DefaultAuthRepository(api, cookieStorage)

        val response = repository.login(LoginRequest(email = "test@example.com", password = "secret"))

        assertEquals("test@example.com", response.user.email)
        assertEquals(0, api.logoutCalls)
    }

    @Test
    fun currentSessionUser_usesSessionEndpoint() = runBlocking {
        val api = FakeAuthApi().apply {
            sessionUser = fakeUser("session@example.com")
        }
        val repository = DefaultAuthRepository(api, FakeAuthCookieStorage())

        val user = repository.currentSessionUser()

        assertEquals("session@example.com", user?.email)
        assertEquals(1, api.sessionCalls)
    }

    @Test
    fun currentSessionUser_returnsNullWhenUnauthenticated() = runBlocking {
        val api = FakeAuthApi().apply {
            sessionUser = null
        }
        val repository = DefaultAuthRepository(api, FakeAuthCookieStorage())

        val user = repository.currentSessionUser()

        assertEquals(null, user)
        assertEquals(1, api.sessionCalls)
    }

    @Test
    fun logout_callsBackendAndClearsCookies() = runBlocking {
        val api = FakeAuthApi()
        val cookieStorage = ClearableCookieStorage()
        cookieStorage.addCookie(
            Url("https://example.com"),
            Cookie(name = "access_token", value = "cookie"),
        )
        val repository = DefaultAuthRepository(api, cookieStorage)

        repository.logout()

        assertEquals(1, api.logoutCalls)
        assertEquals(0, cookieStorage.get(Url("https://example.com/")).size)
    }

    @Test
    fun logout_clearsCookiesWhenServerLogoutFails() = runBlocking {
        val api = FakeAuthApi().apply { logoutThrowable = IllegalStateException("server failed") }
        val cookieStorage = FakeAuthCookieStorage()
        val repository = DefaultAuthRepository(api, cookieStorage)

        assertFailsWith<IllegalStateException> {
            repository.logout()
        }

        assertEquals(1, api.logoutCalls)
        assertEquals(1, cookieStorage.clearAuthCookiesCalls)
    }

    @Test
    fun logout_clearsCookiesWhenNetworkExceptionOccurs() = runBlocking {
        val api = FakeAuthApi().apply { logoutThrowable = NetworkException("network down") }
        val cookieStorage = FakeAuthCookieStorage()
        val repository = DefaultAuthRepository(api, cookieStorage)

        assertFailsWith<NetworkException> {
            repository.logout()
        }

        assertEquals(1, api.logoutCalls)
        assertEquals(1, cookieStorage.clearAuthCookiesCalls)
    }

    private fun fakeUser(email: String): AuthenticatedUser {
        return AuthenticatedUser(
            id = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
            displayName = "Test User",
            bio = null,
            role = UserRole.USER,
            firstName = "Test",
            lastName = "User",
            alias = null,
            birthDate = null,
            email = email,
        )
    }

    private class FakeAuthApi : AuthApi {
        var sessionUser: AuthenticatedUser? = null
        var logoutCalls: Int = 0
        var sessionCalls: Int = 0
        var logoutThrowable: Throwable? = null

        override suspend fun register(request: RegisterRequest): RegisterResponse {
            return RegisterResponse("ok", true)
        }

        override suspend fun login(request: LoginRequest): LoginResponse {
            return LoginResponse(
                user = AuthenticatedUser(
                    id = Uuid.parse("123e4567-e89b-12d3-a456-426614174001"),
                    displayName = "Test User",
                    bio = null,
                    role = UserRole.USER,
                    firstName = null,
                    lastName = null,
                    alias = null,
                    birthDate = null,
                    email = request.email,
                )
            )
        }

        override suspend fun verifyEmail(request: VerifyEmailRequest): VerifyEmailResponse {
            return VerifyEmailResponse(message = "ok", verified = true)
        }

        override suspend fun resendEmailVerification(
            request: ResendEmailVerificationRequest,
        ): ResendEmailVerificationResponse {
            return ResendEmailVerificationResponse(message = "ok")
        }

        override suspend fun session(): AuthenticatedUser? {
            sessionCalls += 1
            return sessionUser
        }

        override suspend fun logout() {
            logoutCalls += 1
            logoutThrowable?.let { throw it }
        }
    }

    private class FakeAuthCookieStorage : AuthCookieStorage {
        var clearAuthCookiesCalls: Int = 0
        var clearAllCookiesCalls: Int = 0

        override suspend fun clearAuthCookies() {
            clearAuthCookiesCalls += 1
        }

        override suspend fun clearAllCookies() {
            clearAllCookiesCalls += 1
        }

        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = Unit

        override suspend fun get(requestUrl: Url): List<Cookie> = emptyList()

        override fun close() = Unit
    }

    private class NetworkException(message: String) : Exception(message)
}
