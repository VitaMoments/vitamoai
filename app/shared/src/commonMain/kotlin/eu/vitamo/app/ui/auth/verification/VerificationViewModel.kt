package eu.vitamo.app.ui.auth.verification


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.errorcodes.AuthErrorCode
import eu.vitamo.app.api.contracts.errorcodes.ApiErrorCode
import eu.vitamo.app.features.auth.repository.AuthRepository
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

class VerificationViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationState())
    val state: StateFlow<VerificationState> = _state.asStateFlow()

    private val _events = Channel<VerifyEmailEvent>()
    val events = _events.receiveAsFlow()

    fun onCodeChanged(value: String) {
        val digitsOnly = value
            .filter { it.isDigit() }
            .take(6)

        _state.update {
            it.copy(
                code = digitsOnly,
                codeError = null,
                generalError = null,
                message = null,
            )
        }
    }

    fun verifyEmail(email: String) {
        val code = state.value.code.trim()

        if (code.length != 6) {
            _state.update {
                it.copy(
                    codeError = "Vul de 6-cijferige verificatiecode in.",
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    codeError = null,
                    generalError = null,
                    message = null,
                )
            }

            when (
                val result = authRepository.verifyEmail(
                    email = email,
                    code = code,
                )
            ) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(isLoading = false)
                    }

                    _events.send(VerifyEmailEvent.VerificationSuccess)
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false)
                    }

                    handleVerifyError(
                        error = result.error
                    )
                }
            }
        }
    }

    fun resendCode(email: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isResending = true,
                    generalError = null,
                    codeError = null,
                    message = null,
                )
            }

            when (
                val result = authRepository.resendEmailVerification(
                    email = email,
                )
            ) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(
                            isResending = false,
                            message = "Er is een nieuwe code verstuurd.",
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(isResending = false)
                    }

                    handleVerifyError(
                        error = result.error
                    )
                }
            }
        }
    }

    private fun handleVerifyError(
        error: RepositoryError,
    ) {
        when (error.authErrorCodeOrNull()) {
            AuthErrorCode.INVALID_VERIFICATION_CODE -> {
                _state.update {
                    it.copy(
                        codeError = "De verificatiecode is onjuist.",
                    )
                }
            }

            AuthErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED -> {
                _state.update {
                    it.copy(
                        generalError = "Je hebt te vaak een verkeerde code ingevoerd. Vraag een nieuwe code aan.",
                    )
                }
            }

            AuthErrorCode.EMAIL_VERIFICATION_FAILED -> {
                _state.update {
                    it.copy(
                        generalError = "Het verifiëren van je e-mailadres is mislukt.",
                    )
                }
            }

            else -> {
                handleGeneralVerifyError(error)
            }
        }
    }
    private fun handleGeneralVerifyError(
        error: RepositoryError,
    ) {
        val message = when (error) {
            is RepositoryError.Network -> {
                "Geen internetverbinding. Controleer je verbinding en probeer opnieuw."
            }

            is RepositoryError.Serialization -> {
                "Het antwoord van de server kon niet worden verwerkt."
            }

            is RepositoryError.Api -> {
                when (ApiErrorCode.from(error.code)) {
                    ApiErrorCode.BAD_REQUEST,
                    ApiErrorCode.VALIDATION_ERROR,
                        -> {
                        error.message.ifBlank {
                            "Controleer de ingevulde verificatiecode."
                        }
                    }

                    ApiErrorCode.RATE_LIMIT -> {
                        "Je hebt te veel pogingen gedaan. Probeer het later opnieuw."
                    }

                    else -> {
                        error.message.ifBlank {
                            "Verificatie is mislukt. Probeer het opnieuw."
                        }
                    }
                }
            }

            is RepositoryError.RequestLimitReached -> {
                "Je hebt te veel pogingen gedaan. Probeer het later opnieuw."
            }

            is RepositoryError.Validation,
            is RepositoryError.BadRequest,
                -> {
                error.message.ifBlank {
                    "Controleer de ingevulde verificatiecode."
                }
            }

            is RepositoryError.Internal,
            is RepositoryError.Unknown,
                -> {
                "Verificatie is mislukt. Probeer het later opnieuw."
            }

            else -> {
                error.message.ifBlank {
                    "Verificatie is mislukt. Probeer het opnieuw."
                }
            }
        }

        _state.update {
            it.copy(
                generalError = message,
            )
        }
    }
}