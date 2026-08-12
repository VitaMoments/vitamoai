package eu.vitamo.app.features.device.provider

import eu.vitamo.app.api.contracts.device.ClientContext

interface ClientContextProvider {
    suspend fun get(): ClientContext
}