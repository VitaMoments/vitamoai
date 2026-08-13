package eu.vitamo.app.infrastructure.notification.push.model

import eu.vitamo.app.api.contracts.notification.PushNotificationAction
import kotlin.uuid.Uuid

sealed interface PushNotification {

    val title: String

    val body: String

    val action: PushNotificationAction?

    data class FriendRequestReceived(
        val userId: Uuid,
        val username: String,
    ) : PushNotification {

        override val title: String = "Nieuw vriendschapsverzoek"

        override val body: String = "$username heeft je een vriendschapsverzoek gestuurd."

        override val action: PushNotificationAction = PushNotificationAction.OpenFriendRequests(userId = userId)
    }

    data class GeneralMessage(
        override val title: String,
        override val body: String,
    ) : PushNotification {
        override val action: PushNotificationAction? = null
    }
}