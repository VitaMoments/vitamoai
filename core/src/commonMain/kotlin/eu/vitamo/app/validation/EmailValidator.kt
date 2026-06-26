package eu.vitamo.app.validation

private val EMAIL_REGEX = Regex(
    pattern = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
    option = RegexOption.IGNORE_CASE
)

object EmailValidator {

    fun normalize(email: String): String {
        return email.trim().lowercase()
    }

    fun isValid(email: String): Boolean {
        val normalized = normalize(email)
        return normalized.isNotBlank() && EMAIL_REGEX.matches(normalized)
    }

    fun normalizeOrNull(email: String): String? {
        val normalized = normalize(email)
        return normalized.takeIf { isValid(it) }
    }

    fun normalizeOrThrow(
        email: String,
        throwable: () -> Throwable = { IllegalArgumentException("Email is invalid.") }
    ): String {
        val normalized = normalize(email)

        if (normalized.isBlank() || !EMAIL_REGEX.matches(normalized)) {
            throw throwable()
        }
        return normalized
    }
}