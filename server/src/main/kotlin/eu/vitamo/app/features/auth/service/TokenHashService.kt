package eu.vitamo.app.features.auth.service

import java.security.MessageDigest

interface TokenHashService {
    fun hash(rawToken: String): String
    fun matches(rawToken: String, hashedToken: String): Boolean
}

class Sha256TokenHashService : TokenHashService {
    override fun hash(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(rawToken.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }

    override fun matches(rawToken: String, hashedToken: String): Boolean {
        val rawHash = hash(rawToken)

        return MessageDigest.isEqual(
            rawHash.toByteArray(Charsets.UTF_8),
            hashedToken.toByteArray(Charsets.UTF_8)
        )
    }
}