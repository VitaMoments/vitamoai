package eu.vitamo.app.infrastructure.notification.di

import eu.vitamo.app.infrastructure.notification.push.service.PushNotificationService
import eu.vitamo.app.infrastructure.notification.push.service.PushNotificationServiceImpl
import org.koin.dsl.module

val notificationModule = module {
    single<PushNotificationService> { PushNotificationServiceImpl(get()) }
}