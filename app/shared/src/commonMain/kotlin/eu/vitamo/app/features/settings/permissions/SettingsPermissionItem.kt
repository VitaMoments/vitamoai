package eu.vitamo.app.features.settings.permissions

import eu.vitamo.app.infrastructure.permissions.AppPermission
import eu.vitamo.app.infrastructure.permissions.PermissionStatus

data class SettingsPermissionItem(
    val permission: AppPermission,
    val status: PermissionStatus,
)