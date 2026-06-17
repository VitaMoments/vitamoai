package eu.vitamo.app.infrastructure.security

interface PasswordHashService {
    fun hashPassword(rawPassword: String): String
    fun verifyPassword(rawPassword: String, passwordHash: String): Boolean
}

