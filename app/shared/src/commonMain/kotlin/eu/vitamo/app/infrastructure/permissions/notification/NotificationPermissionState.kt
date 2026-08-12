package eu.vitamo.app.infrastructure.permissions.notification

import eu.vitamo.app.infrastructure.permissions.PermissionStatus

data class NotificationPermissionState(
    val status: PermissionStatus = PermissionStatus.NOT_DETERMINED,
    val isLoading: Boolean = true,
) {

    val isGranted: Boolean
        get() = status == PermissionStatus.GRANTED ||
                status == PermissionStatus.PARTIAL_GRANTED

    val canRequest: Boolean
        get() = status == PermissionStatus.NOT_DETERMINED ||
                status == PermissionStatus.DENIED

    val requiresSettings: Boolean
        get() = status == PermissionStatus.DENIED_ALWAYS
}