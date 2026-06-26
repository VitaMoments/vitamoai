package eu.vitamo.app.features.user.repository

import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.model.UserAccount
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface UserRepository {
    suspend fun findByEmail(email: String): UserAccount?
    suspend fun findByEmailAsEntity(email: String): UserEntity?

    suspend fun findById(id: Uuid): UserAccount?

    fun deleteById(id: Uuid)

    suspend fun createUser(
        email: String,
        displayName: String,
        hashedPassword: String,
        firstName: String?,
        lastName: String?,
        alias: String?,
        birthDate: LocalDate?,
        now: Long,
    ): UserAccount

    fun markEmailVerified(id: Uuid, emailVerifiedAt: Instant, updatedAt: Long)

    fun updatePassword(userid: Uuid, hashedPassword: String)
}
