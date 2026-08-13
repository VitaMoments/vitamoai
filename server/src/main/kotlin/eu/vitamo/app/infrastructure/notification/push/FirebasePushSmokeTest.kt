package eu.vitamo.app.infrastructure.notification.push

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import eu.vitamo.app.api.contracts.notification.PushNotificationDataKeys

object FirebasePushSmokeTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val projectId =
            System.getenv(
                "FIREBASE_PROJECT_ID",
            )
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: error(
                    "Missing FIREBASE_PROJECT_ID environment variable.",
                )

        val firebaseInstallationId =
            System.getenv(
                "TEST_FIREBASE_INSTALLATION_ID",
            )
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: error(
                    "Missing TEST_FIREBASE_INSTALLATION_ID environment variable.",
                )

        val options =
            FirebaseOptions.builder()
                .setCredentials(
                    GoogleCredentials.getApplicationDefault(),
                )
                .setProjectId(
                    projectId,
                )
                .build()

        val firebaseApp =
            FirebaseApp.initializeApp(
                options,
            )

        try {
            val message =
                Message.builder()
                    .setFid(
                        firebaseInstallationId,
                    )
                    .putData(
                        PushNotificationDataKeys.TITLE,
                        "Goedemorgen 👋",
                    )
                    .putData(
                        PushNotificationDataKeys.BODY,
                        "Welkom! Heb een fijne dag vandaag.",
                    )
                    .setAndroidConfig(
                        AndroidConfig.builder()
                            .setPriority(
                                AndroidConfig.Priority.HIGH,
                            )
                            .build(),
                    )
                    .build()

            val messageId =
                FirebaseMessaging
                    .getInstance(
                        firebaseApp,
                    )
                    .send(
                        message,
                    )

            println(
                "General push succesvol verstuurd: $messageId",
            )
        } finally {
            firebaseApp.delete()
        }
    }
}