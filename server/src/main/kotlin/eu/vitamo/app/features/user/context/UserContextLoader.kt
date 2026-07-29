package eu.vitamo.app.features.user.context

import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.user.mapper.toUser
import eu.vitamo.app.features.user.model.UserRecord
import kotlin.uuid.Uuid

class UserContextLoader(
    private val contextProvider: UserContextProvider,
) {
    suspend fun load(
        currentUserId: Uuid?,
        targetUser: UserRecord,
    ): UserWithContext {
        val context = contextProvider.resolve(
            currentUserId = currentUserId,
            targetUser = targetUser,
        )

        return UserWithContext(
            user = targetUser.toUser(context.accessLevel),
        )
    }

    suspend fun loadAll(
        currentUserId: Uuid?,
        targetUsers: List<UserRecord>,
    ): List<UserWithContext> =
        targetUsers.map { targetUser ->
            load(
                currentUserId = currentUserId,
                targetUser = targetUser,
            )
        }
}