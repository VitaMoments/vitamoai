package eu.vitamo.app.infrastructure.di

import com.russhwolf.settings.Settings
import dev.brewkits.grant.di.grantModule
import dev.brewkits.grant.di.grantPlatformModule
import eu.vitamo.app.features.device.service.FirebaseInstallationIdSynchronizer
import eu.vitamo.app.infrastructure.app.AppInitializer
import eu.vitamo.app.infrastructure.di.modules.koinModules
import eu.vitamo.app.infrastructure.di.modules.uiKoinModules
import eu.vitamo.app.infrastructure.notification.notificationKoinModule
import eu.vitamo.app.infrastructure.notification.notificationPlatformModule
import eu.vitamo.app.infrastructure.permissions.GrantPermissionManager
import eu.vitamo.app.infrastructure.permissions.PermissionManager
import eu.vitamo.app.infrastructure.permissions.notification.NotificationPermissionViewModel
import eu.vitamo.app.infrastructure.storage.AppPreferencesStorage
import eu.vitamo.app.infrastructure.storage.AppPreferencesStorageImpl
import eu.vitamo.app.infrastructure.storage.ClientInstanceIdStorage
import eu.vitamo.app.infrastructure.storage.ClientInstanceIdStorageImpl
import eu.vitamo.app.infrastructure.storage.FirebaseInstallationIdStorage
import eu.vitamo.app.infrastructure.storage.FirebaseInstallationIdStorageImpl
import eu.vitamo.app.network.createAppHttpClient
import eu.vitamo.app.serialization.AppJson
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private var koinStarted = false

internal val sharedAppModule: Module = module {
    single<Json> { AppJson }

    single<HttpClient> {
        createAppHttpClient(
            cookieStorage = get(),
        )
    }

    single<Settings> {
        Settings()
    }

    single<FirebaseInstallationIdStorage> {
        FirebaseInstallationIdStorageImpl(
            settings = get(),
        )
    }

    single<AppPreferencesStorage> {
        AppPreferencesStorageImpl(
            settings = get(),
        )
    }

    single {
        FirebaseInstallationIdSynchronizer(
            firebaseInstallationIdStorage = get(),
            deviceRepository = get(),
        )
    }

    single<ClientInstanceIdStorage> {
        ClientInstanceIdStorageImpl(
            settings = get(),
        )
    }

    single {
        AppInitializer(
            clientInstanceInitializer = get(),
        )
    }

    single<PermissionManager> {
        GrantPermissionManager(
            grantManager = get(),
        )
    }

    viewModelOf(::NotificationPermissionViewModel)
}

fun initKoin(
    appDeclaration: KoinAppDeclaration = {},
) {
    if (koinStarted) {
        return
    }

    startKoin {
        appDeclaration()

        modules(
            grantModule,
            grantPlatformModule,
            notificationKoinModule,
            notificationPlatformModule(),
            sharedAppModule,
            *koinModules.toTypedArray(),
            *uiKoinModules.toTypedArray(),
        )
    }

    koinStarted = true
}