package eu.vitamo.app.mail.template

import kotlin.test.Test
import kotlin.test.assertContains

class ThymeleafMailTemplateRendererTest {
    private val renderer = ThymeleafMailTemplateRenderer(cacheEnabled = false)

    @Test
    fun renderHtml_usesTemplateVariables() {
        val result = renderer.renderHtml(
            templateName = "email-verification",
            variables = mapOf(
                "displayName" to "Ava",
                "code" to "123456",
                "expiresInMinutes" to 15,
                "appName" to "VitaMo",
            ),
        )

        assertContains(result, "Ava")
        assertContains(result, "123456")
        assertContains(result, "15")
        assertContains(result, "VitaMo")
    }

    @Test
    fun renderText_usesTemplateVariables() {
        val result = renderer.renderText(
            templateName = "email-verification",
            variables = mapOf(
                "displayName" to "Ava",
                "code" to "123456",
                "expiresInMinutes" to 15,
                "appName" to "VitaMo",
            ),
        )

        assertContains(result, "Ava")
        assertContains(result, "123456")
        assertContains(result, "15")
        assertContains(result, "VitaMo")
    }
}
