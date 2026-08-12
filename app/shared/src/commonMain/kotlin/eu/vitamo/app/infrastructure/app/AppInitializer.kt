package eu.vitamo.app.infrastructure.app

import eu.vitamo.app.features.device.provider.ClientInstanceInitializer

class AppInitializer(
    private val clientInstanceInitializer: ClientInstanceInitializer,
) {

    suspend fun initialize() {
        clientInstanceInitializer.initialize()
    }
}