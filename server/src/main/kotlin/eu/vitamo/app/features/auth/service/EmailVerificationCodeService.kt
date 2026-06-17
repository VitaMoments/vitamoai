package eu.vitamo.app.features.auth.service

import java.security.SecureRandom

class EmailVerificationCodeService(
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    fun generateCode(): String {
        return secureRandom.nextInt(1_000_000)
            .toString()
            .padStart(6, '0')
    }
}
