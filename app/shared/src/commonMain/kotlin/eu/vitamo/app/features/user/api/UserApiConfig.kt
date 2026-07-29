package eu.vitamo.app.features.user.api

import eu.vitamo.app.network.DevNetworkConfig

data class UserApiConfig(
    val baseUrl: String = DevNetworkConfig.API_BASE_URL + "/users",
)