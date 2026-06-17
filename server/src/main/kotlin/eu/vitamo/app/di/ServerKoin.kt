package eu.vitamo.app.di

import eu.vitamo.app.auth.di.authModule
import eu.vitamo.app.database.DatabaseFactory
import eu.vitamo.app.database.databaseModule
import eu.vitamo.app.sayHello
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var koinStarted = false

internal val serverModule: Module = module {
    single { sayHello("server") }
}

private const val SKIP_DB_INIT_PROPERTY = "VITAMO_SKIP_DB_INIT"

fun initServerKoin() {
    if (koinStarted || GlobalContext.getOrNull() != null) {
        koinStarted = true
        return
    }

    startKoin {
        modules(serverModule, databaseModule, authModule)
    }

    val skipDbInit = System.getProperty(SKIP_DB_INIT_PROPERTY)?.toBooleanStrictOrNull() == true
    if (!skipDbInit) {
        GlobalContext.get().get<DatabaseFactory>().init()
    }

    koinStarted = true
}
