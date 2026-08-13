package eu.vitamo.app.infrastructure.notification.push.service

import eu.vitamo.app.infrastructure.notification.push.model.PushNotification

interface PushNotificationService {
    suspend fun sendToDevice(
        firebaseInstallationId: String,
        notification: PushNotification
    )

    suspend fun sendToDevices(
        firebaseInstallationIds: List<String>,
        notification: PushNotification
    )
}