package eu.vitamo.app.infrastructure.notification

import org.koin.dsl.module

actual fun notificationPlatformModule() =
    module {

        single<PlatformLocalNotificationManager> {
            IosLocalNotificationManager()
        }
    }