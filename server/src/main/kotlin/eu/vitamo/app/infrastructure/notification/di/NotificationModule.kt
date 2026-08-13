package eu.vitamo.app.infrastructure.notification.di

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import eu.vitamo.app.infrastructure.notification.push.service.PushNotificationService
import eu.vitamo.app.infrastructure.notification.push.service.PushNotificationServiceImpl
import org.koin.dsl.module

val notificationModule = module {
    single<FirebaseApp> {
        val projectId =
            System.getenv("FIREBASE_PROJECT_ID")
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: error(
                    "Missing FIREBASE_PROJECT_ID environment variable.",
                )

        val options =
            FirebaseOptions.builder()
                .setCredentials(
                    GoogleCredentials.getApplicationDefault(),
                )
                .setProjectId(projectId)
                .build()

        FirebaseApp.initializeApp(
            options,
        )
    }
    single<FirebaseMessaging> { FirebaseMessaging.getInstance(get<FirebaseApp>(),) }

    single<PushNotificationService> { PushNotificationServiceImpl(get()) }
}