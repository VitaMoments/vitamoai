package eu.vitamo.app.validation

object PasswordValidator {
    fun isValid(password: String): Boolean {
        return getValidationErrors(password).isEmpty()
    }

    fun getValidationErrors(password: String): List<String> {
        return buildList {
            if (password.length < 8) {
                add("Password must be at least 8 characters.")
            }

            if (password.any { it.isWhitespace() }) {
                add("Password must not contain whitespace.")
            }

            if (password.none { it.isDigit() }) {
                add("Password must contain at least one number.")
            }

            if (password.none { it.isLowerCase() }) {
                add("Password must contain at least one lowercase letter.")
            }

            if (password.none { it.isUpperCase() }) {
                add("Password must contain at least one uppercase letter.")
            }

            if (password.none { !it.isLetterOrDigit() }) {
                add("Password must contain at least one special character.")
            }
        }
    }

    fun validateOrThrow(
        password: String,
        throwable: (List<String>) -> Throwable = { errors ->
            IllegalArgumentException(errors.joinToString(separator = " "))
        }
    ) {
        val errors = getValidationErrors(password)

        if (errors.isNotEmpty()) {
            throw throwable(errors)
        }
    }
}