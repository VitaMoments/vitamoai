package eu.vitamo.app.features.user.context

import eu.vitamo.app.features.user.model.UserRecord
import kotlin.uuid.Uuid

interface UserContextProvider {
    suspend fun resolve(
        currentUserId: Uuid?,
        targetUser: UserRecord,
    ): UserContext

    suspend fun resolveAll(
        currentUserId: Uuid?,
        targetUsers: List<UserRecord>,
    ): Map<Uuid, UserContext>
}