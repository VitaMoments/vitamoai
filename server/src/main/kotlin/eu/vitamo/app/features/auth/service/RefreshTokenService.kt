package eu.vitamo.app.features.auth.service

import eu.vitamo.app.auth.ClientContext
import eu.vitamo.app.features.auth.model.AuthToken
import eu.vitamo.app.features.auth.persistence.refresh.RefreshTokenEntity
import eu.vitamo.app.features.auth.persistence.refresh.RefreshTokensTable
import eu.vitamo.app.features.auth.persistence.refresh.applyClientContext
import eu.vitamo.app.features.auth.persistence.refresh.isValid
import eu.vitamo.app.features.auth.persistence.refresh.markCreated
import eu.vitamo.app.features.auth.persistence.refresh.markExpires
import eu.vitamo.app.features.auth.persistence.refresh.revoke
import eu.vitamo.app.features.auth.persistence.refresh.touch
import eu.vitamo.app.features.user.table.UsersTable
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.uuid.Uuid

open class RefreshTokenService(
    private val tokenHashService: TokenHashService,
) {
    fun create(
        authToken: AuthToken,
        userId: Uuid,
        context: ClientContext?,
    ): RefreshTokenEntity = transaction {
        val now = Clock.System.now().epochSeconds
        val expiresAt = authToken.expiresAt.epochSeconds
        RefreshTokenEntity.new {
            tokenHash = tokenHashService.hash(authToken.token)
            this.userId = EntityID(userId, UsersTable)
            applyClientContext(context)
            markExpires(expiresAt)
            markCreated(now)
            revokedAt = null
            lastUsedAt = null
            replacedBySessionId = null
            deletedAt = null
        }
    }

    fun findValid(refreshToken: String): RefreshTokenEntity? = transaction {
        val tokenHash = tokenHashService.hash(refreshToken)
        val now = Clock.System.now().epochSeconds

        RefreshTokenEntity.find {
            RefreshTokensTable.tokenHash eq tokenHash
        }
            .firstOrNull { entity ->
                entity.tokenHash == tokenHash && entity.isValid(now)
            }
    }

    fun markUsed(tokenId: EntityID<Long>) {
        transaction {
            RefreshTokenEntity.findById(tokenId)?.let {
                it.touch(Clock.System.now().epochSeconds)
            }
        }
    }

    fun revoke(tokenId: EntityID<Long>) {
        transaction {
            RefreshTokenEntity.findById(tokenId)?.let {
                it.revoke(Clock.System.now().epochSeconds)
            }
        }
    }
}
