package eu.vitamo.app.ui.auth.password_recovery.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged -> {
                _state.update {
                    it.copy(
                        email = event.value,
                        errorMessage = null,
                        successMessage = null)
                }
            }
            ForgotPasswordEvent.Submit -> { submit() }
            ForgotPasswordEvent.ClearMessage -> {
                _state.update {
                    it.copy(
                        errorMessage = null,
                        successMessage = null) }
            }
        }
    }

    private fun submit() {
        val email = state.value.email.trim()
        if (email.isBlank()) {
            _state.update {
                it.copy(errorMessage = "Vul je e-mailadres in.") }
            return

        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null)
            }

            when (val result = authRepository.forgotPassword(email)) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = result.data.message)
                    } }
                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false,
                            errorMessage = result.error.message ?: "Er is iets misgegaan. Probeer het opnieuw.")
                    }
                }
            }
        }
    }
}