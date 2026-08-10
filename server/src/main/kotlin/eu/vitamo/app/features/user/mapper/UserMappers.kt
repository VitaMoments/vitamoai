package eu.vitamo.app.features.user.mapper

import eu.vitamo.app.api.contracts.media.MediaReference
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.PublicUser
import eu.vitamo.app.api.contracts.user.User
import eu.vitamo.app.features.user.context.UserAccessLevel
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.model.UserCredentialsRecord
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun UserEntity.toRecord(): UserRecord =
    UserRecord(
        id = id.value,
        email = email,
        displayName = displayName,
        firstName = firstName,
        lastName = lastName,
        alias = alias,
        bio = bio,
        birthDate = birthDate,
        role = role,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        emailVerifiedAt = emailVerifiedAt,
        deletedAt = deletedAt?.let {
            Instant.fromEpochMilliseconds(it)
        },
        profileImageId = profileImageId,
    )

internal fun UserEntity.toCredentialsRecord(): UserCredentialsRecord =
    UserCredentialsRecord(
        user = toRecord(),
        passwordHash = hashedPassword,
    )

fun searchPredicate(
    meId: Uuid,
    needle: String?,
    includeSelf: Boolean = false,
    includeRemoved: Boolean = true,
): Op<Boolean> {
    val excludeMe = if (includeSelf) {
        Op.TRUE
    } else {
        UsersTable.id neq meId
    }

    val excludeRemoved = if (includeRemoved) {
        Op.TRUE
    } else {
        UsersTable.deletedAt.isNull()
    }

    val base = excludeMe and excludeRemoved

    val query = needle
        ?.trim()
        ?.takeIf(String::isNotBlank)
        ?.lowercase()
        ?: return base

    val pattern = "%$query%"

    val matches =
        (
                UsersTable.firstName.isNotNull() and
                        (UsersTable.firstName.lowerCase() like pattern)
                ) or
                (
                        UsersTable.lastName.isNotNull() and
                                (UsersTable.lastName.lowerCase() like pattern)
                        ) or
                (
                        UsersTable.alias.isNotNull() and
                                (UsersTable.alias.lowerCase() like pattern)
                        ) or
                (
                        UsersTable.displayName.isNotNull() and (UsersTable.displayName.lowerCase() like pattern)
                        )

    return base and matches
}

internal fun ResultRow.toUserRecord(): UserRecord =
    UserRecord(
        id = this[UsersTable.id].value,
        email = this[UsersTable.email],
        displayName = this[UsersTable.displayName],
        firstName = this[UsersTable.firstName],
        lastName = this[UsersTable.lastName],
        alias = this[UsersTable.alias],
        bio = this[UsersTable.bio],
        birthDate = this[UsersTable.birthDate],
        role = this[UsersTable.role],
        createdAt = Instant.fromEpochMilliseconds(
            this[UsersTable.createdAt],
        ),
        updatedAt = Instant.fromEpochMilliseconds(
            this[UsersTable.updatedAt],
        ),
        emailVerifiedAt = this[UsersTable.emailVerifiedAt],
        deletedAt = this[UsersTable.deletedAt]?.let {
            Instant.fromEpochMilliseconds(it)
        },
        profileImageId = this[UsersTable.profileImageId],
    )

fun UserRecord.toPublicUser(
    profileImage: MediaReference?,
): PublicUser =
    PublicUser(
        id = id,
        displayName = displayName,
        bio = bio,
        role = role,
        profileImage = profileImage,
    )

fun UserRecord.toAuthenticatedUser(
    profileImage: MediaReference?,
): AuthenticatedUser =
    AuthenticatedUser(
        id = id,
        displayName = displayName,
        bio = bio,
        role = role,
        firstName = firstName,
        lastName = lastName,
        alias = alias,
        birthDate = birthDate,
        email = email,
        profileImage = profileImage,
    )

fun UserRecord.toUser(
    accessLevel: UserAccessLevel,
    profileImage: MediaReference?,
): User =
    when (accessLevel) {
        UserAccessLevel.PUBLIC ->
            toPublicUser(
                profileImage = profileImage,
            )

        UserAccessLevel.SELF ->
            toAuthenticatedUser(
                profileImage = profileImage,
            )
    }