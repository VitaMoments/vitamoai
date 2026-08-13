package eu.vitamo.app.infrastructure.notification.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import eu.vitamo.app.features.device.service.FirebaseInstallationIdSynchronizer
import eu.vitamo.app.infrastructure.notification.AppNotification
import eu.vitamo.app.infrastructure.notification.AppNotificationChannel
import eu.vitamo.app.infrastructure.notification.LocalNotificationManager
import eu.vitamo.app.infrastructure.storage.FirebaseInstallationIdStorage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.auth.AuthStatus
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class VitaMoFirebaseMessagingService :
    FirebaseMessagingService(),
    KoinComponent {
    private val firebaseInstallationIdSynchronizer:
            FirebaseInstallationIdSynchronizer by inject()

    private val authSessionCoordinator:
            AuthSessionCoordinator by inject()
    private val localNotificationManager:
            LocalNotificationManager by inject()

    private val firebaseInstallationIdStorage:
            FirebaseInstallationIdStorage by inject()

    override fun onRegistered(
        installationId: String,
    ) {
        super.onRegistered(installationId)

        runBlocking {
            firebaseInstallationIdStorage.set(
                installationId = installationId,
            )

            if (
                authSessionCoordinator.state.value ==
                AuthStatus.Authenticated
            ) {
                firebaseInstallationIdSynchronizer
                    .synchronizeStored()
            }
        }
    }

    override fun onUnregistered(
        installationId: String,
    ) {
        super.onUnregistered(installationId)

        Log.d(
            TAG,
            "Firebase installation unregistered: $installationId",
        )

        runBlocking {
            val storedInstallationId =
                firebaseInstallationIdStorage.get()

            if (storedInstallationId == installationId) {
                firebaseInstallationIdStorage.clear()
            }
        }
    }

    override fun onMessageReceived(
        message: RemoteMessage,
    ) {
        super.onMessageReceived(message)

        Log.d(
            TAG,
            "FCM message received. messageId=${message.messageId}",
        )

        val notification =
            message.toAppNotification()
                ?: run {
                    Log.d(
                        TAG,
                        "FCM message contains no notification content.",
                    )

                    return
                }

        runBlocking {
            val result =
                localNotificationManager.show(
                    notification = notification,
                )

            Log.d(
                TAG,
                "FCM notification handled: $result",
            )
        }
    }

    private fun RemoteMessage.toAppNotification():
            AppNotification? {

        val title = notification
            ?.title
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: data[DATA_KEY_TITLE]
                ?.trim()
                ?.takeIf(String::isNotEmpty)

        val body = notification
            ?.body
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: data[DATA_KEY_BODY]
                ?.trim()
                ?.takeIf(String::isNotEmpty)

        if (
            title == null &&
            body == null
        ) {
            return null
        }

        return AppNotification(
            id = messageId
                ?: "fcm-${System.currentTimeMillis()}",
            title = title
                ?: DEFAULT_NOTIFICATION_TITLE,
            body = body.orEmpty(),
            channel = AppNotificationChannel.GENERAL,
        )
    }

    private companion object {
        const val TAG =
            "VitaMoFCM"

        const val DATA_KEY_TITLE =
            "title"

        const val DATA_KEY_BODY =
            "body"

        const val DEFAULT_NOTIFICATION_TITLE =
            "VitaMo"
    }
}