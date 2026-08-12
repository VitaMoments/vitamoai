package eu.vitamo.app.infrastructure.notification

import eu.vitamo.app.infrastructure.permissions.PermissionStatus

sealed interface LocalNotificationResult {

    data object Shown : LocalNotificationResult

    data class PermissionRequired(
        val status: PermissionStatus,
    ) : LocalNotificationResult

    data class Error(
        val message: String?,
    ) : LocalNotificationResult
}