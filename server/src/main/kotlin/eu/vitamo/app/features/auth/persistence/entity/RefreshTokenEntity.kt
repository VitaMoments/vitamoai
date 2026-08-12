package eu.vitamo.app.features.auth.persistence.entity

import eu.vitamo.app.features.auth.persistence.table.RefreshTokensTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class RefreshTokenEntity(
    id: EntityID<Long>,
) : LongEntity(id) {

    companion object :
        LongEntityClass<RefreshTokenEntity>(
            table = RefreshTokensTable,
        )

    var tokenHash by RefreshTokensTable.tokenHash

    var userId by RefreshTokensTable.userId
    var deviceId by RefreshTokensTable.deviceId

    var expiredAt by RefreshTokensTable.expiredAt
    var lastUsedAt by RefreshTokensTable.lastUsedAt
    var revokedAt by RefreshTokensTable.revokedAt

    var replacedBySessionId by RefreshTokensTable.replacedBySessionId

    var createdAt by RefreshTokensTable.createdAt
    var updatedAt by RefreshTokensTable.updatedAt
    var deletedAt by RefreshTokensTable.deletedAt
}