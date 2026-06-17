package eu.vitamo.app.config

data class SmtpConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val fromAddress: String,
    val fromName: String? = "VitaMo",
    val sslEnabled: Boolean,
    val startTlsEnabled: Boolean,
    val authEnabled: Boolean,
)
