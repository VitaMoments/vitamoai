package eu.vitamo.app.features.user.di

import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.context.UserContextProvider
import eu.vitamo.app.features.user.context.UserContextProviderImpl
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.user.repository.UserRepositoryImpl
import eu.vitamo.app.features.user.usecase.GetUserUseCase
import org.koin.dsl.module

val userModule = module {

    single<UserRepository> { UserRepositoryImpl() }
    single<UserContextProvider> { UserContextProviderImpl() }
    single<UserContextLoader> { UserContextLoader(get()) }

    single { GetUserUseCase(get(), get()) }
}