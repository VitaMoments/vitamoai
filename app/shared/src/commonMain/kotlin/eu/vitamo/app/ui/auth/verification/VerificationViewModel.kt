package eu.vitamo.app.ui.auth.verification


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.auth.AuthErrorCode
import eu.vitamo.app.api.contracts.common.BaseErrorCode.BAD_REQUEST_CODE
import eu.vitamo.app.auth.repository.AuthRepository
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
                        code = result.error.code!!,
                        message = result.error.message!!,
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
                        code = result.error.code!!,
                        message = result.error.message!!,
                    )
                }
            }
        }
    }

    private fun handleVerifyError(
        code: String,
        message: String,
    ) {
        when (code) {
            AuthErrorCode.INVALID_VERIFICATION_CODE -> {
                _state.update {
                    it.copy(
                        codeError = "De verificatiecode is onjuist.",
                    )
                }
            }

            AuthErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED_CODE -> {
                _state.update {
                    it.copy(
                        generalError = "Je hebt te vaak een verkeerde code ingevoerd. Vraag een nieuwe code aan.",
                    )
                }
            }

            AuthErrorCode.EMAIL_VERIFICATION_FAILED_CODE -> {
                _state.update {
                    it.copy(
                        generalError = "Het verifiëren van je e-mailadres is mislukt.",
                    )
                }
            }

            BAD_REQUEST_CODE -> {
                _state.update {
                    it.copy(
                        generalError = message,
                    )
                }
            }

            else -> {
                _state.update {
                    it.copy(
                        generalError = message.ifBlank {
                            "Verificatie is mislukt. Probeer het opnieuw."
                        },
                    )
                }
            }
        }
    }
}