package eu.vitamo.app.features.user.di

import eu.vitamo.app.features.device.repository.DeviceRepository
import eu.vitamo.app.features.device.repository.DeviceRepositoryImpl
import eu.vitamo.app.features.media.usecase.UpdateProfileImageUseCase
import eu.vitamo.app.features.user.context.UserCapabilitiesProvider
import eu.vitamo.app.features.user.context.UserCapabilitiesProviderImpl
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.context.UserContextProvider
import eu.vitamo.app.features.user.context.UserContextProviderImpl
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.user.repository.UserRepositoryImpl
import eu.vitamo.app.features.user.repository.UserSettingsRepository
import eu.vitamo.app.features.user.repository.UserSettingsRepositoryImpl
import eu.vitamo.app.features.user.repository.UserSubscriptionRepository
import eu.vitamo.app.features.user.repository.UserSubscriptionRepositoryImpl
import eu.vitamo.app.features.user.usecase.GetUserUseCase
import eu.vitamo.app.features.user.usecase.SearchUsersUseCase
import org.koin.dsl.module

val userModule = module {
    single<DeviceRepository> { DeviceRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    single<UserContextProvider> { UserContextProviderImpl() }
    single<UserSubscriptionRepository> { UserSubscriptionRepositoryImpl() }
    single<UserSettingsRepository> { UserSettingsRepositoryImpl() }
    single<UserContextLoader> { UserContextLoader(get(), get(), get(), get(), get()) }
    single<UserCapabilitiesProvider> { UserCapabilitiesProviderImpl(get()) }

    single { SearchUsersUseCase(get(),get()) }
    single { GetUserUseCase(get(), get()) }
    single {
        UpdateProfileImageUseCase(
            userRepository = get(),
            mediaAssetRepository = get(),
            mediaStorage = get(),
            imageProcessor = get(),
            userContextLoader = get(),
        )
    }
}