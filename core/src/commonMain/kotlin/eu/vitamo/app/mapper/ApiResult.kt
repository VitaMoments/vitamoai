package eu.vitamo.app.mapper

import eu.vitamo.app.api.result.ApiFailure
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.repository.FieldError
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult

fun <T> ApiResult<T>.toRepositoryResult(): RepositoryResult<T> {
    return toRepositoryResult { value ->
        value
    }
}

inline fun <ApiModel, DomainModel> ApiResult<ApiModel>.toRepositoryResult(
    mapper: (ApiModel) -> DomainModel,
): RepositoryResult<DomainModel> =
    when (this) {
        is ApiResult.Success -> {
            RepositoryResult.Success(
                data = mapper(data),
            )
        }

        is ApiResult.Error -> {
            RepositoryResult.Error(
                error = error.toRepositoryError(),
            )
        }
    }

fun ApiFailure.toRepositoryError(): RepositoryError =
    when (this) {
        is ApiFailure.Server -> {
            RepositoryError.Api(
                code = apiError.code,
                message = apiError.message,
                fieldErrors = apiError.fieldErrors.map { fieldError ->
                    FieldError(
                        field = fieldError.field,
                        message = fieldError.message,
                    )
                },
                traceId = apiError.traceId,
                status = apiError.status,
            )
        }

        is ApiFailure.Network -> {
            RepositoryError.Network(
                message = message,
                cause = cause,
            )
        }

        is ApiFailure.Serialization -> {
            RepositoryError.Serialization(
                message = message,
                cause = cause,
            )
        }

        is ApiFailure.Unknown -> {
            RepositoryError.Unknown(
                message = message,
                cause = cause,
            )
        }
    }