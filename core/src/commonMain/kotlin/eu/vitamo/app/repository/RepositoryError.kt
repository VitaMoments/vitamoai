package eu.vitamo.app.repository

import kotlinx.serialization.Serializable

@Serializable
sealed interface RepositoryError {
    val code: String?
    val message: String?

    data class Api(
        override val code: String,
        override val message: String,
        val status: Int? = null,
    ) : RepositoryError

    data class Network(
        override val message: String = "Geen internetverbinding.",
    ) : RepositoryError {
        override val code: String = "NETWORK_ERROR"
    }

    data class Unknown(
        override val message: String = "Er ging iets mis.",
    ) : RepositoryError {
        override val code: String = "UNKNOWN_ERROR"
    }
}