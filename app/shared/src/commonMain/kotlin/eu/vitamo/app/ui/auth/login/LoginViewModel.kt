package eu.vitamo.app.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.auth.AuthErrorCode
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.network.helper.authErrorCodeOrNull
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onEmailAddressChanged(value: String) {
        _state.update {
            it.copy(
                emailAddress = value,
                emailAddressError = null,
                generalError = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _state.update {
            it.copy(
                password = value,
                passwordError = null,
                generalError = null,
            )
        }
    }

    fun login() {
        val currentState = state.value
        val emailAddress = currentState.emailAddress.trim()
        val password = currentState.password

        val hasValidationError = validateInput(
            emailAddress = emailAddress,
            password = password,
        )

        if (hasValidationError) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    emailAddressError = null,
                    passwordError = null,
                    generalError = null,
                )
            }

            when (
                val result = authRepository.login(
                    email = emailAddress, password = password
                )
            ) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(isLoading = false)
                    }

                    _events.send(LoginEvent.LoginSuccess)
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false)
                    }

                    handleLoginError(
                        error = result.error,
                        emailAddress = emailAddress,
                    )
                }
            }
        }
    }

    private fun validateInput(
        emailAddress: String,
        password: String,
    ): Boolean {
        var hasError = false

        if (emailAddress.isBlank()) {
            hasError = true
            _state.update {
                it.copy(emailAddressError = "Vul je e-mailadres in.")
            }
        } else if (!emailAddress.contains("@")) {
            hasError = true
            _state.update {
                it.copy(emailAddressError = "Vul een geldig e-mailadres in.")
            }
        }

        if (password.isBlank()) {
            hasError = true
            _state.update {
                it.copy(passwordError = "Vul je wachtwoord in.")
            }
        }

        return hasError
    }

    private suspend fun handleLoginError(
        error: RepositoryError,
        emailAddress: String,
    ) {
        when (error.authErrorCodeOrNull()) {
            AuthErrorCode.INVALID_CREDENTIALS -> {
                _state.update {
                    it.copy(
                        generalError =
                            "E-mailadres of wachtwoord is onjuist.",
                    )
                }
            }

            AuthErrorCode.EMAIL_NOT_VERIFIED -> {
                _events.send(
                    LoginEvent.EmailNotVerified(
                        emailAddress = emailAddress,
                    ),
                )
            }

            else -> {
                handleGeneralRepositoryError(error)
            }
        }
    }

    private fun handleGeneralRepositoryError(
        error: RepositoryError,
    ) {
        val message = when (error) {
            is RepositoryError.Network ->
                "Geen internetverbinding. Controleer je verbinding en probeer opnieuw."

            is RepositoryError.Serialization ->
                "Het antwoord van de server kon niet worden verwerkt."

            is RepositoryError.Api ->
                error.message

            is RepositoryError.RequestLimitReached ->
                "Je hebt te veel pogingen gedaan. Probeer het later opnieuw."

            is RepositoryError.Internal,
            is RepositoryError.Unknown,
                ->
                "Inloggen is mislukt. Probeer het later opnieuw."

            else ->
                error.message
        }

        _state.update {
            it.copy(
                generalError = message,
            )
        }
    }
}