package eu.vitamo.app.infrastructure.network.helpers

import eu.vitamo.app.api.contracts.auth.AuthErrorCode
import eu.vitamo.app.config.JWTConfig
import eu.vitamo.app.features.auth.model.AuthException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import kotlin.uuid.Uuid


val ApplicationCall.userId: Uuid?
    get() = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(JWTConfig.USER_ID_CLAIM)
        ?.asString()
        ?.let {
            runCatching {  Uuid.parse(it) }.getOrNull()
        }

suspend fun ApplicationCall.requireUserId() : Uuid = userId ?: throw AuthException.InvalidAccessTokenException()