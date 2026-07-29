package eu.vitamo.app.di


import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.auth.api.AuthApiConfig
import eu.vitamo.app.auth.api.KtorAuthApi
import eu.vitamo.app.auth.repository.AuthRepository
import eu.vitamo.app.auth.repository.AuthRepositoryImpl
import eu.vitamo.app.di.modules.uiKoinModules
import eu.vitamo.app.features.user.api.UserApi
import eu.vitamo.app.features.user.api.UserApiConfig
import eu.vitamo.app.features.user.api.UserApiImpl
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.user.repository.UserRepositoryImpl
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
    single<HttpClient> { createAppHttpClient(get()) }

    single<AuthCookieStorage> { PersistentCookieStorage(createAuthCookiePersistence()) }
    single { AuthSessionCoordinator(get(), get(), get()) }
    single { AuthApiConfig() }
    single<AuthApi> { KtorAuthApi(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    single { UserApiConfig() }
    single<UserApi> { UserApiImpl(get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
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
