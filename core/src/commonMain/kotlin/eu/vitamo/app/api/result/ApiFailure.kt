package eu.vitamo.app.api.result

sealed interface ApiFailure {

    data class Server(
        val apiError: ApiError,
    ) : ApiFailure

    data class Network(
        val message: String = "Geen internetverbinding.",
        val cause: Throwable? = null,
    ) : ApiFailure

    data class Serialization(
        val message: String = "De server gaf een onverwacht antwoord terug.",
        val cause: Throwable? = null,
    ) : ApiFailure

    data class Unknown(
        val message: String = "Er ging iets mis.",
        val cause: Throwable? = null,
    ) : ApiFailure
}