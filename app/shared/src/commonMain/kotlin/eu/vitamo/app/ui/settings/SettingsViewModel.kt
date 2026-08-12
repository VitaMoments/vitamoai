package eu.vitamo.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.features.settings.permissions.SettingsPermissionItem
import eu.vitamo.app.infrastructure.notification.AppNotification
import eu.vitamo.app.infrastructure.notification.AppNotificationChannel
import eu.vitamo.app.infrastructure.notification.LocalNotificationManager
import eu.vitamo.app.infrastructure.permissions.AppPermission
import eu.vitamo.app.infrastructure.permissions.PermissionManager
import eu.vitamo.app.infrastructure.permissions.PermissionStatus
import eu.vitamo.app.infrastructure.permissions.RequiredAppPermissions
import eu.vitamo.app.infrastructure.storage.AppPreferencesStorage
import eu.vitamo.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesStorage: AppPreferencesStorage,
    private val permissionManager: PermissionManager,
    private val localNotificationManager: LocalNotificationManager,
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
                        themeMode = ThemeMode.fromStored(theme),
                    )
                }
            }
        }
    }

    fun refreshPermissions() {
        viewModelScope.launch {
            loadMissingPermissions(
                showLoading = true,
            )
        }
    }

    fun requestPermission(
        permission: AppPermission,
    ) {
        if (
            _state.value.permissionInProgress != null
        ) {
            return
        }

        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    permissionInProgress = permission,
                    errorMessage = null,
                )
            }

            try {
                val currentStatus =
                    permissionManager.check(
                        permission = permission,
                    )

                when (currentStatus) {
                    PermissionStatus.GRANTED,
                    PermissionStatus.PARTIAL_GRANTED -> {
                        // Niets nodig.
                    }

                    PermissionStatus.DENIED_ALWAYS -> {
                        permissionManager.openSettings()
                    }

                    PermissionStatus.NOT_DETERMINED,
                    PermissionStatus.DENIED -> {
                        permissionManager.request(
                            permission = permission,
                        )
                    }
                    PermissionStatus.BUSY -> TODO()
                }

                loadMissingPermissions(
                    showLoading = false,
                )
            } catch (cause: Throwable) {
                _state.update { currentState ->
                    currentState.copy(
                        errorMessage =
                            cause.message
                                ?: "De permissie kon niet worden aangepast.",
                    )
                }
            } finally {
                _state.update { currentState ->
                    currentState.copy(
                        permissionInProgress = null,
                    )
                }
            }
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            localNotificationManager.show(
                AppNotification(
                    id = "settings-test-v2",
                    title = "VitaMo",
                    body = "Test notificatie werkt 🎉",
                    channel = AppNotificationChannel.GENERAL,
                ),
            )
        }
    }

    private suspend fun loadMissingPermissions(
        showLoading: Boolean,
    ) {
        if (showLoading) {
            _state.update { currentState ->
                currentState.copy(
                    isPermissionsLoading = true,
                )
            }
        }

        try {
            val missingPermissions =
                RequiredAppPermissions.all
                    .mapNotNull { permission ->
                        val status =
                            permissionManager.check(
                                permission = permission,
                            )

                        if (status.hasAccess()) {
                            null
                        } else {
                            SettingsPermissionItem(
                                permission = permission,
                                status = status,
                            )
                        }
                    }

            _state.update { currentState ->
                currentState.copy(
                    missingPermissions =
                        missingPermissions,
                    isPermissionsLoading = false,
                )
            }
        } catch (cause: Throwable) {
            _state.update { currentState ->
                currentState.copy(
                    isPermissionsLoading = false,
                    errorMessage =
                        cause.message
                            ?: "Permissies konden niet worden gecontroleerd.",
                )
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
                        errorMessage =
                            cause.message
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

private fun PermissionStatus.hasAccess(): Boolean {
    return this == PermissionStatus.GRANTED ||
            this == PermissionStatus.PARTIAL_GRANTED
}