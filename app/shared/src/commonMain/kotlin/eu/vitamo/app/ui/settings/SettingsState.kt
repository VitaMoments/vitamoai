package eu.vitamo.app.ui.settings

import eu.vitamo.app.ui.theme.ThemeMode

data class SettingsState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val errorMessage: String? = null,
)