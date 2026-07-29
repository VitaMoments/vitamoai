package eu.vitamo.app.ui.error.unavailable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnavailableViewModel(
    private val authSessionCoordinator: AuthSessionCoordinator,
) : ViewModel() {
    private val _isRetrying = MutableStateFlow(false)
    val isRetrying: StateFlow<Boolean> = _isRetrying.asStateFlow()

    fun retry() {
        if (_isRetrying.value) {
            return
        }

        viewModelScope.launch {
            _isRetrying.value = true

            try {
                authSessionCoordinator.bootstrap()
            } finally {
                _isRetrying.value = false
            }
        }
    }
}