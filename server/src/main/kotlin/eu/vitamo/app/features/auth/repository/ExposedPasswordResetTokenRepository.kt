package eu.vitamo.app.features.auth.repository

import eu.vitamo.app.database.helpers.dbQuery
import eu.vitamo.app.features.auth.entity.EmailVerificationChallengeEntity
import eu.vitamo.app.features.auth.entity.PasswordResetTokenEntity
import eu.vitamo.app.features.auth.table.PasswordResetTokensTable
import eu.vitamo.app.features.user.entity.UserEntity
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExposedPasswordResetTokenRepository : PasswordResetTokenRepository {

    override suspend fun create(
        user: UserEntity,
        tokenHash: String,
        createdAt: Instant,
        expiresAt: Instant,
    ): PasswordResetTokenEntity = dbQuery {
        PasswordResetTokenEntity.new {
            this.user = user
            this.tokenHash = tokenHash
            this.createdAt = createdAt
            this.expiresAt = expiresAt
            this.consumedAt = null
            this.attempts = 0
            this.lastAttemptAt = null
        }
    }

    override suspend fun findByTokenHash(
        tokenHash: String,
    ): PasswordResetTokenEntity? = dbQuery {
        PasswordResetTokenEntity
            .find {
                PasswordResetTokensTable.tokenHash eq tokenHash
            }
            .limit(1)
            .singleOrNull()
    }

    override suspend fun findLatestByUserId(
        userId: Uuid,
    ): PasswordResetTokenEntity? = dbQuery {
        PasswordResetTokenEntity
            .find {
                PasswordResetTokensTable.user eq userId
            }
            .orderBy(PasswordResetTokensTable.createdAt to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
    }

    override suspend fun consumeIfActive(
        tokenId: Uuid,
        consumedAt: Instant,
    ): Boolean = dbQuery {
        val token = PasswordResetTokenEntity.findById(tokenId)
            ?: return@dbQuery false

        if (token.consumedAt != null) {
            return@dbQuery false
        }

        token.consumedAt = consumedAt
        true
    }

    override fun markConsumed(id: Uuid, consumedAt: Instant) {
        transaction {
            EmailVerificationChallengeEntity.findById(id)?.let {
                it.consumedAt = consumedAt
                it.lastAttemptAt = consumedAt
            }
        }
    }

    override suspend fun consumeActiveForUser(
        userId: Uuid,
        consumedAt: Instant,
    ): Unit = dbQuery {
        PasswordResetTokenEntity
            .find {
                (PasswordResetTokensTable.user eq userId) and
                        PasswordResetTokensTable.consumedAt.isNull()
            }
            .forEach { token ->
                token.consumedAt = consumedAt
            }
    }

    override suspend fun incrementAttempts(
        tokenId: Uuid,
        attemptedAt: Instant,
    ): Unit = dbQuery {
        val token = PasswordResetTokenEntity.findById(tokenId)
            ?: return@dbQuery

        token.attempts += 1
        token.lastAttemptAt = attemptedAt
    }
}