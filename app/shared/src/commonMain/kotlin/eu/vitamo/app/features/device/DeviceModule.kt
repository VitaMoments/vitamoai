package eu.vitamo.app.features.device

import eu.vitamo.app.features.device.api.DeviceApi
import eu.vitamo.app.features.device.api.DeviceApiImpl
import eu.vitamo.app.features.device.provider.ClientContextProvider
import eu.vitamo.app.features.device.provider.ClientContextProviderImpl
import eu.vitamo.app.features.device.provider.ClientInstanceInitializer
import eu.vitamo.app.features.device.provider.PlatformClientInfoProvider
import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.features.device.repository.DeviceRepositoryImpl
import org.koin.dsl.module

val deviceModule = module {
    single<DeviceApi> {
        DeviceApiImpl(
            client = get(),
            authSessionCoordinator = get(),
        )
    }

    single<DeviceRepository> {
        DeviceRepositoryImpl(
            deviceApi = get(),
        )
    }

    single {
        PlatformClientInfoProvider()
    }

    single<ClientContextProvider> {
        ClientContextProviderImpl(
            clientInstanceIdStorage = get(),
            platformClientInfoProvider = get(),
        )
    }

    single {
        ClientInstanceInitializer(
            clientInstanceIdStorage = get(),
        )
    }
}