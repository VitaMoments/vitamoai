package eu.vitamo.app.features.user

import eu.vitamo.app.features.user.api.UserApi
import eu.vitamo.app.features.user.api.UserApiImpl
import eu.vitamo.app.features.user.friendship.api.FriendshipApi
import eu.vitamo.app.features.user.friendship.api.FriendshipApiImpl
import eu.vitamo.app.features.user.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.user.friendship.repository.FriendshipRepositoryImpl
import eu.vitamo.app.features.user.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.GetFriendRequestsUseCase
import eu.vitamo.app.features.user.friendship.usecase.RejectFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.RemoveFriendshipUseCase
import eu.vitamo.app.features.user.friendship.usecase.RevokeFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.SendFriendRequestUseCase
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.user.repository.UserRepositoryImpl
import eu.vitamo.app.features.user.search.usecase.SearchUsersUseCase
import org.koin.dsl.module

val userModule = module {
    single<UserApi> { UserApiImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }

    single<FriendshipApi> { FriendshipApiImpl(get(), get()) }
    single<FriendshipRepository> { FriendshipRepositoryImpl(get()) }

    single { SearchUsersUseCase(get()) }
    single { GetFriendRequestsUseCase(get()) }

    single { AcceptFriendRequestUseCase(get()) }
    single { RejectFriendRequestUseCase(get()) }
    single { RevokeFriendRequestUseCase(get()) }
    single { RemoveFriendshipUseCase(get()) }
    single { SendFriendRequestUseCase(get()) }
}