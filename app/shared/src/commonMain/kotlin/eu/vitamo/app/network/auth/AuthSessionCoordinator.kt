package eu.vitamo.app.network.auth

import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.auth.api.AuthApiConfig
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.network.AuthCookieStorage
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSessionCoordinator(
    private val authApi: AuthApi,
    private val authApiConfig: AuthApiConfig,
    private val cookieStorage: AuthCookieStorage,
) {
    private val _state = MutableStateFlow<AuthStatus>(AuthStatus.Loading)
    val state: StateFlow<AuthStatus> = _state.asStateFlow()

    suspend fun bootstrap() {
        if (!hasAuthCookies()) {
            _state.value = AuthStatus.Unauthenticated
            return
        }

        when (val sessionResult = authApi.session()) {
            is ApiResult.Success -> {
                _state.value = AuthStatus.Authenticated
            }

            is ApiResult.Error -> {
                if (isUnauthorized(sessionResult.error.status) && refreshSession()) {
                    return
                }
                signOut()
            }
        }
    }

    suspend fun refreshSession(): Boolean {
        return when (val refreshResult = authApi.refreshSession()) {
            is ApiResult.Success -> {
                _state.value = AuthStatus.Authenticated
                true
            }

            is ApiResult.Error -> {
                if (isUnauthorized(refreshResult.error.status)) {
                    signOut()
                }
                false
            }
        }
    }

    suspend fun markAuthenticated() {
        _state.value = AuthStatus.Authenticated
    }

    suspend fun signOut() {
        cookieStorage.clearAuthCookies()
        _state.value = AuthStatus.Unauthenticated
    }

    suspend fun hasAuthCookies(): Boolean {
        return cookieStorage.get(Url(authApiConfig.baseUrl)).any { cookie ->
            cookie.name == "access_token" || cookie.name == "refresh_token"
        }
    }

    private fun isUnauthorized(status: Int?): Boolean {
        return status == 401
    }
}
