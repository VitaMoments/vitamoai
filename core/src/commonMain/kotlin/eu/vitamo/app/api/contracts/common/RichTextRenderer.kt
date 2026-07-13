package eu.vitamo.app.api.contracts.common

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive


object RichTextRenderer {

    fun toPlainText(doc: RichTextDocument): String =
        docToPlainText(doc)

    fun toSafeHtml(doc: RichTextDocument): String =
        docToSafeHtml(doc)

    private fun docToPlainText(doc: RichTextDocument): String {
        val blocks = doc.content?.jsonArrayOrNull() ?: return ""
        val sb = StringBuilder()

        blocks.forEachIndexed { bi, blockEl ->
            val block = blockEl.jsonObjectOrNull() ?: return@forEachIndexed
            when (block["type"]?.jsonPrimitive?.contentOrNull) {
                "paragraph", "heading" -> {
                    val inlines = block["content"]?.jsonArrayOrNull()
                    inlines?.forEach { inlineEl ->
                        val inline = inlineEl.jsonObjectOrNull() ?: return@forEach
                        when (inline["type"]?.jsonPrimitive?.contentOrNull) {
                            "text" -> sb.append(inline["text"]?.jsonPrimitive?.contentOrNull.orEmpty())
                            "hardBreak" -> sb.append('\n')
                        }
                    }
                    if (bi != blocks.size - 1) sb.append('\n')
                }
            }
        }
        return sb.toString().trimEnd()
    }

    private fun docToSafeHtml(doc: RichTextDocument): String {
        val blocks = doc.content?.jsonArrayOrNull() ?: return ""
        val sb = StringBuilder()

        blocks.forEach { blockEl ->
            val block = blockEl.jsonObjectOrNull() ?: return@forEach
            when (block["type"]?.jsonPrimitive?.contentOrNull) {
                "paragraph" -> renderParagraph(block, sb)
                "heading" -> renderHeading(block, sb)
            }
        }
        return sb.toString()
    }

    private fun renderParagraph(block: JsonObject, sb: StringBuilder) {
        sb.append("<p>")
        val inlines = block["content"]?.jsonArrayOrNull()
        if (inlines.isNullOrEmpty()) sb.append("<br/>") else renderInlines(inlines, sb)
        sb.append("</p>")
    }

    private fun renderHeading(block: JsonObject, sb: StringBuilder) {
        val level = block["attrs"]?.jsonObjectOrNull()
            ?.get("level")?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?.coerceIn(1, 6) ?: 2

        sb.append("<h").append(level).append(">")
        val inlines = block["content"]?.jsonArrayOrNull()
        if (!inlines.isNullOrEmpty()) renderInlines(inlines, sb)
        sb.append("</h").append(level).append(">")
    }

    private fun renderInlines(inlines: JsonArray, sb: StringBuilder) {
        inlines.forEach { inlineEl ->
            val inline = inlineEl.jsonObjectOrNull() ?: return@forEach
            when (inline["type"]?.jsonPrimitive?.contentOrNull) {
                "hardBreak" -> sb.append("<br/>")
                "text" -> {
                    val raw = inline["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val text = escapeHtml(raw)

                    val marks = inline["marks"]?.jsonArrayOrNull()
                        ?.mapNotNull { it.jsonObjectOrNull()?.get("type")?.jsonPrimitive?.contentOrNull }
                        ?.toSet()
                        ?: emptySet()

                    if ("bold" in marks) sb.append("<strong>")
                    if ("italic" in marks) sb.append("<em>")
                    if ("underline" in marks) sb.append("<u>")

                    sb.append(text)

                    if ("underline" in marks) sb.append("</u>")
                    if ("italic" in marks) sb.append("</em>")
                    if ("bold" in marks) sb.append("</strong>")
                }
            }
        }
    }

    private fun JsonElement.jsonObjectOrNull(): JsonObject? =
        this as? JsonObject

    private fun JsonElement.jsonArrayOrNull(): JsonArray? =
        this as? JsonArray

    private fun escapeHtml(s: String): String =
        buildString(s.length) {
            for (ch in s) {
                append(
                    when (ch) {
                        '<' -> "&lt;"
                        '>' -> "&gt;"
                        '&' -> "&amp;"
                        '"' -> "&quot;"
                        '\'' -> "&#39;"
                        else -> ch
                    }
                )
            }
        }
}
