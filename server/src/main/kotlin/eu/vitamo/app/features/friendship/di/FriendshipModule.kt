package eu.vitamo.app.features.friendship.di

import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.friendship.repository.FriendshipRepositoryImpl
import eu.vitamo.app.features.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.DeleteFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.GetFriendRequestsUseCase
import eu.vitamo.app.features.friendship.usecase.GetFriendsUseCase
import eu.vitamo.app.features.friendship.usecase.RemoveFriendshipUseCase
import eu.vitamo.app.features.friendship.usecase.SendFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.helper.FriendshipPageLoader
import eu.vitamo.app.features.user.context.FriendshipsProvider
import eu.vitamo.app.features.user.context.FriendshipsProviderImpl
import eu.vitamo.app.features.user.context.UserContextLoader
import org.koin.dsl.module
import kotlin.math.sin

val friendshipModule = module {
    single<FriendshipRepository> { FriendshipRepositoryImpl() }

    single { FriendshipPageLoader(get(), get())}
    single<FriendshipsProvider> { FriendshipsProviderImpl(get(), get(), get()) }

    single { SendFriendRequestUseCase(get(),get(),get(),get(),get()) }
    single { AcceptFriendRequestUseCase(get(),get(), get())}
    single { DeleteFriendRequestUseCase(get(),get(),get()) }
    single { RemoveFriendshipUseCase(get(),get(),get()) }
    single { GetFriendRequestsUseCase(get(), get()) }
    single { GetFriendsUseCase(get(), get()) }
}