package eu.vitamo.app.features.auth.persistence.entity

fun RefreshTokenEntity.isValid(
    nowEpochSeconds: Long,
): Boolean {
    return revokedAt == null &&
            deletedAt == null &&
            expiredAt > nowEpochSeconds
}

fun RefreshTokenEntity.touch(
    nowEpochSeconds: Long,
) {
    lastUsedAt = nowEpochSeconds
    updatedAt = nowEpochSeconds
}

fun RefreshTokenEntity.revoke(
    nowEpochSeconds: Long,
) {
    revokedAt = nowEpochSeconds
    updatedAt = nowEpochSeconds
}

fun RefreshTokenEntity.markCreated(
    nowEpochSeconds: Long,
) {
    createdAt = nowEpochSeconds
    updatedAt = nowEpochSeconds
}

fun RefreshTokenEntity.markExpires(
    expiresAtEpochSeconds: Long,
) {
    expiredAt = expiresAtEpochSeconds
}