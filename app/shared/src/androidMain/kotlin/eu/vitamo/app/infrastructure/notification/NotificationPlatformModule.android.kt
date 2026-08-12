package eu.vitamo.app.infrastructure.notification

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun notificationPlatformModule() =
    module {

        single<PlatformLocalNotificationManager> {
            AndroidLocalNotificationManager(
                context = androidContext(),
            )
        }
    }