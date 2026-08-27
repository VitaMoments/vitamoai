package eu.vitamo.app.features.feed.di

import eu.vitamo.app.features.feed.context.FeedItemContextLoader
import eu.vitamo.app.features.feed.context.FeedItemContextProvider
import eu.vitamo.app.features.feed.context.FeedItemContextProviderImpl
import eu.vitamo.app.features.feed.context.FeedItemReactionContextLoader
import eu.vitamo.app.features.feed.context.FeedItemReactionContextProvider
import eu.vitamo.app.features.feed.context.FeedItemReactionContextProviderImpl
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepositoryImpl
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.features.feed.repository.FeedItemRepositoryImpl
import eu.vitamo.app.features.feed.usecase.CreateFeedItemCommentUseCase
import eu.vitamo.app.features.feed.usecase.CreateFeedItemReplyUseCase
import eu.vitamo.app.features.feed.usecase.CreatePostUseCase
import eu.vitamo.app.features.feed.usecase.DeleteMediaAssetUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedUseCase
import eu.vitamo.app.features.feed.usecase.LikeFeedItemReactionUseCase
import eu.vitamo.app.features.feed.usecase.LikeFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.UnlikeFeedItemReactionUseCase
import eu.vitamo.app.features.feed.usecase.UnlikeFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.friendship.di.friendshipModule
import org.koin.dsl.module

val feedModule = module {

    /*
     * Repositories
     */
    single<FeedItemRepository> {
        FeedItemRepositoryImpl()
    }

    single<FeedItemReactionRepository> {
        FeedItemReactionRepositoryImpl()
    }

    /*
     * Context providers
     */
    single<FeedItemContextProvider> {
        FeedItemContextProviderImpl(
            feedItemRepository = get(),
        )
    }

    single<FeedItemReactionContextProvider> {
        FeedItemReactionContextProviderImpl(
            reactionRepository = get(),
        )
    }

    /*
     * Context loaders
     */
    single {
        FeedItemReactionContextLoader(
            reactionRepository = get(),
            contextProvider = get(),
            userRepository = get(),
            userContextLoader = get(),
        )
    }

    single {
        FeedItemContextLoader(
            feedItemRepository = get(),
            reactionRepository = get(),
            mediaAssetRepository = get(),
            userRepository = get(),
            userContextLoader = get(),
            reactionContextLoader = get(),
            contextProvider = get(),
        )
    }

    /*
     * Read
     */
    single {
        GetFeedUseCase(
            friendshipRepository = get(),
            feedItemRepository = get(),
            feedItemContextLoader = get(),
        )
    }

    single {
        GetFeedItemUseCase(
            feedItemRepository = get(),
            feedItemContextLoader = get(),
        )
    }

    /*
     * Feed item likes
     */
    single {
        LikeFeedItemUseCase(
            feedItemRepository = get(),
        )
    }

    single {
        UnlikeFeedItemUseCase(
            feedItemRepository = get(),
        )
    }

    /*
     * Reaction likes
     */
    single {
        LikeFeedItemReactionUseCase(
            reactionRepository = get(),
        )
    }

    single {
        UnlikeFeedItemReactionUseCase(
            reactionRepository = get(),
        )
    }

    /*
     * Comments / replies
     */
    single {
        CreateFeedItemCommentUseCase(
            feedItemRepository = get(),
            reactionRepository = get(),
            reactionContextLoader = get(),
        )
    }

    single {
        CreateFeedItemReplyUseCase(
            feedItemRepository = get(),
            reactionRepository = get(),
            reactionContextLoader = get(),
        )
    }

    /*
     * Post creation
     */
    single {
        DeleteMediaAssetUseCase(
            mediaRepository = get(),
            mediaStorage = get(),
        )
    }

    single {
        CreatePostUseCase(
            feedItemRepository = get(),
            mediaAssetRepository = get(),
            uploadImageUseCase = get(),
            deleteMediaAssetUseCase = get(),
            feedItemContextLoader = get(),
        )
    }

    single {
        CreateFeedItemUseCase(
            createPostUseCase = get(),
        )
    }
}