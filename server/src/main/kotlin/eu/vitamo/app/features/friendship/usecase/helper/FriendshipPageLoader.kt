package eu.vitamo.app.features.friendship.usecase.helper

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.api.result.PagedResult
import eu.vitamo.app.features.friendship.model.FriendshipRecord
import eu.vitamo.app.features.user.context.UserContextLoader
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class FriendshipPageLoader(
    private val userRepository: UserRepository,
    private val userContextLoader: UserContextLoader,
) {

    suspend fun load(
        currentUserId: Uuid,
        page: Page<FriendshipRecord>,
    ): RepositoryResult<PagedResult<UserWithContext>> {
        val userRecords =
            mutableListOf<UserRecord>()

        for (friendship in page.items) {
            val otherUserId =
                friendship.otherUserId(
                    currentUserId =
                        currentUserId,
                )

            when (
                val result =
                    userRepository.findById(
                        id = otherUserId,
                    )
            ) {
                is RepositoryResult.Success -> {
                    userRecords += result.data
                }

                is RepositoryResult.Error -> {
                    return result
                }
            }
        }

        val usersWithContext =
            userContextLoader.loadAll(
                currentUserId =
                    currentUserId,
                targetUsers =
                    userRecords,
            )

        val consumed =
            page.offset +
                    usersWithContext.size

        val hasMore =
            consumed < page.total

        return RepositoryResult.Success(
            data = PagedResult(
                items = usersWithContext,
                limit = page.limit,
                offset = page.offset,
                total = page.total,
                hasMore = hasMore,
                nextOffset = if (hasMore) {
                    consumed
                } else {
                    null
                },
            ),
        )
    }
}