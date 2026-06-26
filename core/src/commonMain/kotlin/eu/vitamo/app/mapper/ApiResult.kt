package eu.vitamo.app.mapper

import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult

inline fun <ApiModel, DomainModel> ApiResult<ApiModel>.toRepositoryResult(
    mapper: (ApiModel) -> DomainModel,
): RepositoryResult<DomainModel> {
    return when (this) {
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
}

fun ApiError.toRepositoryError(): RepositoryError.Api {
    return RepositoryError.Api(
        code = code,
        message = message,
        status = status,
    )
}