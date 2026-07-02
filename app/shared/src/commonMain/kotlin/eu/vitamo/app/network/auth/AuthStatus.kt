package eu.vitamo.app.network.auth

sealed interface AuthStatus {
    data object Loading : AuthStatus
    data object Authenticated : AuthStatus
    data object Unauthenticated : AuthStatus
}
