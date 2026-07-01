package eu.vitamo.app.navigation.helper

import eu.vitamo.app.navigation.AuthDestination

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