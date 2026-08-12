package eu.vitamo.app.infrastructure.notification

interface PlatformLocalNotificationManager {

    suspend fun show(
        notification: AppNotification,
    )

    suspend fun cancel(
        id: String,
    )
}