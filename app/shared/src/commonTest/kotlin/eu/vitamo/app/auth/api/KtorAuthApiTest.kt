package eu.vitamo.app.auth.api

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.SessionResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.serialization.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import kotlinx.coroutines.runBlocking

class KtorAuthApiTest {
    @Test
    fun login_doesNotSendAuthorizationHeader() = runBlocking {
        var capturedRequest: HttpRequestData? = null
        val client = HttpClient(
            MockEngine { request ->
                capturedRequest = request
                respond(
                    content = AppJson.encodeToString(
                        LoginResponse.serializer(),
                        LoginResponse(user = fakeUser("login@example.com")),
                    ),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        ) {
            install(ContentNegotiation) {
                json(AppJson)
            }
        }
        val api = KtorAuthApi(client, AuthApiConfig(baseUrl = "https://example.com"))

        val result = api.login(LoginRequest(email = "login@example.com", password = "secret"))

        assertTrue(result is ApiResult.Success)
        assertEquals(null, capturedRequest?.headers?.get(HttpHeaders.Authorization))
    }

    @Test
    fun session_returnsUserOnOk() = runBlocking {
        val expectedUser = fakeUser("session@example.com")
        val client = HttpClient(
            MockEngine {
                respond(
                    content = AppJson.encodeToString(SessionResponse.serializer(), SessionResponse(user = expectedUser)),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            },
        ) {
            install(ContentNegotiation) {
                json(AppJson)
            }
        }
        val api = KtorAuthApi(client, AuthApiConfig(baseUrl = "https://example.com"))

        val session = api.session()

        assertTrue(session is ApiResult.Success)
        assertEquals(expectedUser.email, (session as ApiResult.Success).data.user.email)
    }

    private fun fakeUser(email: String): AuthenticatedUser {
        return AuthenticatedUser(
            id = Uuid.parse("123e4567-e89b-12d3-a456-426614174099"),
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
}
