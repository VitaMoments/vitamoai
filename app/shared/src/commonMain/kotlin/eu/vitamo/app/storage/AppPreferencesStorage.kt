package eu.vitamo.app.storage

import kotlinx.coroutines.flow.Flow

interface AppPreferencesStorage {
    val lastRoute: Flow<String>
    val themeMode: Flow<String?>
    val dailyQuestionPostponeUntilIso: Flow<String?>
    suspend fun setLastRoute(route: String)
    suspend fun setThemeMode(mode: String?)
    suspend fun setDailyQuestionPostponeUntilIso(isoInstant: String?)
}