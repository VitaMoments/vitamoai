package eu.vitamo.app.features.auth.api

import eu.vitamo.app.network.DevNetworkConfig

data class AuthApiConfig(
    val baseUrl: String = DevNetworkConfig.API_BASE_URL + "/auth",
)

