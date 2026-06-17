package eu.vitamo.app.mail.model

data class MailMessage(
    val to: String,
    val subject: String,
    val textBody: String? = null,
    val htmlBody: String? = null,
)
