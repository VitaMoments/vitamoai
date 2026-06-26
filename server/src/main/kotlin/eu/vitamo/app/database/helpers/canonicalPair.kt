package eu.vitamo.app.database.helpers

import java.util.UUID
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

fun canonicalPair(a: Uuid, b: Uuid): Pair<UUID, UUID> = canonicalPair(a.toJavaUuid(), b.toJavaUuid())

fun canonicalPair(a: UUID, b: UUID): Pair<UUID, UUID> {
    return if (a.toString() < b.toString()) a to b else b to a
}
