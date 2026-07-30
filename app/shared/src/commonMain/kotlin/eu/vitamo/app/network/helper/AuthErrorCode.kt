package eu.vitamo.app.network.helper

import eu.vitamo.app.api.contracts.errorcodes.AuthErrorCode
import eu.vitamo.app.repository.RepositoryError

fun RepositoryError.authErrorCodeOrNull(): AuthErrorCode? {
    val apiError = this as? RepositoryError.Api
        ?: return null

    return AuthErrorCode.from(apiError.code)
}