package eu.vitamo.app.features.user.di

import eu.vitamo.app.features.media.usecase.UpdateProfileImageUseCase
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.context.UserContextProvider
import eu.vitamo.app.features.user.context.UserContextProviderImpl
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.user.repository.UserRepositoryImpl
import eu.vitamo.app.features.user.usecase.GetUserUseCase
import eu.vitamo.app.features.user.usecase.SearchUsersUseCase
import org.koin.dsl.module

val userModule = module {

    single<UserRepository> { UserRepositoryImpl() }
    single<UserContextProvider> { UserContextProviderImpl() }
    single<UserContextLoader> { UserContextLoader(get(), get(), get()) }

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