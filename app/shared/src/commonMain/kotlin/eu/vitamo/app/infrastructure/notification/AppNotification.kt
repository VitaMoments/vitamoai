package eu.vitamo.app.infrastructure.notification

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val channel: AppNotificationChannel =
        AppNotificationChannel.GENERAL,
)