package eu.vitamo.app.features.user.friendship.api

import eu.vitamo.app.network.DevNetworkConfig

data class FriendshipApiConfig(
    val baseUrl: String = DevNetworkConfig.API_BASE_URL + "/friendships", )