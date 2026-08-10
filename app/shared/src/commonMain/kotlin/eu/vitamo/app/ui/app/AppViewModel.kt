package eu.vitamo.app.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.storage.AppPreferencesStorage
import eu.vitamo.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class AppViewModel(
    private val preferencesStorage:
    AppPreferencesStorage,
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            AppUiState(),
        )

    val state: StateFlow<AppUiState> =
        _state.asStateFlow()

    init {
        observeThemeMode()
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            preferencesStorage
                .themeMode
                .collect { storedMode ->
                    _state.update { currentState ->
                        currentState.copy(
                            themeMode =
                                ThemeMode.fromStored(
                                    storedMode,
                                ),
                        )
                    }
                }
        }
    }

    fun setThemeMode(
        mode: ThemeMode,
    ) {
        viewModelScope.launch {
            preferencesStorage.setThemeMode(
                mode = ThemeMode.toStored(mode),
            )
        }
    }

    fun setLastRoute(
        route: String,
    ) {
        viewModelScope.launch {
            preferencesStorage.setLastRoute(
                route = route,
            )
        }
    }

    fun postponeDailyQuestionUntil(
        isoInstant: String?,
    ) {
        viewModelScope.launch {
            preferencesStorage
                .setDailyQuestionPostponeUntilIso(
                    isoInstant = isoInstant,
                )
        }
    }
}