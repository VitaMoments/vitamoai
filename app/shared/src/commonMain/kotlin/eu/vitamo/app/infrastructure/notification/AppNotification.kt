package eu.vitamo.app.infrastructure.notification

import eu.vitamo.app.api.contracts.notification.PushNotificationAction

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val channel: AppNotificationChannel = AppNotificationChannel.GENERAL,
    val action: PushNotificationAction? = null
)