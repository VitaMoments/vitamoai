package eu.vitamo.app.mail.template

interface MailTemplateRenderer {
    fun renderHtml(templateName: String, variables: Map<String, Any?>): String

    fun renderText(templateName: String, variables: Map<String, Any?>): String
}
