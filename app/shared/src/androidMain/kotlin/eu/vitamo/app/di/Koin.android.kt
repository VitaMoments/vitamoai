package eu.vitamo.app.di

import eu.vitamo.app.Platform
import eu.vitamo.app.getPlatform
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun platformModule(): Module = module {
    single<Platform> { getPlatform() }
}


