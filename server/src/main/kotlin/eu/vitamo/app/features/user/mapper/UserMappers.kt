package eu.vitamo.app.features.user.mapper

import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.contracts.user.PrivateUser
import eu.vitamo.app.api.contracts.user.PublicUser
import eu.vitamo.app.features.user.entity.UserEntity
import eu.vitamo.app.features.user.model.UserAccount

fun UserEntity.toAuthenticatedUser(): AuthenticatedUser = AuthenticatedUser(
    id = id.value,
    displayName = displayName,
    bio = bio,
    role = role,
    firstName = firstName,
    lastName = lastName,
    alias = alias,
    birthDate = birthDate,
    email = email,
)

fun UserAccount.toAuthenticatedUser(): AuthenticatedUser = AuthenticatedUser(
    id = id,
    displayName = displayName,
    bio = bio,
    role = role,
    firstName = firstName,
    lastName = lastName,
    alias = alias,
    birthDate = birthDate,
    email = email,
)

fun UserEntity.toPrivateUser(): PrivateUser = PrivateUser(
    id = id.value,
    displayName = displayName,
    bio = bio,
    role = role,
    firstName = firstName,
    lastName = lastName,
    alias = alias,
    birthDate = birthDate,
)

fun UserEntity.toPublicUser(): PublicUser = PublicUser(
    id = id.value,
    displayName = displayName,
    bio = bio,
    role = role,
)
