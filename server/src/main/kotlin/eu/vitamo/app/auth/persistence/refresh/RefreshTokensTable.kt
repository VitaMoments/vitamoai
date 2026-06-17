package eu.vitamo.app.auth.persistence.refresh

import eu.vitamo.app.auth.ClientContext
import eu.vitamo.app.auth.ClientPlatform
import eu.vitamo.app.auth.ClientType
import eu.vitamo.app.auth.persistence.users.UsersTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import kotlin.uuid.Uuid

object RefreshTokensTable : LongIdTable(name = "refresh_tokens") {
    val tokenHash = varchar(name = "token_hash", length = 255).uniqueIndex("refresh_tokens_token_hash_uidx")
    val userId = reference(
        name = "user_id",
        foreign = UsersTable,
        onDelete = ReferenceOption.CASCADE,
    ).index("refresh_tokens_user_id_idx")
    val clientInstanceId = uuid(name = "client_instance_id").nullable()
    val clientType = varchar(name = "client_type", length = 20).nullable()
    val deviceName = varchar(name = "device_name", length = 255).nullable()
    val platform = varchar(name = "platform", length = 20).nullable()
    val browserName = varchar(name = "browser_name", length = 255).nullable()
    val appVersion = varchar(name = "app_version", length = 50).nullable()
    val expiredAt = long(name = "expired_at_epoch_seconds").index("refresh_tokens_expired_at_idx")
    val lastUsedAt = long(name = "last_used_at_epoch_seconds").nullable()
    val revokedAt = long(name = "revoked_at_epoch_seconds").nullable().index("refresh_tokens_revoked_at_idx")
    val replacedBySessionId = uuid(name = "replaced_by_session_id").nullable()
    val createdAt = long(name = "created_at_epoch_seconds")
    val updatedAt = long(name = "updated_at_epoch_seconds")
    val deletedAt = long(name = "deleted_at_epoch_seconds").nullable()
}

class RefreshTokenEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RefreshTokenEntity>(RefreshTokensTable)

    var tokenHash by RefreshTokensTable.tokenHash
    var userId by RefreshTokensTable.userId
    var clientInstanceId by RefreshTokensTable.clientInstanceId
    var clientType by RefreshTokensTable.clientType
    var deviceName by RefreshTokensTable.deviceName
    var platform by RefreshTokensTable.platform
    var browserName by RefreshTokensTable.browserName
    var appVersion by RefreshTokensTable.appVersion
    var expiredAt by RefreshTokensTable.expiredAt
    var lastUsedAt by RefreshTokensTable.lastUsedAt
    var revokedAt by RefreshTokensTable.revokedAt
    var replacedBySessionId by RefreshTokensTable.replacedBySessionId
    var createdAt by RefreshTokensTable.createdAt
    var updatedAt by RefreshTokensTable.updatedAt
    var deletedAt by RefreshTokensTable.deletedAt
}

fun RefreshTokenEntity.toClientContextOrNull(): ClientContext? {
    val instanceId = clientInstanceId?.let { Uuid.parse(it.toString()) }
    val parsedClientType = clientType?.let(::parseClientType)
    val parsedPlatform = platform?.let(::parseClientPlatform)
    if (instanceId == null || parsedClientType == null || parsedPlatform == null) {
        return null
    }
    return ClientContext(
        clientInstanceId = instanceId,
        clientType = parsedClientType,
        deviceName = deviceName,
        platform = parsedPlatform,
        browserName = browserName,
        appVersion = appVersion,
    )
}

private fun parseClientType(raw: String): ClientType? {
    return ClientType.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
}

private fun parseClientPlatform(raw: String): ClientPlatform? {
    return ClientPlatform.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }
}

fun RefreshTokenEntity.isValid(nowEpochSeconds: Long): Boolean {
    return revokedAt == null && expiredAt > nowEpochSeconds
}

fun RefreshTokenEntity.touch(nowEpochSeconds: Long) {
    lastUsedAt = nowEpochSeconds
    updatedAt = nowEpochSeconds
}

fun RefreshTokenEntity.revoke(nowEpochSeconds: Long) {
    revokedAt = nowEpochSeconds
    updatedAt = nowEpochSeconds
}

fun RefreshTokenEntity.applyClientContext(context: ClientContext?) {
    if (context == null) {
        clientInstanceId = null
        clientType = null
        deviceName = null
        platform = null
        browserName = null
        appVersion = null
        return
    }
    clientInstanceId = context.clientInstanceId
    clientType = context.clientType.name
    deviceName = context.deviceName
    platform = context.platform.name
    browserName = context.browserName
    appVersion = context.appVersion
}

fun RefreshTokenEntity.markCreated(nowEpochSeconds: Long) {
    createdAt = nowEpochSeconds
    updatedAt = nowEpochSeconds
}

fun RefreshTokenEntity.markExpires(expiresAtEpochSeconds: Long) {
    expiredAt = expiresAtEpochSeconds
}
