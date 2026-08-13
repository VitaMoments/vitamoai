package eu.vitamo.app.infrastructure.notification.push.service


import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import eu.vitamo.app.api.contracts.notification.PushNotificationDataKeys
import eu.vitamo.app.infrastructure.notification.push.model.PushNotification

class PushNotificationServiceImpl(
    private val firebaseMessaging:
    FirebaseMessaging,
) : PushNotificationService {

    override suspend fun sendToDevice(
        firebaseInstallationId: String,
        notification: PushNotification,
    ) {
        val message =
            Message.builder()
                .setFid(
                    firebaseInstallationId,
                )
                .putAllData(
                    notification.toFirebaseData(),
                )
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(
                            AndroidConfig.Priority.HIGH,
                        )
                        .build(),
                )
                .build()

        firebaseMessaging.send(
            message,
        )
    }

    override suspend fun sendToDevices(
        firebaseInstallationIds: List<String>,
        notification: PushNotification
    ) = firebaseInstallationIds.forEach { id -> sendToDevice(id, notification) }

    private fun PushNotification.toFirebaseData():
            Map<String, String> {
        return buildMap {
            put(PushNotificationDataKeys.TITLE, title)
            put(PushNotificationDataKeys.BODY, body)

            action?.let { action ->
                put(
                    PushNotificationDataKeys.ACTION,
                    action.type.wireValue,
                )

                action.targetId?.let { targetId ->
                    put(
                        PushNotificationDataKeys.TARGET_ID,
                        targetId.toString(),
                    )
                }
            }
        }
    }
}





