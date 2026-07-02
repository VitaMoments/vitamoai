package eu.vitamo.app.storage

import kotlinx.coroutines.flow.Flow

enum class AppThemePreference {
    System,
    Light,
    Dark,
}

interface AppPreferencesStorage {
    val theme: Flow<AppThemePreference>
    val onboardingCompleted: Flow<Boolean>

    fun currentTheme(): AppThemePreference
    fun currentOnboardingCompleted(): Boolean

    suspend fun setTheme(theme: AppThemePreference)

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun clear()
}