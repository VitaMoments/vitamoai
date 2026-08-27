package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.repository.RepositoryResult

class GetFeedUseCase(
    private val feedRepository: FeedRepository,
) {

    suspend operator fun invoke(
        limit: Int = DEFAULT_LIMIT,
        offset: Long = 0L,
    ): RepositoryResult<PagedResult<FeedItem>> {
        return feedRepository.getFeed(
            limit = limit.coerceIn(
                minimumValue = 1,
                maximumValue = MAX_LIMIT,
            ),
            offset = offset.coerceAtLeast(0L),
        )
    }

    private companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 50
    }
}