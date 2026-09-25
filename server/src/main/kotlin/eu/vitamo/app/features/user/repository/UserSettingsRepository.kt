package eu.vitamo.app.features.user.repository

import eu.vitamo.app.features.user.model.UserSettingsRecord
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

interface UserSettingsRepository {

    suspend fun findByUserId(
        userId: Uuid,
    ): RepositoryResult<UserSettingsRecord>

    suspend fun findByUserIds(
        userIds: Collection<Uuid>,
    ): RepositoryResult<Map<Uuid, UserSettingsRecord>>

    suspend fun updateCommentsEnabled(
        userId: Uuid,
        enabled: Boolean,
    ): RepositoryResult<UserSettingsRecord>
}