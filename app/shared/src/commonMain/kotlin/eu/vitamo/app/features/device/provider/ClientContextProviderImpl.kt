package eu.vitamo.app.features.device.provider

import eu.vitamo.app.api.contracts.device.ClientContext
import eu.vitamo.app.infrastructure.storage.ClientInstanceIdStorage

class ClientContextProviderImpl(
    private val clientInstanceIdStorage: ClientInstanceIdStorage,
    private val platformClientInfoProvider: PlatformClientInfoProvider,
) : ClientContextProvider {

    override suspend fun get(): ClientContext {
        val info = platformClientInfoProvider.get()

        return ClientContext(
            clientInstanceId =
                clientInstanceIdStorage.getOrCreate(),

            clientType = info.clientType,
            clientName = info.clientName,
            clientVersion = info.clientVersion,

            platform = info.platform,
            osVersion = info.osVersion,

            deviceName = info.deviceName,
            deviceModel = info.deviceModel,
        )
    }
}