package eu.vitamo.app.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppPreferencesStorageImpl(
    private val settings: Settings,
) : AppPreferencesStorage {
    private val writeMutex = Mutex()

    private val _lastRoute = MutableStateFlow(
        settings.getString(
            key = KEY_LAST_ROUTE,
            defaultValue = DEFAULT_LAST_ROUTE,
        ),
    )

    override val lastRoute: Flow<String> =
        _lastRoute.asStateFlow()

    private val _themeMode = MutableStateFlow(
        settings.getStringOrNull(
            key = KEY_THEME_MODE,
        ),
    )

    override val themeMode: Flow<String?> =
        _themeMode.asStateFlow()

    private val _dailyQuestionPostponeUntilIso =
        MutableStateFlow(
            settings.getStringOrNull(
                key = KEY_DAILY_QUESTION_POSTPONE_UNTIL,
            ),
        )

    override val dailyQuestionPostponeUntilIso:
            Flow<String?> =
        _dailyQuestionPostponeUntilIso.asStateFlow()

    override suspend fun setLastRoute(
        route: String,
    ) {
        writeMutex.withLock {
            val normalizedRoute = route
                .trim()
                .takeIf(String::isNotEmpty)
                ?: DEFAULT_LAST_ROUTE

            settings.putString(
                key = KEY_LAST_ROUTE,
                value = normalizedRoute,
            )

            _lastRoute.value = normalizedRoute
        }
    }

    override suspend fun setThemeMode(
        mode: String?,
    ) {
        writeMutex.withLock {
            val normalizedMode = mode
                ?.trim()
                ?.lowercase()
                ?.takeIf { value ->
                    value in SUPPORTED_THEME_MODES
                }

            if (normalizedMode == null) {
                settings.remove(
                    key = KEY_THEME_MODE,
                )
            } else {
                settings.putString(
                    key = KEY_THEME_MODE,
                    value = normalizedMode,
                )
            }

            _themeMode.value = normalizedMode
        }
    }

    override suspend fun setDailyQuestionPostponeUntilIso(
        isoInstant: String?,
    ) {
        writeMutex.withLock {
            val normalizedInstant = isoInstant
                ?.trim()
                ?.takeIf(String::isNotEmpty)

            if (normalizedInstant == null) {
                settings.remove(
                    key =
                        KEY_DAILY_QUESTION_POSTPONE_UNTIL,
                )
            } else {
                settings.putString(
                    key =
                        KEY_DAILY_QUESTION_POSTPONE_UNTIL,
                    value = normalizedInstant,
                )
            }

            _dailyQuestionPostponeUntilIso.value =
                normalizedInstant
        }
    }

    private companion object {
        const val KEY_LAST_ROUTE =
            "app.last_route"

        const val KEY_THEME_MODE =
            "app.theme_mode"

        const val KEY_DAILY_QUESTION_POSTPONE_UNTIL =
            "app.daily_question_postpone_until_iso"

        const val DEFAULT_LAST_ROUTE =
            "home"

        val SUPPORTED_THEME_MODES = setOf(
            "light",
            "dark",
            "system",
        )
    }
}