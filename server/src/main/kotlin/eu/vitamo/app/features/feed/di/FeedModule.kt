package eu.vitamo.app.features.feed.di

import eu.vitamo.app.features.feed.repository.ExposedFeedRepository
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.service.FeedFriendshipService
import eu.vitamo.app.features.feed.service.NoopFeedFriendshipService
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.DeleteFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedPageUseCase
import eu.vitamo.app.features.feed.usecase.UpdateFeedItemUseCase
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import org.koin.dsl.module

val feedModule = module {
    single<FeedRepository> { ExposedFeedRepository() }
    single<FeedFriendshipService> { NoopFeedFriendshipService() }
    single { FeedInputValidator() }

    single { CreateFeedItemUseCase(get(), get(), get()) }
    single { GetFeedItemUseCase(get(), get(), get()) }
    single { GetFeedPageUseCase(get(), get(), get()) }
    single { UpdateFeedItemUseCase(get(), get(), get()) }
    single { DeleteFeedItemUseCase(get()) }
}
