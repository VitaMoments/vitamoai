package eu.vitamo.app.api.contracts.user

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface User {
    @Contextual
    val id: Uuid
    val displayName: String
    val bio: String?
    val role: UserRole
}

@Serializable
@SerialName("authenticated")
data class AuthenticatedUser(
    @Contextual
    override val id: Uuid,
    override val displayName: String,
    override val bio: String?,
    override val role: UserRole,
    val firstName: String?,
    val lastName: String?,
    val alias: String?,
    val birthDate: LocalDate?,
    val email: String,
) : User

@Serializable
@SerialName("private")
data class PrivateUser(
    @Contextual
    override val id: Uuid,
    override val displayName: String,
    override val bio: String?,
    override val role: UserRole,
    val firstName: String?,
    val lastName: String?,
    val alias: String?,
    val birthDate: LocalDate?,
) : User

@Serializable
@SerialName("public")
data class PublicUser(
    @Contextual
    override val id: Uuid,
    override val displayName: String,
    override val bio: String?,
    override val role: UserRole,
) : User

