package eu.vitamo.app.features.user.repository

import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.user.entity.UserSettingsEntity
import eu.vitamo.app.features.user.model.UserSettingsRecord
import eu.vitamo.app.features.user.table.UserSettingsTable
import eu.vitamo.app.features.user.table.UsersTable
import eu.vitamo.app.repository.RepositoryResult
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import kotlin.time.Clock
import kotlin.uuid.Uuid

class UserSettingsRepositoryImpl :
    UserSettingsRepository {

    override suspend fun findByUserId(
        userId: Uuid,
    ): RepositoryResult<UserSettingsRecord> =
        dbQuery {
            val userEntityId =
                EntityID(
                    id = userId,
                    table = UsersTable,
                )

            val entity =
                UserSettingsEntity
                    .find {
                        UserSettingsTable.userId eq
                            userEntityId
                    }
                    .firstOrNull()

            RepositoryResult.Success(
                data = entity?.toRecord()
                    ?: defaultSettings(
                        userId = userId,
                    ),
            )
        }

    override suspend fun findByUserIds(
        userIds: Collection<Uuid>,
    ): RepositoryResult<Map<Uuid, UserSettingsRecord>> =
        dbQuery {
            val distinctUserIds =
                userIds.distinct()

            if (distinctUserIds.isEmpty()) {
                return@dbQuery RepositoryResult.Success(
                    data = emptyMap(),
                )
            }

            val entityIds =
                distinctUserIds.map { userId ->
                    EntityID(
                        id = userId,
                        table = UsersTable,
                    )
                }

            val storedSettings =
                UserSettingsEntity
                    .find {
                        UserSettingsTable.userId inList
                            entityIds
                    }
                    .associate { entity ->
                        entity.userId.value to
                            entity.toRecord()
                    }

            /*
             * Een gebruiker zonder opgeslagen settings
             * krijgt gewoon de defaults.
             */
            val settings =
                distinctUserIds.associateWith { userId ->
                    storedSettings[userId]
                        ?: defaultSettings(
                            userId = userId,
                        )
                }

            RepositoryResult.Success(
                data = settings,
            )
        }

    override suspend fun updateCommentsEnabled(
        userId: Uuid,
        enabled: Boolean,
    ): RepositoryResult<UserSettingsRecord> =
        dbQuery {
            val userEntityId =
                EntityID(
                    id = userId,
                    table = UsersTable,
                )

            val now =
                Clock.System.now()
                    .epochSeconds

            val entity =
                UserSettingsEntity
                    .find {
                        UserSettingsTable.userId eq
                            userEntityId
                    }
                    .firstOrNull()
                    ?.apply {
                        commentsEnabled = enabled
                        updatedAt = now
                    }
                    ?: UserSettingsEntity.new {
                        this.userId = userEntityId
                        this.commentsEnabled = enabled
                        this.createdAt = now
                        this.updatedAt = now
                    }

            RepositoryResult.Success(
                data = entity.toRecord(),
            )
        }

    private fun UserSettingsEntity.toRecord() =
        UserSettingsRecord(
            userId = userId.value,
            commentsEnabled = commentsEnabled,
        )

    private fun defaultSettings(
        userId: Uuid,
    ) =
        UserSettingsRecord(
            userId = userId,
            commentsEnabled =
                DEFAULT_COMMENTS_ENABLED,
        )

    private companion object {
        const val DEFAULT_COMMENTS_ENABLED =
            true
    }
}