package eu.vitamo.app.infrastructure.notification.push.model

sealed interface PushNotification {
    val title: String
    val body: String
    val data: Map<String, String>

    data class FriendRequestReceived(
        val username: String,
    ) : PushNotification {
        override val title: String =
            "Nieuw vriendschapsverzoek"

        override val body: String = "$username heeft je een vriendschapsverzoek gestuurd."

        override val data: Map<String, String> = mapOf("type" to "friend_request_received")
    }
}