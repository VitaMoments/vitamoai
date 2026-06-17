package eu.vitamo.app.modules

import eu.vitamo.app.config.JWTConfig
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respondText
import org.koin.core.context.GlobalContext

fun Application.configureSecurity() {
    if (pluginOrNull(Authentication) != null) {
        return
    }

    val koin = GlobalContext.get()
    val jwtConfig = koin.get<JWTConfig>()

    install(Authentication) {
        jwt("cookie-jwt-authentication") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier)
            authHeader { call ->
                val token = call.request.cookies["access_token"] ?: return@authHeader null
                HttpAuthHeader.Single("Bearer", token)
            }
            validate { credential ->
                credential.payload
                    .getClaim(JWTConfig.USER_ID_CLAIM)
                    .asString()
                    ?.takeIf(String::isNotBlank)
                    ?.let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ ->
                call.respondText(
                    text = "Token is invalid or expired",
                    status = HttpStatusCode.Unauthorized,
                )
            }
        }
    }
}
