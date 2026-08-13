package eu.vitamo.app.infrastructure.notification.push.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import eu.vitamo.app.infrastructure.notification.push.model.PushNotification

class PushNotificationServiceImpl(
    private val firebaseMessaging: FirebaseMessaging,
) : PushNotificationService {
    override suspend fun sendToDevice(
        firebaseInstallationId: String,
        notification: PushNotification,
    ) {
        val message = Message.builder()
            .setFid(firebaseInstallationId)
            .setNotification(
                Notification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.body)
                    .build(),
            )
            .putAllData(notification.data)
            .build()

        firebaseMessaging.send(message)
    }

    override suspend fun sendToDevices(
        firebaseInstallationIds: List<String>,
        notification: PushNotification
    ) = firebaseInstallationIds.forEach { id -> sendToDevice(id, notification) }

}