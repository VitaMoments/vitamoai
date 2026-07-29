package eu.vitamo.app.features.user.repository

import eu.vitamo.app.features.user.model.UserCredentialsRecord
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.infrastructure.network.models.Page
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface UserRepository {
    suspend fun search(
        currentUserId: Uuid,
        query: String?,
        limit: Int = 20,
        offset: Long = 0,
    ) : RepositoryResult<Page<UserRecord>>
    suspend fun findByEmail(email: String): UserRecord?
    suspend fun findById(id: Uuid): RepositoryResult<UserRecord>
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
    ): UserRecord

    suspend fun findUserWithCredentials(email: String): RepositoryResult<UserCredentialsRecord>

    fun markEmailVerified(id: Uuid, emailVerifiedAt: Instant, updatedAt: Long)
    fun updatePassword(userid: Uuid, hashedPassword: String)
}
