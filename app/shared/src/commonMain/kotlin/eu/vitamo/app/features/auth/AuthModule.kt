package eu.vitamo.app.features.auth

import eu.vitamo.app.features.auth.api.AuthApi
import eu.vitamo.app.features.auth.api.KtorAuthApi
import eu.vitamo.app.features.auth.repository.AuthRepository
import eu.vitamo.app.features.auth.repository.AuthRepositoryImpl
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.auth.PersistentCookieStorage
import eu.vitamo.app.network.auth.createAuthCookiePersistence
import org.koin.dsl.module

val authModule = module {
    single<AuthCookieStorage> {
        PersistentCookieStorage(
            createAuthCookiePersistence(),
        )
    }

    single { AuthSessionCoordinator(get(), get(),) }

    single<AuthApi> { KtorAuthApi(get()) }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            authSessionCoordinator = get(),
            clientContextProvider = get(),
            firebaseInstallationIdStorage = get(),
            firebaseInstallationIdSynchronizer = get(),
        )
    }
}