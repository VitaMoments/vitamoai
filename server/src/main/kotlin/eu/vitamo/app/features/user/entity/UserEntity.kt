package eu.vitamo.app.features.user.entity

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UuidEntity
import org.jetbrains.exposed.v1.dao.UuidEntityClass
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UserEntity(id: EntityID<Uuid>) : UuidEntity(id) {
    companion object : UuidEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var displayName by UsersTable.displayName
    var hashedPassword by UsersTable.hashedPassword
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var alias by UsersTable.alias
    var bio by UsersTable.bio
    var birthDate by UsersTable.birthDate
    var role by UsersTable.role
    var createdAt by UsersTable.createdAt
    var updatedAt by UsersTable.updatedAt
    var emailVerifiedAt by UsersTable.emailVerifiedAt
    var deletedAt by UsersTable.deletedAt
}
