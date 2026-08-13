package eu.vitamo.app.features.device.di

import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.features.device.repository.DeviceRepositoryImpl
import eu.vitamo.app.features.device.usecase.UpdateFirebaseInstallationIdUseCase
import org.koin.dsl.module

val deviceModule = module {
    single<DeviceRepository> {
        DeviceRepositoryImpl()
    }

    single {
        UpdateFirebaseInstallationIdUseCase(
            deviceRepository = get(),
        )
    }
}