package eu.vitamo.app.infrastructure.notification.push

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification

object FirebasePushSmokeTest {

    @JvmStatic
    fun main(args: Array<String>) {
        val projectId = System.getenv(
            "FIREBASE_PROJECT_ID",
        )
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: error(
                "Missing FIREBASE_PROJECT_ID environment variable.",
            )

        val firebaseInstallationId = System.getenv(
            "TEST_FIREBASE_INSTALLATION_ID",
        )
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: error(
                "Missing TEST_FIREBASE_INSTALLATION_ID environment variable.",
            )

        val options = FirebaseOptions.builder()
            .setCredentials(
                GoogleCredentials.getApplicationDefault(),
            )
            .setProjectId(projectId)
            .build()

        val firebaseApp =
            FirebaseApp.initializeApp(options)

        try {
            val message = Message.builder()
                .setFid(
                    firebaseInstallationId,
                )
                .setNotification(
                    Notification.builder()
                        .setTitle(
                            "VitaMo test",
                        )
                        .setBody(
                            "Push vanaf de VitaMo server werkt 🎉",
                        )
                        .build(),
                )
                .putData(
                    "title",
                    "VitaMo test",
                )
                .putData(
                    "body",
                    "Push vanaf de VitaMo server werkt 🎉",
                )
                .build()

            val messageId =
                FirebaseMessaging
                    .getInstance(firebaseApp)
                    .send(message)

            println(
                "Firebase push succesvol verstuurd: $messageId",
            )
        } finally {
            firebaseApp.delete()
        }
    }
}