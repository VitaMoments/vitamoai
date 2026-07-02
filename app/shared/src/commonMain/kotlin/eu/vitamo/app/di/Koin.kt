package eu.vitamo.app.di


import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.auth.api.AuthApiConfig
import eu.vitamo.app.auth.api.KtorAuthApi
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.auth.repository.DefaultAuthRepository
import eu.vitamo.app.features.feed.api.FeedApi
import eu.vitamo.app.features.feed.api.FeedApiConfig
import eu.vitamo.app.features.feed.api.KtorFeedApi
import eu.vitamo.app.features.feed.repository.DefaultFeedRepository
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.di.modules.uiKoinModules
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.auth.PersistentCookieStorage
import eu.vitamo.app.network.auth.createAuthCookiePersistence
import eu.vitamo.app.network.createAppHttpClient
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private var koinStarted = false

internal val sharedAppModule: Module = module {
    single { AuthApiConfig() }
    single<AuthCookieStorage> { PersistentCookieStorage(createAuthCookiePersistence()) }
    single<HttpClient> { createAppHttpClient(get()) }
    single<AuthApi> { KtorAuthApi(get(), get()) }
    single { AuthSessionCoordinator(get(), get(), get()) }
    single<AuthRepository> { DefaultAuthRepository(get(), get()) }
    single { FeedApiConfig() }
    single<FeedApi> { KtorFeedApi(get(), get(), get()) }
    single<FeedRepository> { DefaultFeedRepository(get()) }
}

fun initKoin() {
    if (koinStarted) {
        return
    }

    startKoin {
        modules(
            sharedAppModule,
            *uiKoinModules.toTypedArray())
    }

    koinStarted = true
}
