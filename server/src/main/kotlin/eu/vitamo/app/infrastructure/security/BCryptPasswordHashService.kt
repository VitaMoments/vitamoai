package eu.vitamo.app.infrastructure.security

import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHashService(
    private val cost: Int = DEFAULT_COST,
) : PasswordHashService {
    override fun hashPassword(rawPassword: String): String {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(cost))
    }

    override fun verifyPassword(rawPassword: String, passwordHash: String): Boolean {
        return BCrypt.checkpw(rawPassword, passwordHash)
    }

    companion object {
        private const val DEFAULT_COST = 12
    }
}

