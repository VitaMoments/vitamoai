package eu.vitamo.app.network.auth

interface AuthCookiePersistence {
    fun read(): String?

    fun write(serializedCookies: String)

    fun clear()
}

expect fun createAuthCookiePersistence(): AuthCookiePersistence
