package eu.vitamo.app.auth.api

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.serialization.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject

class KtorAuthApi(
    private val client: HttpClient,
    private val config: AuthApiConfig,
) : AuthApi {
    override suspend fun register(request: RegisterRequest): RegisterResponse {
        return client.post("${config.baseUrl}/auth/register") {
            headers.append(HttpHeaders.ContentType, "application/json")
            setBody(request)
        }.body()
    }

    override suspend fun login(request: LoginRequest): LoginResponse {
        return client.post("${config.baseUrl}/auth/login") {
            headers.append(HttpHeaders.ContentType, "application/json")
            setBody(request)
        }.body()
    }

    override suspend fun verifyEmail(request: VerifyEmailRequest): VerifyEmailResponse {
        return client.post("${config.baseUrl}/auth/verify-email") {
            headers.append(HttpHeaders.ContentType, "application/json")
            setBody(request)
        }.body()
    }

    override suspend fun resendEmailVerification(
        request: ResendEmailVerificationRequest,
    ): ResendEmailVerificationResponse {
        return client.post("${config.baseUrl}/auth/resend-email-verification") {
            headers.append(HttpHeaders.ContentType, "application/json")
            setBody(request)
        }.body()
    }

    override suspend fun session(): AuthenticatedUser? {
        val response = client.get("${config.baseUrl}/auth/session")
        if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
            return null
        }
        if (response.status != HttpStatusCode.OK) {
            return null
        }

        val payload = response.bodyAsText()
        val root = AppJson.parseToJsonElement(payload)
        return when (root) {
            is JsonObject -> {
                val hasUser = "user" in root
                if (hasUser) {
                    AppJson.decodeFromString(LoginResponse.serializer(), payload).user
                } else {
                    AppJson.decodeFromString(AuthenticatedUser.serializer(), payload)
                }
            }
            else -> null
        }
    }

    override suspend fun logout() {
        client.post("${config.baseUrl}/auth/logout")
    }
}
