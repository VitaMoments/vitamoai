package eu.vitamo.app.features.user.model

import eu.vitamo.app.api.contracts.user.UserRole
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserRecord(
    val id: Uuid,
    val email: String,
    val displayName: String,
    val firstName: String?,
    val lastName: String?,
    val alias: String?,
    val bio: String?,
    val birthDate: LocalDate?,
    val role: UserRole,
    val createdAt: Instant,
    val updatedAt: Instant,
    val emailVerifiedAt: Instant?,
    val deletedAt: Instant?,
)