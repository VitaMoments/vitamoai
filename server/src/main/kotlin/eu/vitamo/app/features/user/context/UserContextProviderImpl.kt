package eu.vitamo.app.features.user.context

import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class UserContextProviderImpl : UserContextProvider {

    override suspend fun resolve(
        currentUserId: Uuid?,
        targetUser: UserRecord,
    ): UserContext =
         createContext(
            currentUserId = currentUserId,
            user = targetUser,)

    override suspend fun resolveAll(
        currentUserId: Uuid?,
        targetUsers: List<UserRecord>,
    ): Map<Uuid, UserContext> =
        targetUsers.associate { user ->
            user.id to createContext(
                currentUserId = currentUserId,
                user = user,
            )
        }


    private fun createContext(
        currentUserId: Uuid?,
        user: UserRecord,
    ): UserContext {
        val accessLevel =
            if (currentUserId == user.id) {
                UserAccessLevel.SELF
            } else {
                UserAccessLevel.PUBLIC
            }

        return UserContext(
            accessLevel = accessLevel,
        )
    }
}