package eu.vitamo.app.infrastructure.notification

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

class IosLocalNotificationManager :
    PlatformLocalNotificationManager {

    override suspend fun show(
        notification: AppNotification,
    ) {
        suspendCancellableCoroutine { continuation ->
            val content = UNMutableNotificationContent()

            content.setTitle(
                notification.title,
            )

            content.setBody(
                notification.body,
            )

            content.setSound(
                UNNotificationSound.defaultSound,
            )

            val request =
                UNNotificationRequest.requestWithIdentifier(
                    identifier = notification.id,
                    content = content,
                    trigger = null,
                )

            UNUserNotificationCenter
                .currentNotificationCenter()
                .addNotificationRequest(
                    request = request,
                    withCompletionHandler = { error ->
                        if (!continuation.isActive) {
                            return@addNotificationRequest
                        }

                        if (error == null) {
                            continuation.resumeWith(
                                Result.success(Unit),
                            )
                        } else {
                            continuation.resumeWith(
                                Result.failure(
                                    IllegalStateException(
                                        error.localizedDescription,
                                    ),
                                ),
                            )
                        }
                    },
                )
        }
    }

    override suspend fun cancel(
        id: String,
    ) {
        val notificationCenter =
            UNUserNotificationCenter
                .currentNotificationCenter()

        notificationCenter
            .removePendingNotificationRequestsWithIdentifiers(
                listOf(id),
            )

        notificationCenter
            .removeDeliveredNotificationsWithIdentifiers(
                listOf(id),
            )
    }
}