package eu.vitamo.app.infrastructure.navigation.helper

import eu.vitamo.app.infrastructure.navigation.AuthDestination

fun parseAuthDeepLink(url: String?): AuthDestination? {
    if (url.isNullOrBlank()) return null
    if (!url.startsWith("https://vitamo.eu/auth/reset-password")) {
        return null
    }
    val token = url
        .substringAfter("token=", missingDelimiterValue = "")
        .substringBefore("&")
        .trim()
        .takeIf { it.isNotBlank() }
    return token?.let {
        AuthDestination.ResetPassword(token = it)
    }
}