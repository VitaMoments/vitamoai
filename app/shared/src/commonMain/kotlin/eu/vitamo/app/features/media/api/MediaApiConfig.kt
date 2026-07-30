package eu.vitamo.app.features.media.api

import eu.vitamo.app.network.DevNetworkConfig

data class MediaApiConfig(
    val baseUrl: String =
        DevNetworkConfig.API_BASE_URL + "/media",
)