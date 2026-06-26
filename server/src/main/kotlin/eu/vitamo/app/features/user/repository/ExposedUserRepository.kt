package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.model.UserAccount
import eu.vitamo.app.features.user.table.UsersTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExposedUserRepository : UserRepository {
    override suspend fun findByEmail(email: String): UserAccount? = dbQuery {
        findByEmailAsEntityInternal(email)?.toUserAccount()
    }

    override suspend fun findByEmailAsEntity(email: String): UserEntity? = dbQuery {
        findByEmailAsEntityInternal(email)
    }

    private fun findByEmailAsEntityInternal(email: String): UserEntity? {
        return UserEntity.find {
            (UsersTable.email eq email.trim().lowercase()) and
                    UsersTable.deletedAt.isNull()
        }.firstOrNull()
    }

    override suspend fun findById(id: Uuid): UserAccount? = dbQuery {
        UserEntity.findById(id)?.toUserAccount()
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
    ): UserAccount = dbQuery {
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
        }.toUserAccount()
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

private fun UserEntity.toUserAccount(): UserAccount = UserAccount(
    id = id.value,
    email = email,
    displayName = displayName,
    hashedPassword = hashedPassword,
    firstName = firstName,
    lastName = lastName,
    alias = alias,
    bio = bio,
    birthDate = birthDate,
    role = role,
    emailVerifiedAt = emailVerifiedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)
