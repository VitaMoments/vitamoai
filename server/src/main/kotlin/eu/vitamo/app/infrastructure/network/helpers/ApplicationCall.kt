package eu.vitamo.app.infrastructure.network.helpers

import eu.vitamo.app.api.contracts.errorcodes.ApiErrorCode
import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiFieldError
import eu.vitamo.app.api.result.ErrorCode
import eu.vitamo.app.config.JWTConfig
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.infrastructure.network.models.PaginationParameters
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import kotlin.uuid.Uuid

val ApplicationCall.userId: Uuid?
    get() = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim(JWTConfig.USER_ID_CLAIM)
        ?.asString()
        ?.let {
            runCatching {  Uuid.parse(it) }.getOrNull()
        }

fun ApplicationCall.requireUserId() : Uuid = userId ?: throw AuthException.InvalidAccessToken()

fun ApplicationCall.getQueryParameter(param: String) : String? = this.request.queryParameters[param]
fun ApplicationCall.requireQueryParameter(param: String) : String = this
    .getQueryParameter(param)
    ?.trim()
    ?.takeIf(String::isNotEmpty)
    ?: throw ApiException.BadRequest("Parameter not provided: $param")

fun ApplicationCall.getPaginationParameters(
    defaultLimit: Int = 20,
    maxLimit: Int = 100,
): PaginationParameters {
    val limit = request.queryParameters["limit"]
        ?.toIntOrNull()
        ?.coerceIn(1, maxLimit)
        ?: defaultLimit

    val offset = request.queryParameters["offset"]
        ?.toIntOrNull()
        ?.coerceAtLeast(0)
        ?: 0

    return PaginationParameters(
        limit = limit,
        offset = offset,
    )
}

//suspend fun ApplicationCall.respondError(err: RepositoryError) {
//    respond(err.message, err.toApiError())
//}

//suspend inline fun <reified T> ApplicationCall.respondRepository(
//    rr: RepositoryResult<T>,
//    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
//    messageType: TypeInfo = typeInfo<T>()
//) {
//    when (rr) {
//        is RepositoryResult.Success -> {
//            respond(
//                status = successStatusCode,
//                message = rr.body,
//                messageType = messageType
//            )
//        }
//        is RepositoryResult.Error -> respondError(rr.error)
//    }
//}

suspend inline fun <reified T : Any> ApplicationCall.handleResult(
    result: RepositoryResult<T>,
    successStatusCode: HttpStatusCode = HttpStatusCode.OK,
    noinline onSuccess: (suspend ApplicationCall.(T) -> Unit)? = null,
    noinline onError: (suspend ApplicationCall.(RepositoryError) -> Unit)? = null,
) {
    when (result) {
        is RepositoryResult.Success -> {
            if (onSuccess != null) {
                onSuccess(result.data)
            } else {
                respond(
                    status = successStatusCode,
                    message = result.data,
                )
            }
        }

        is RepositoryResult.Error -> {
            if (onError != null) {
                onError(result.error)
            } else {
                respondRepositoryError(result.error)
            }
        }
    }
}

suspend fun ApplicationCall.respondRepositoryError(
    error: RepositoryError,
) {
    respondApiError(
        status = error.statusCode(),
        code = error.errorCode(),
        message = error.message,
        fieldErrors = error.apiFieldErrors(),
        traceId = (error as? RepositoryError.Api)?.traceId,
    )
}

suspend fun ApplicationCall.respondApiError(
    status: HttpStatusCode,
    code: ErrorCode,
    message: String,
    fieldErrors: List<ApiFieldError> = emptyList(),
    traceId: String? = null,
) {
    respond(
        status = status,
        message = ApiError(
            code = code,
            message = message,
            fieldErrors = fieldErrors,
            traceId = traceId,
            status = status.value,
        ),
    )
}

suspend fun ApplicationCall.respondApiError(
    status: HttpStatusCode,
    code: ApiErrorCode,
    message: String,
    fieldErrors: List<ApiFieldError> = emptyList(),
    traceId: String? = null,
) {
    respondApiError(
        status = status,
        code = code.code,
        message = message,
        fieldErrors = fieldErrors,
        traceId = traceId,
    )
}
