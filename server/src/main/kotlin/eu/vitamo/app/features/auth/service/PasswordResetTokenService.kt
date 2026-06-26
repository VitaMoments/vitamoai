package eu.vitamo.app.features.auth.service

import java.security.SecureRandom

open class PasswordResetTokenService(
    private val tokenHashService: TokenHashService,
    private val secureRandom: SecureRandom = SecureRandom()
)  {
    fun generateToken(): String =
        secureRandom.nextInt(1_000_000)
            .toString()
            .padStart(10, '0')

    fun hashToken(token: String): String =
        tokenHashService.hash(token)

    fun generateHashedToken(): Pair<String, String> {
        val token = generateToken()
        val hashedToken = hashToken(token)
        return Pair(token, hashedToken)
    }
}