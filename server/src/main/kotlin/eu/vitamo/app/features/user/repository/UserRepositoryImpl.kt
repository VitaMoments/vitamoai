package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.mapper.searchPredicate
import eu.vitamo.app.features.user.mapper.toCredentialsRecord
import eu.vitamo.app.features.user.mapper.toRecord
import eu.vitamo.app.features.user.mapper.toUserRecord
import eu.vitamo.app.features.user.model.ProfileImageUpdateRecord
import eu.vitamo.app.features.user.model.UserCredentialsRecord
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.table.UsersTable
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserRepositoryImpl: UserRepository {
    override suspend fun search(
        currentUserId: Uuid,
        query: String?,
        limit: Int,
        offset: Long
    ): RepositoryResult<Page<UserRecord>> = dbQuery {
        val needle = query
            ?.trim()
            ?.takeIf(String::isNotEmpty)

        val safeLimit = limit.coerceIn(1, 50)
        val safeOffset = offset.coerceAtLeast(0)

        val predicate = searchPredicate(
            meId = currentUserId,
            needle = needle,
            includeRemoved = false,
        )

        val total = UsersTable
            .selectAll()
            .where { predicate }
            .count()

        val items = UsersTable
            .selectAll()
            .where { predicate }
            .orderBy(
                UsersTable.alias to SortOrder.ASC,
                UsersTable.id to SortOrder.ASC,
            )
            .limit(safeLimit)
            .offset(safeOffset)
            .map(ResultRow::toUserRecord)

        RepositoryResult.Success(
            data = Page(
                items = items,
                limit = safeLimit,
                offset = safeOffset,
                total = total,
            ),
        )
    }

    override suspend fun findByEmail(email: String): UserRecord? = dbQuery {
        findByEmailAsEntityInternal(email)?.toRecord()
    }

    private fun findByEmailAsEntityInternal(email: String): UserEntity? {
        return UserEntity.find {
            (UsersTable.email eq email.trim().lowercase()) and
                    UsersTable.deletedAt.isNull()
        }.firstOrNull()
    }

    override suspend fun findById(
        id: Uuid,
    ): RepositoryResult<UserRecord> = dbQuery {
        UserEntity
            .findById(id)
            ?.toRecord()
            ?.let { record ->
                RepositoryResult.Success(data = record)
            }
            ?: RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "User with id $id was not found.",
                ),
            )
    }

    override suspend fun findByIds(
        ids: Collection<Uuid>,
    ): RepositoryResult<List<UserRecord>> = dbQuery {
        if (ids.isEmpty()) {
            return@dbQuery RepositoryResult.Success(
                data = emptyList(),
            )
        }

        val entityIds = ids
            .distinct()
            .map { id ->
                EntityID(
                    id = id,
                    table = UsersTable,
                )
            }

        val users = UserEntity
            .find {
                (UsersTable.id inList entityIds) and
                        UsersTable.deletedAt.isNull()
            }
            .map { entity ->
                entity.toRecord()
            }

        RepositoryResult.Success(
            data = users,
        )
    }

    override fun deleteById(id: Uuid) {
        transaction {
            UserEntity.findById(id)?.delete()
        }
    }

    override suspend fun createUser(
        email: String,
        displayName: String,
        hashedPassword: String,
        firstName: String?,
        lastName: String?,
        alias: String?,
        birthDate: LocalDate?,
        now: Long,
    ): UserRecord = dbQuery {
        UserEntity.new {
            this.email = email
            this.displayName = displayName
            this.hashedPassword = hashedPassword
            this.firstName = firstName
            this.lastName = lastName
            this.alias = alias
            this.bio = null
            this.birthDate = birthDate
            this.role = UserRole.USER
            this.createdAt = now
            this.updatedAt = now
            this.emailVerifiedAt = null
            this.deletedAt = null
        }.toRecord()
    }

    override suspend fun findUserWithCredentials(email: String): RepositoryResult<UserCredentialsRecord> = dbQuery {
        UserEntity.find {
            UsersTable.email eq email.trim().lowercase()
        }.firstOrNull()
            ?.toCredentialsRecord()
            ?.let { record ->
                RepositoryResult.Success(data = record)
            }
            ?: RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "User with email $email was not found.",
                ),
            )
    }

    override suspend fun replaceProfileImage(
        userId: Uuid,
        profileImageId: Uuid,
    ): RepositoryResult<ProfileImageUpdateRecord> = dbQuery {
        val userEntity = UserEntity.findById(userId)
            ?: return@dbQuery RepositoryResult.Error(
                error = RepositoryError.NotFound(
                    message = "User with id $userId was not found.",
                ),
            )

        val previousProfileImageId =
            userEntity.profileImageId

        userEntity.profileImageId =
            profileImageId

        userEntity.updatedAt = Clock.System
            .now()
            .toEpochMilliseconds()

        RepositoryResult.Success(
            data = ProfileImageUpdateRecord(
                user = userEntity.toRecord(),
                previousProfileImageId =
                    previousProfileImageId,
            ),
        )
    }


    override fun markEmailVerified(id: Uuid, emailVerifiedAt: Instant, updatedAt: Long) {
        transaction {
            UserEntity.findById(id)?.let {
                it.emailVerifiedAt = emailVerifiedAt
                it.updatedAt = updatedAt
            }
        }
    }

    override fun updatePassword(userid: Uuid, hashedPassword: String) {
        transaction {
            UserEntity.findById(userid)?.let {
                it.hashedPassword = hashedPassword
                it.updatedAt = Clock.System.now().toEpochMilliseconds()
            }
        }
    }
}
