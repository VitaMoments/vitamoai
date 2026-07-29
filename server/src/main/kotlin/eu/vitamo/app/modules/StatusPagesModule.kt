package eu.vitamo.app.modules

import eu.vitamo.app.api.contracts.common.ApiErrorCode
import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ErrorCode
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPagesModule")

fun Application.configureStatusPages() {
    if (pluginOrNull(StatusPages) != null) {
        return
    }

    install(StatusPages) {
        exception<SerializationException> { call, cause ->
            logger.warn(
                "Failed to deserialize request body: {}",
                cause.message,
            )

            call.respondApiError(
                status = HttpStatusCode.BadRequest,
                code = ApiErrorCode.BAD_REQUEST.code,
                message = "Invalid JSON",
            )
        }

        exception<JsonConvertException> { call, cause ->
            logger.warn(
                "Failed to convert request body: {}",
                cause.message,
            )

            call.respondApiError(
                status = HttpStatusCode.BadRequest,
                code = ApiErrorCode.BAD_REQUEST.code,
                message = "Invalid JSON",
            )
        }

        exception<BadRequestException> { call, cause ->
            logger.warn(
                "Bad request payload: {}",
                cause.message,
            )

            call.respondApiError(
                status = HttpStatusCode.BadRequest,
                code = ApiErrorCode.BAD_REQUEST.code,
                message = "Invalid request body",
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.warn(
                "Invalid request data: {}",
                cause.message,
            )

            call.respondApiError(
                status = HttpStatusCode.BadRequest,
                code = ApiErrorCode.BAD_REQUEST.code,
                message = cause.message ?: "Invalid request data",
            )
        }

        exception<AuthException> { call, cause ->
            logger.warn(
                "Authentication request failed: code={}, status={}",
                cause.code,
                cause.status.value,
            )

            call.respondApiError(
                status = cause.status,
                code = cause.code,
                message = cause.message,
            )
        }

        exception<ApiException> { call, cause ->
            logger.warn(
                "API request failed: code={}, status={}",
                cause.code,
                cause.status.value,
            )

            call.respondApiError(
                status = cause.status,
                code = cause.code,
                message = cause.message,
            )
        }

        exception<Throwable> { call, cause ->
            logger.error(
                "Unhandled server error",
                cause,
            )

            call.respondApiError(
                status = HttpStatusCode.InternalServerError,
                code = ApiErrorCode.INTERNAL.code,
                message = "Internal server error",
            )
        }
    }
}

private suspend fun ApplicationCall.respondApiError(
    status: HttpStatusCode,
    code: ErrorCode,
    message: String,
) {
    respond(
        status = status,
        message = ApiError(
            code = code,
            message = message,
            status = status.value,
        ),
    )
}