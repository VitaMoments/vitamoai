package eu.vitamo.app.mail.template

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templatemode.TemplateMode
import java.nio.charset.StandardCharsets
import java.util.Properties

class ThymeleafMailTemplateRenderer(
    private val cacheEnabled: Boolean = mailTemplateCacheEnabled(),
) : MailTemplateRenderer {
    private val htmlEngine = templateEngine(TemplateMode.HTML, ".html")
    private val textEngine = templateEngine(TemplateMode.TEXT, ".txt")

    override fun renderHtml(templateName: String, variables: Map<String, Any?>): String {
        return htmlEngine.process(templateName, context(variables))
    }

    override fun renderText(templateName: String, variables: Map<String, Any?>): String {
        return textEngine.process(templateName, context(variables))
    }

    private fun templateEngine(templateMode: TemplateMode, suffix: String): TemplateEngine {
        val resolver = ClassLoaderTemplateResolver().apply {
            prefix = "mail/"
            this.suffix = suffix
            characterEncoding = StandardCharsets.UTF_8.name()
            this.templateMode = templateMode
            setCacheable(cacheEnabled)
        }

        return TemplateEngine().apply {
            setTemplateResolver(resolver)
        }
    }

    private fun context(variables: Map<String, Any?>): Context {
        return Context().apply {
            setVariables(variables.filterValues { it != null }.mapValues { it.value!! })
        }
    }
}

internal fun mailTemplateCacheEnabled(
    environment: Map<String, String> = System.getenv(),
    systemProperties: Properties = System.getProperties(),
): Boolean {
    return readBooleanSetting("MAIL_TEMPLATE_CACHE_ENABLED", environment, systemProperties, true)
}

private fun readBooleanSetting(
    key: String,
    environment: Map<String, String>,
    systemProperties: Properties,
    defaultValue: Boolean,
): Boolean {
    return (environment[key]
        ?: systemProperties.getProperty(key)
        ?: defaultValue.toString()
        ).trim().ifBlank { defaultValue.toString() }.toBooleanStrictOrNull()
        ?: error("Invalid value for $key")
}
