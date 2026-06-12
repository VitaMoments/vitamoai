package eu.vitamo.app.di

import eu.vitamo.app.Greeting
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var koinStarted = false

internal val sharedAppModule: Module = module {
    single { Greeting() }
}

internal expect fun platformModule(): Module

fun initKoin() {
    if (koinStarted) {
        return
    }

    startKoin {
        modules(sharedAppModule, platformModule())
    }

    koinStarted = true
}



