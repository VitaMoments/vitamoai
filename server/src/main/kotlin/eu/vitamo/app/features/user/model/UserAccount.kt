package eu.vitamo.app.features.user.model

import eu.vitamo.app.api.contracts.user.UserRole
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserAccount(
    val id: Uuid,
    val email: String,
    val displayName: String,
    val hashedPassword: String,
    val firstName: String?,
    val lastName: String?,
    val alias: String?,
    val bio: String?,
    val birthDate: LocalDate?,
    val role: UserRole,
    val emailVerifiedAt: Instant?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
