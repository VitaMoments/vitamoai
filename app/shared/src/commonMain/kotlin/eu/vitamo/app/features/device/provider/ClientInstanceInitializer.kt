package eu.vitamo.app.features.device.provider

import eu.vitamo.app.infrastructure.storage.ClientInstanceIdStorage

class ClientInstanceInitializer(
    private val clientInstanceIdStorage: ClientInstanceIdStorage,
) {

    suspend fun initialize() {
        clientInstanceIdStorage.getOrCreate()
    }
}