package eu.vitamo.app.infrastructure.storage

import kotlin.uuid.Uuid

interface ClientInstanceIdStorage {
    suspend fun getOrCreate(): Uuid
}