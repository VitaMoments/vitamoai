package eu.vitamo.app.network.auth

import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.result.ApiFailure

sealed interface AuthStatus {

    data object Loading : AuthStatus

    data class Authenticated(
        val user: AuthenticatedUser,
    ) : AuthStatus

    data object Unauthenticated : AuthStatus

    data class Unavailable(
        val failure: ApiFailure,
    ) : AuthStatus
}
