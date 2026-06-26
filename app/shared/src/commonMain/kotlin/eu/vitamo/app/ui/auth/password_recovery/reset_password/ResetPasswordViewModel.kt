package eu.vitamo.app.ui.auth.password_recovery.reset_password

import eu.vitamo.app.api.result.ApiResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state.asStateFlow()

    fun onEvent(event: ResetPasswordEvent) {
        when (event) {
            is ResetPasswordEvent.TokenLoaded -> {
                _state.update {
                    it.copy(
                        token = event.token,
                        errorMessage = null,
                    )
                }
            }
            is ResetPasswordEvent.EmailChanged -> {
                _state.update {
                    it.copy(
                        email = event.value,
                        errorMessage = null,
                        successMessage = null,
                    )
                }
            }

            is ResetPasswordEvent.PasswordChanged -> {
                _state.update {
                    it.copy(
                        password = event.value,
                        errorMessage = null,
                        successMessage = null,
                    )
                }
            }

            is ResetPasswordEvent.RepeatPasswordChanged -> {
                _state.update {
                    it.copy(
                        repeatPassword = event.value,
                        errorMessage = null,
                        successMessage = null,
                    )
                }
            }

            ResetPasswordEvent.Submit -> {
                submit()
            }

            ResetPasswordEvent.ClearMessage -> {
                _state.update {
                    it.copy(
                        errorMessage = null,
                        successMessage = null,
                    )
                }
            }
        }
    }

    private fun submit() {
        val currentState = state.value
        val token = currentState.token.trim()
        val email = currentState.email.trim().lowercase()
        val password = currentState.password
        val repeatPassword = currentState.repeatPassword

        when {
            token.isBlank() -> {
                _state.update {
                    it.copy(errorMessage = "Reset-token ontbreekt.")
                }
                return
            }

            email.isBlank() -> {
                _state.update {
                    it.copy(errorMessage = "Vul je e-mailadres in.")
                }
                return
            }

            password.isBlank() -> {
                _state.update {
                    it.copy(errorMessage = "Vul een nieuw wachtwoord in.")
                }
                return
            }

            password.length < 8 -> {
                _state.update {
                    it.copy(errorMessage = "Wachtwoord moet minimaal 8 tekens bevatten.")
                }
                return
            }

            repeatPassword != password -> {
                _state.update {
                    it.copy(errorMessage = "Wachtwoorden komen niet overeen.")
                }
                return
            }
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            when (val result = authRepository.resetPassword(token, email, password)) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = result.data.message,
                            resetCompleted = result.data.passwordChanged,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.message ?:
                                "Er is iets misgegaan."
                            ,
                        )
                    }
                }
            }
        }
    }
}