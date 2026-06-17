package eu.vitamo.app.features.auth.repository

import eu.vitamo.app.features.auth.entity.EmailVerificationChallengeEntity
import eu.vitamo.app.features.auth.entity.toModel
import eu.vitamo.app.features.auth.model.EmailVerificationChallenge
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import eu.vitamo.app.features.auth.table.EmailVerificationChallengesTable
import eu.vitamo.app.features.user.table.UsersTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExposedEmailVerificationChallengeRepository : EmailVerificationChallengeRepository {
    override fun create(
        userId: Uuid,
        email: String,
        codeHash: String,
        purpose: EmailVerificationPurpose,
        createdAt: Instant,
        expiresAt: Instant,
    ): EmailVerificationChallenge = transaction {
        EmailVerificationChallengeEntity.new {
            this.userId = EntityID(userId, UsersTable)
            this.email = email
            this.codeHash = codeHash
            this.purpose = purpose.name
            this.createdAt = createdAt
            this.expiresAt = expiresAt
            this.consumedAt = null
            this.attempts = 0
            this.lastAttemptAt = null
        }.toModel()
    }

    override fun findLatestActive(
        email: String,
        purpose: EmailVerificationPurpose,
        now: Instant,
    ): EmailVerificationChallenge? = transaction {
        EmailVerificationChallengeEntity.find {
            (EmailVerificationChallengesTable.email eq email) and
                (EmailVerificationChallengesTable.purpose eq purpose.name)
        }
            .orderBy(EmailVerificationChallengesTable.createdAt to SortOrder.DESC)
            .firstOrNull { entity ->
                entity.consumedAt == null && entity.expiresAt > now
            }
            ?.toModel()
    }

    override fun deleteById(id: Uuid) {
        transaction {
            EmailVerificationChallengeEntity.findById(id)?.delete()
        }
    }

    override fun markConsumed(id: Uuid, consumedAt: Instant) {
        transaction {
            EmailVerificationChallengeEntity.findById(id)?.let {
                it.consumedAt = consumedAt
                it.lastAttemptAt = consumedAt
            }
        }
    }

    override fun incrementAttempts(id: Uuid, attemptedAt: Instant) {
        transaction {
            EmailVerificationChallengeEntity.findById(id)?.let {
                it.attempts = it.attempts + 1
                it.lastAttemptAt = attemptedAt
            }
        }
    }
}
