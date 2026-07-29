package eu.vitamo.app.network.auth

import eu.vitamo.app.api.result.ApiFailure

sealed interface AuthStatus {
    data object Loading : AuthStatus
    data object Authenticated : AuthStatus
    data object Unauthenticated : AuthStatus

    data class Unavailable(
        val failure: ApiFailure,
    ) : AuthStatus
}
