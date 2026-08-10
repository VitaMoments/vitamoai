package eu.vitamo.app.ui.theme

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromStored(
            value: String?,
        ): ThemeMode {
            return when (
                value
                    ?.trim()
                    ?.lowercase()
            ) {
                "light" -> LIGHT
                "dark" -> DARK
                "system",
                null,
                "",
                    -> SYSTEM

                else -> SYSTEM
            }
        }

        fun toStored(
            mode: ThemeMode,
        ): String {
            return when (mode) {
                LIGHT -> "light"
                DARK -> "dark"
                SYSTEM -> "system"
            }
        }
    }
}