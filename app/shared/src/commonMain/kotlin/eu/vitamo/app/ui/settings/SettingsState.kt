package eu.vitamo.app.ui.settings

import eu.vitamo.app.features.settings.permissions.SettingsPermissionItem
import eu.vitamo.app.infrastructure.permissions.AppPermission
import eu.vitamo.app.ui.theme.ThemeMode

data class SettingsState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    val themeMode: ThemeMode = ThemeMode.SYSTEM,

    val missingPermissions: List<SettingsPermissionItem> =
        emptyList(),

    val isPermissionsLoading: Boolean = false,

    val permissionInProgress: AppPermission? = null,

    val errorMessage: String? = null,
)