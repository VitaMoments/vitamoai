package eu.vitamo.app.infrastructure.notification.push.model

data class PushNotification(
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap()
)