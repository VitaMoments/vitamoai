package eu.vitamo.app.features.feed.api

import eu.vitamo.app.network.DevNetworkConfig

data class FeedApiConfig(
    val baseUrl: String = "${DevNetworkConfig.API_BASE_URL}/v1/feed",
)
