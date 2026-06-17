package eu.vitamo.app.modules

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import eu.vitamo.app.api.contracts.auth.ApiErrorResponse
import eu.vitamo.app.features.auth.model.AuthException
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPagesModule")

fun Application.configureStatusPages() {
    if (pluginOrNull(StatusPages) != null) {
        return
    }

    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            logger.warn("Failed to deserialize request body: {}", cause.message)
            call.respondText(
                text = "Invalid JSON",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }

        exception<JsonConvertException> { call, cause ->
            logger.warn("Failed to convert request body: {}", cause.message)
            call.respondText(
                text = "Invalid JSON",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }

        exception<BadRequestException> { call, cause ->
            logger.warn("Bad request payload: {}", cause.message)
            call.respondText(
                text = "Invalid JSON",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Invalid request data: {}", cause.message)
            call.respondText(
                text = "Invalid request data",
                contentType = ContentType.Text.Plain,
                status = HttpStatusCode.BadRequest,
            )
        }

        exception<AuthException> { call, cause ->
            logger.warn("Auth request failed: {}", cause.code)
            call.respond(
                status = cause.status,
                message = ApiErrorResponse(
                    code = cause.code,
                    message = cause.message,
                ),
            )
        }
    }
}

