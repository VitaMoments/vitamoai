package eu.vitamo.app.features.user.table

import eu.vitamo.app.api.contracts.user.UserRole
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.timestamp

object UsersTable : UuidTable(name = "users") {
    val email = varchar(name = "email", length = 255).uniqueIndex("users_email_uidx")
    val displayName = varchar(name = "display_name", length = 100).index("users_display_name_idx")
    val hashedPassword = varchar(name = "hashed_password", length = 255)
    val firstName = varchar(name = "first_name", length = 100).nullable()
    val lastName = varchar(name = "last_name", length = 100).nullable()
    val alias = varchar(name = "alias", length = 100).nullable()
    val bio = varchar(name = "bio", length = 500).nullable()
    val birthDate = date(name = "birth_date").nullable()
    val role = enumerationByName(name = "role", length = 50, klass = UserRole::class)
    val createdAt = long(name = "created_at")
    val updatedAt = long(name = "updated_at")
    val emailVerifiedAt = timestamp(name = "email_verified_at").nullable()
    val deletedAt = long(name = "deleted_at").nullable()
}
