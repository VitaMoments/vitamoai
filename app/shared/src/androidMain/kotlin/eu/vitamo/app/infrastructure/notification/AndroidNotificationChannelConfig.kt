package eu.vitamo.app.infrastructure.notification

internal data class AndroidNotificationChannelConfig(
    val id: String,
    val name: String,
    val description: String,
)

internal fun AppNotificationChannel
        .toAndroidChannelConfig():
        AndroidNotificationChannelConfig {

    return when (this) {
        AppNotificationChannel.GENERAL ->
            AndroidNotificationChannelConfig(
                id = "general",
                name = "Algemeen",
                description =
                    "Algemene VitaMo meldingen",
            )

        AppNotificationChannel.SOCIAL ->
            AndroidNotificationChannelConfig(
                id = "social",
                name = "Sociaal",
                description =
                    "Vriendschappen en sociale meldingen",
            )
    }
}