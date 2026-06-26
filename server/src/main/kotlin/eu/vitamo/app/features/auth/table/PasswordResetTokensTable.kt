package eu.vitamo.app.features.auth.table

import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.timestamp

object PasswordResetTokensTable: UuidTable(name = "password_reset_tokens") {
    val user = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    )

    val tokenHash = varchar("token_hash", 255).uniqueIndex()
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
    val consumedAt = timestamp("consumed_at").nullable()
    val attempts = integer("attempts").default(3)
    val lastAttemptAt = timestamp("last_attempt_at").nullable()

    init {
        index(
            customIndexName = "idx_password_reset_tokens_user_id",
            isUnique = false,
            columns = arrayOf(user),
        )

        index(
            customIndexName = "idx_password_reset_tokens_expires_at",
            isUnique = false,
            columns = arrayOf(expiresAt),
        )

        index(
            customIndexName = "idx_password_reset_tokens_consumed_at",
            isUnique = false,
            columns = arrayOf(consumedAt),
        )

        index(
            customIndexName = "idx_password_reset_tokens_lookup",
            isUnique = false,
            columns = arrayOf(tokenHash, consumedAt, expiresAt),
        )
    }
}