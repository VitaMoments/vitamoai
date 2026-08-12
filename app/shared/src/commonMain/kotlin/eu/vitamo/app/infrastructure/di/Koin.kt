package eu.vitamo.app.infrastructure.di


import dev.brewkits.grant.di.grantModule
import dev.brewkits.grant.di.grantPlatformModule
import eu.vitamo.app.features.auth.api.AuthApi
import eu.vitamo.app.features.auth.api.AuthApiConfig
import eu.vitamo.app.features.auth.api.KtorAuthApi
import eu.vitamo.app.features.auth.repository.AuthRepository
import eu.vitamo.app.features.auth.repository.AuthRepositoryImpl
import eu.vitamo.app.infrastructure.di.modules.uiKoinModules
import eu.vitamo.app.features.user.friendship.api.FriendshipApiImpl
import eu.vitamo.app.features.media.api.MediaApi
import eu.vitamo.app.features.media.api.MediaApiConfig
import eu.vitamo.app.features.media.api.MediaApiImpl
import eu.vitamo.app.features.media.repository.MediaRepository
import eu.vitamo.app.features.media.repository.MediaRepositoryImpl
import eu.vitamo.app.features.user.api.UserApi
import eu.vitamo.app.features.user.api.UserApiConfig
import eu.vitamo.app.features.user.api.UserApiImpl
import eu.vitamo.app.features.user.friendship.api.FriendshipApi
import eu.vitamo.app.features.user.friendship.api.FriendshipApiConfig
import eu.vitamo.app.features.user.friendship.repository.FriendshipRepository
import eu.vitamo.app.features.user.friendship.repository.FriendshipRepositoryImpl
import eu.vitamo.app.features.user.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.RejectFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.RemoveFriendshipUseCase
import eu.vitamo.app.features.user.friendship.usecase.RevokeFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.SendFriendRequestUseCase
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.user.repository.UserRepositoryImpl
import eu.vitamo.app.features.user.search.usecase.SearchUsersUseCase
import eu.vitamo.app.infrastructure.notification.notificationKoinModule
import eu.vitamo.app.infrastructure.notification.notificationPlatformModule
import eu.vitamo.app.infrastructure.permissions.notification.NotificationPermissionViewModel
import eu.vitamo.app.infrastructure.permissions.GrantPermissionManager
import eu.vitamo.app.infrastructure.permissions.PermissionManager
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.auth.PersistentCookieStorage
import eu.vitamo.app.network.auth.createAuthCookiePersistence
import eu.vitamo.app.network.createAppHttpClient
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private var koinStarted = false

internal val sharedAppModule: Module = module {
    single<HttpClient> { createAppHttpClient(get()) }

    single<PermissionManager> {
        GrantPermissionManager(
            grantManager = get(),
        )
    }

    viewModelOf(::NotificationPermissionViewModel,)

    single<AuthCookieStorage> { PersistentCookieStorage(createAuthCookiePersistence()) }
    single { AuthSessionCoordinator(get(), get(), get()) }
    single { AuthApiConfig() }
    single<AuthApi> { KtorAuthApi(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    single { UserApiConfig() }
    single<UserApi> { UserApiImpl(get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single { SearchUsersUseCase(get()) }

    single { MediaApiConfig() }
    single<MediaApi> { MediaApiImpl(get(), get(), get()) }
    single<MediaRepository> { MediaRepositoryImpl(get()) }

    single { FriendshipApiConfig() }
    single<FriendshipApi> { FriendshipApiImpl(get(), get(),get()) }
    single<FriendshipRepository> { FriendshipRepositoryImpl(get()) }
    single { AcceptFriendRequestUseCase(get()) }
    single { RejectFriendRequestUseCase(get()) }
    single { RevokeFriendRequestUseCase(get()) }
    single { RemoveFriendshipUseCase(get()) }
    single { SendFriendRequestUseCase(get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
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
            *uiKoinModules.toTypedArray())
    }

    koinStarted = true
}
