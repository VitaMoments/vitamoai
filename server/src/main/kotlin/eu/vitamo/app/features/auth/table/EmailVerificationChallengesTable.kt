package eu.vitamo.app.features.auth.table

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object EmailVerificationChallengesTable : UuidTable(name = "email_verification_challenges") {
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    )
    val email = varchar(name = "email", length = 255)
    val codeHash = varchar(name = "code_hash", length = 255)
    val purpose = varchar(name = "purpose", length = 50)
    val createdAt = timestamp(name = "created_at")
    val expiresAt = timestamp(name = "expires_at")
    val consumedAt = timestamp(name = "consumed_at").nullable()
    val attempts = integer(name = "attempts").default(0)
    val lastAttemptAt = timestamp(name = "last_attempt_at").nullable()
}
