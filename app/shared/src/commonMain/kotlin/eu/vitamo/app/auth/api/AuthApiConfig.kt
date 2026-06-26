package eu.vitamo.app.auth.api

import eu.vitamo.app.network.DevNetworkConfig

data class AuthApiConfig(
    val baseUrl: String = DevNetworkConfig.API_BASE_URL + "/auth",
)

