package eu.vitamo.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.storage.AppPreferencesStorage
import eu.vitamo.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesStorage: AppPreferencesStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(),
    )

    val state: StateFlow<SettingsState> =
        _state.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesStorage.themeMode.collect { theme ->
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        themeMode = ThemeMode.fromStored(theme)
                    )
                }
            }
        }
    }

    fun setThemeMode(
        mode: ThemeMode,
    ) {
        savePreference {
            preferencesStorage.setThemeMode(
                mode = ThemeMode.toStored(mode),
            )
        }
    }

    fun clearDailyQuestionPostpone() {
        savePreference {
            preferencesStorage
                .setDailyQuestionPostponeUntilIso(
                    isoInstant = null,
                )
        }
    }

    fun clearError() {
        _state.update { currentState ->
            currentState.copy(
                errorMessage = null,
            )
        }
    }

    private fun savePreference(
        operation: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    isSaving = true,
                    errorMessage = null,
                )
            }

            try {
                operation()
            } catch (cause: Throwable) {
                _state.update { currentState ->
                    currentState.copy(
                        errorMessage = cause.message
                            ?: "De instelling kon niet worden opgeslagen.",
                    )
                }
            } finally {
                _state.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                    )
                }
            }
        }
    }
}