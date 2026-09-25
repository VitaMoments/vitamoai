package eu.vitamo.app.api.contracts.subscribtions

import kotlinx.serialization.Serializable

@Serializable
enum class SubscriptionPlan {
    BASIC,
    PLUS
}