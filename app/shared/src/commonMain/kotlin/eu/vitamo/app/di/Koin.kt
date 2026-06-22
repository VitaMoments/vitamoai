package eu.vitamo.app.di

import eu.vitamo.app.Greeting
import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.auth.api.AuthApiConfig
import eu.vitamo.app.auth.api.KtorAuthApi
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.auth.repository.DefaultAuthRepository
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.ClearableCookieStorage
import eu.vitamo.app.network.createAppHttpClient
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var koinStarted = false

internal val sharedAppModule: Module = module {
    single { Greeting() }
    single { AuthApiConfig() }
    single<AuthCookieStorage> { ClearableCookieStorage() }
    single<HttpClient> { createAppHttpClient(get()) }
    single<AuthApi> { KtorAuthApi(get(), get()) }
    single<AuthRepository> { DefaultAuthRepository(get(), get()) }
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

