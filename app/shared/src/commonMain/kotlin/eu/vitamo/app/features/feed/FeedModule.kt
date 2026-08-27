package eu.vitamo.app.features.feed

import eu.vitamo.app.features.feed.api.FeedApi
import eu.vitamo.app.features.feed.api.FeedApiImpl
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.features.feed.repository.FeedRepositoryImpl
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedUseCase
import org.koin.dsl.module

val feedModule = module {
    single<FeedApi> { FeedApiImpl(get(), get(), get(), get()) }
    single<FeedRepository> { FeedRepositoryImpl(get()) }

    single { CreateFeedItemUseCase(get()) }
    single { GetFeedUseCase(feedRepository = get(),) }
}