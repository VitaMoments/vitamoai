package eu.vitamo.app.mail

import eu.vitamo.app.mail.model.MailMessage

interface MailService {
    suspend fun send(message: MailMessage)
}
