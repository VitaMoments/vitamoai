package eu.vitamo.app.auth.persistence.users

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

object UsersTable : LongIdTable(name = "users") {
    val email = varchar(name = "email", length = 255).uniqueIndex("users_email_uidx")
    val username = varchar(name = "username", length = 100).uniqueIndex("users_username_uidx")
    val password = varchar(name = "password", length = 255).nullable()
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(UsersTable)

    var email by UsersTable.email
    var username by UsersTable.username
    var password by UsersTable.password
}
