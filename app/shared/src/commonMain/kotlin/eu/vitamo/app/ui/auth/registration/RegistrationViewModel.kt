package eu.vitamo.app.ui.auth.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.auth.AuthErrorCode
import eu.vitamo.app.api.contracts.common.BaseErrorCode
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    private val _events = Channel<RegistrationEvent>()
    val events = _events.receiveAsFlow()

    fun onEmailChanged(value: String) {
        _state.update {
            it.copy(
                email = value,
                emailError = null,
                generalError = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _state.update {
            it.copy(
                password = value,
                passwordError = null,
                confirmPasswordError = null,
                generalError = null,
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _state.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = null,
                generalError = null,
            )
        }
    }

    fun onDisplayNameChanged(value: String) {
        _state.update {
            it.copy(
                displayName = value,
                displayNameError = null,
                generalError = null,
            )
        }
    }

    fun onFirstNameChanged(value: String) {
        _state.update {
            it.copy(firstName = value)
        }
    }

    fun onLastNameChanged(value: String) {
        _state.update {
            it.copy(lastName = value)
        }
    }

    fun onAliasChanged(value: String) {
        _state.update {
            it.copy(alias = value)
        }
    }

    fun onBirthDateChanged(value: String) {
        _state.update {
            it.copy(
                birthDate = value,
                birthDateError = null,
            )
        }
    }

    fun register() {
        val currentState = state.value

        val email = currentState.email.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword
        val displayName = currentState.displayName.trim()

        val hasValidationError = validateInput(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            displayName = displayName,
        )

        if (hasValidationError) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    displayNameError = null,
                    birthDateError = null,
                    generalError = null,
                )
            }

            when (
                val result = authRepository.register(
                    username = displayName,
                    email = email,
                    password = password,
                )
            ) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(isLoading = false)
                    }

                    _events.send(
                        RegistrationEvent.RegistrationSuccess(
                            email = email,
                        )
                    )
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false)
                    }

                    handleRegisterError(
                        code = result.error.code!!,
                        message = result.error.message!!,
                    )
                }
            }
        }
    }

    private fun validateInput(
        email: String,
        password: String,
        confirmPassword: String,
        displayName: String,
    ): Boolean {
        var hasError = false

        if (displayName.isBlank()) {
            hasError = true
            _state.update {
                it.copy(displayNameError = "Vul een gebruikersnaam in.")
            }
        }

        if (email.isBlank()) {
            hasError = true
            _state.update {
                it.copy(emailError = "Vul je e-mailadres in.")
            }
        } else if (!email.contains("@")) {
            hasError = true
            _state.update {
                it.copy(emailError = "Vul een geldig e-mailadres in.")
            }
        }

        if (password.isBlank()) {
            hasError = true
            _state.update {
                it.copy(passwordError = "Vul een wachtwoord in.")
            }
        } else if (password.length < 8) {
            hasError = true
            _state.update {
                it.copy(passwordError = "Je wachtwoord moet minimaal 8 tekens zijn.")
            }
        }

        if (confirmPassword.isBlank()) {
            hasError = true
            _state.update {
                it.copy(confirmPasswordError = "Herhaal je wachtwoord.")
            }
        } else if (password != confirmPassword) {
            hasError = true
            _state.update {
                it.copy(confirmPasswordError = "De wachtwoorden komen niet overeen.")
            }
        }

        return hasError
    }

    private fun handleRegisterError(
        code: String,
        message: String,
    ) {
        when (code) {
            AuthErrorCode.EMAIL_ALREADY_EXISTS -> {
                _state.update {
                    it.copy(emailError = "Dit e-mailadres is al in gebruik.")
                }
            }

            AuthErrorCode.INVALID_EMAIL -> {
                _state.update {
                    it.copy(emailError = "Vul een geldig e-mailadres in.")
                }
            }

            BaseErrorCode.BAD_REQUEST_CODE -> {
                _state.update {
                    it.copy(generalError = message)
                }
            }

            else -> {
                _state.update {
                    it.copy(
                        generalError = message.ifBlank {
                            "Registreren is mislukt. Probeer het opnieuw."
                        }
                    )
                }
            }
        }
    }
}