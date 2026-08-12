package eu.vitamo.app.infrastructure.notification

import eu.vitamo.app.infrastructure.permissions.AppPermission
import eu.vitamo.app.infrastructure.permissions.PermissionManager
import eu.vitamo.app.infrastructure.permissions.PermissionStatus

class LocalNotificationManager(
    private val permissionManager: PermissionManager,
    private val platformNotificationManager:
    PlatformLocalNotificationManager,
) {

    suspend fun show(
        notification: AppNotification,
    ): LocalNotificationResult {
        val permissionStatus =
            permissionManager.check(
                permission =
                    AppPermission.NOTIFICATIONS,
            )

        if (!permissionStatus.canShowNotifications()) {
            return LocalNotificationResult
                .PermissionRequired(
                    status = permissionStatus,
                )
        }

        return try {
            platformNotificationManager.show(
                notification = notification,
            )

            LocalNotificationResult.Shown
        } catch (throwable: Throwable) {
            LocalNotificationResult.Error(
                message = throwable.message,
            )
        }
    }

    suspend fun cancel(
        id: String,
    ) {
        platformNotificationManager.cancel(
            id = id,
        )
    }
}

private fun PermissionStatus.canShowNotifications(): Boolean {
    return this == PermissionStatus.GRANTED ||
            this == PermissionStatus.PARTIAL_GRANTED
}