package eu.vitamo.app.di

import eu.vitamo.app.features.auth.di.authModule
import eu.vitamo.app.database.DatabaseFactory
import eu.vitamo.app.database.databaseModule
import eu.vitamo.app.features.device.di.deviceModule
import eu.vitamo.app.features.feed.di.feedModule
import eu.vitamo.app.features.friendship.di.friendshipModule
import eu.vitamo.app.features.media.di.mediaModule
import eu.vitamo.app.features.user.di.userModule
import eu.vitamo.app.infrastructure.notification.di.notificationModule
import eu.vitamo.app.mail.di.mailModule
import eu.vitamo.app.sayHello
import eu.vitamo.app.serialization.AppJson
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var koinStarted = false

internal val serverModule: Module = module {
    single { sayHello("server") }

    single<Json> { AppJson }
}

private const val SKIP_DB_INIT_PROPERTY = "VITAMO_SKIP_DB_INIT"

fun initServerKoin() {
    if (koinStarted || GlobalContext.getOrNull() != null) {
        koinStarted = true
        return
    }

    startKoin {
        modules(
            serverModule,
            databaseModule,
            deviceModule,
            feedModule,
            notificationModule,
            authModule,
            mailModule,
            userModule,
            mediaModule,
            friendshipModule,
        )
    }

    val skipDbInit = System.getProperty(SKIP_DB_INIT_PROPERTY)?.toBooleanStrictOrNull() == true
    if (!skipDbInit) {
        GlobalContext.get().get<DatabaseFactory>().init()
    }

    koinStarted = true
}
