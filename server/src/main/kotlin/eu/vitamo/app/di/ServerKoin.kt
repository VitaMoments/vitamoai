package eu.vitamo.app.di

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

fun initServerKoin() {
    if (koinStarted) {
        return
    }

    startKoin {
        modules(serverModule, databaseModule)
    }

    GlobalContext.get().get<DatabaseFactory>().init()

    koinStarted = true
}



