package eu.vitamo.app.features.friendship.di

import eu.vitamo.app.features.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.friendship.repository.FriendshipRepositoryImpl
import eu.vitamo.app.features.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.DeleteFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.GetFriendsUseCase
import eu.vitamo.app.features.friendship.usecase.GetIncomingFriendRequestsUseCase
import eu.vitamo.app.features.friendship.usecase.GetOutgoingFriendRequestsUseCase
import eu.vitamo.app.features.friendship.usecase.RemoveFriendshipUseCase
import eu.vitamo.app.features.friendship.usecase.SendFriendRequestUseCase
import eu.vitamo.app.features.friendship.usecase.helper.FriendshipPageLoader
import eu.vitamo.app.features.user.context.UserContextLoader
import org.koin.dsl.module

val friendshipModule = module {
    single<FriendshipRepository> {
        FriendshipRepositoryImpl()
    }

    single {
        UserContextLoader(
            contextProvider = get(),
            mediaAssetRepository = get(),
            friendshipRepository = get(),
        )
    }

    single {
        FriendshipPageLoader(
            userRepository = get(),
            userContextLoader = get(),
        )
    }

    single {
        SendFriendRequestUseCase(
            userRepository = get(),
            friendshipRepository = get(),
            userContextLoader = get(),
            notificationService = get(),
            deviceRepository = get()
        )
    }

    single {
        AcceptFriendRequestUseCase(
            friendshipRepository = get(),
            userRepository = get(),
            userContextLoader = get(),
        )
    }

    single {
        DeleteFriendRequestUseCase(
            userRepository = get(),
            friendshipRepository = get(),
            userContextLoader = get()
        )
    }

    single {
        RemoveFriendshipUseCase(
            userRepository = get(),
            friendshipRepository = get(),
            userContextLoader = get()
        )
    }

    single {
        GetIncomingFriendRequestsUseCase(
            friendshipRepository = get(),
            friendshipPageLoader = get(),
        )
    }

    single {
        GetOutgoingFriendRequestsUseCase(
            friendshipRepository = get(),
            friendshipPageLoader = get(),
        )
    }

    single {
        GetFriendsUseCase(
            friendshipRepository = get(),
            friendshipPageLoader = get(),
        )
    }
}