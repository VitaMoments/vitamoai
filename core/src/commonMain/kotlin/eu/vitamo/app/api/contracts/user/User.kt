package eu.vitamo.app.api.contracts.user

import eu.vitamo.app.api.contracts.friendship.FriendshipContext
import eu.vitamo.app.api.contracts.friendship.FriendshipState
import eu.vitamo.app.api.contracts.media.MediaReference
import eu.vitamo.app.serialization.UuidSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface User {
    @Serializable(with = UuidSerializer::class)
    val id: Uuid
    val displayName: String
    val bio: String?
    val role: UserRole
    val profileImage: MediaReference?
}

@Serializable
@SerialName("authenticated")
data class AuthenticatedUser(
    @Serializable(with = UuidSerializer::class)
    override val id: Uuid,
    override val displayName: String,
    override val bio: String?,
    override val role: UserRole,
    override val profileImage: MediaReference?,
    val firstName: String?,
    val lastName: String?,
    val alias: String?,
    val birthDate: LocalDate?,
    val email: String,
) : User

@Serializable
@SerialName("public")
data class PublicUser(
    @Serializable(with = UuidSerializer::class)
    override val id: Uuid,
    override val displayName: String,
    override val bio: String?,
    override val role: UserRole,
    override val profileImage: MediaReference?,
) : User

@Serializable
@SerialName("friend")
data class FriendshipUser(
    val user: User,
    @Serializable(with = UuidSerializer::class)
    val friendshipId: Uuid,
    val state: FriendshipState,
)

@Serializable
data class UserWithContext(
    val user: User,
    val friendshipContext: FriendshipContext = FriendshipContext()
)

