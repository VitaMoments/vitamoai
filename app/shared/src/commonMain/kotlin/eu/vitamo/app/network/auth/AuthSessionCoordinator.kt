package eu.vitamo.app.network.auth

import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.features.auth.api.AuthApi
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.helper.isUnauthorized
import io.ktor.client.plugins.cookies.get
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSessionCoordinator(
    private val authApi: AuthApi,
    private val cookieStorage: AuthCookieStorage,
) {
    private val _state = MutableStateFlow<AuthStatus>(
        AuthStatus.Loading,
    )

    val state: StateFlow<AuthStatus> =
        _state.asStateFlow()

    suspend fun bootstrap() {
        _state.value = AuthStatus.Loading

        if (!hasAuthCookies()) {
            _state.value = AuthStatus.Unauthenticated
            return
        }

        when (val result = authApi.session()) {
            is ApiResult.Success -> {
                _state.value = AuthStatus.Authenticated
            }

            is ApiResult.Error -> {
                if (result.error.isUnauthorized()) {
                    refreshSession()
                } else {
                    _state.value = AuthStatus.Unavailable(
                        failure = result.error,
                    )
                }
            }
        }
    }

    suspend fun refreshSession(): Boolean {
        return when (val result = authApi.refreshSession()) {
            is ApiResult.Success -> {
                _state.value = AuthStatus.Authenticated
                true
            }

            is ApiResult.Error -> {
                if (result.error.isUnauthorized()) {
                    signOut()
                } else {
                    _state.value = AuthStatus.Unavailable(
                        failure = result.error,
                    )
                }

                false
            }
        }
    }

    fun markAuthenticated() {
        _state.value = AuthStatus.Authenticated
    }

    suspend fun signOut() {
        cookieStorage.clearAuthCookies()
        _state.value = AuthStatus.Unauthenticated
    }

    suspend fun getAccessCookie(): String? =
        cookieStorage
            .get(Url("auth/"))[ACCESS_TOKEN_COOKIE]?.value

    suspend fun getRefreshCookie(): String? =
        cookieStorage
            .get(Url("auth/"))[REFRESH_TOKEN_COOKIE]?.value

    suspend fun hasAuthCookies(): Boolean {
        return cookieStorage
            .get(Url("auth/"))
            .any { cookie ->
                cookie.name == ACCESS_TOKEN_COOKIE ||
                        cookie.name == REFRESH_TOKEN_COOKIE
            }
    }

    private companion object {
        const val ACCESS_TOKEN_COOKIE = "access_token"
        const val REFRESH_TOKEN_COOKIE = "refresh_token"
    }
}