package tmenier.fr.notifications.templates

/** A rendered e-mail: the HTML part and its plain-text alternative carry the same content. */
internal data class EmailBody(
    val subject: String,
    val html: String,
    val text: String,
)

/**
 * Layout shared by every notification e-mail.
 *
 * Built for mail clients rather than browsers: nested tables, inline styles only, no web fonts,
 * no images (often blocked by default), 600px max width so it reads well on phones and in Outlook.
 */
internal object EmailTemplate {
    enum class Tone(
        val accent: String,
        val soft: String,
    ) {
        DOWN("#DC2626", "#FEF2F2"),
        DEGRADED("#D97706", "#FFFBEB"),
        STILL_DOWN("#EA580C", "#FFF7ED"),
        UP("#16A34A", "#F0FDF4"),
        TEST("#2563EB", "#EFF6FF"),
    }

    data class Row(
        val label: String,
        val value: String,
        val monospace: Boolean = false,
    )

    data class Content(
        val tone: Tone,
        val badge: String,
        val subject: String,
        val headline: String,
        val intro: String,
        val rows: List<Row> = emptyList(),
        val detailsLabel: String = "Details",
        val details: String? = null,
        val version: String? = null,
    )

    const val MAX_DETAILS_LENGTH = 4000

    private const val FONT = "-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif"
    private const val MONO = "ui-monospace,SFMono-Regular,Menlo,Consolas,'Liberation Mono',monospace"
    private const val FOOTER_NOTE =
        "You are receiving this e-mail because this address is a recipient of an Uptime Kotlin notification channel."

    fun render(content: Content): EmailBody {
        val details =
            content.details
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let(::truncate)
        return EmailBody(
            subject = content.subject,
            html = html(content, details),
            text = text(content, details),
        )
    }

    private fun html(
        c: Content,
        details: String?,
    ): String =
        buildString {
            append("<!DOCTYPE html>")
            append("<html lang=\"en\"><head>")
            append("<meta charset=\"utf-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            append("<meta name=\"color-scheme\" content=\"light\">")
            append("<meta name=\"supported-color-schemes\" content=\"light\">")
            append("<title>${esc(c.subject)}</title>")
            append("</head>")
            append("<body style=\"margin:0;padding:0;background-color:#F3F4F6;-webkit-text-size-adjust:100%;\">")

            append("<div style=\"display:none;max-height:0;overflow:hidden;opacity:0;mso-hide:all;\">")
            append(esc(c.intro))
            append("</div>")

            append(table("background-color:#F3F4F6;"))
            append("<tr><td align=\"center\" style=\"padding:32px 16px;\">")
            append(table("max-width:600px;"))

            append("<tr><td style=\"padding:0 4px 14px;font-family:$FONT;font-size:14px;font-weight:700;color:#111827;\">")
            append("<span style=\"color:${c.tone.accent};\">&#9679;</span>&nbsp; Uptime Kotlin")
            append("</td></tr>")

            append("<tr><td style=\"background-color:#FFFFFF;border:1px solid #E5E7EB;border-radius:10px;\">")
            append(table(""))
            append(
                "<tr><td style=\"height:4px;background-color:${c.tone.accent};border-radius:10px 10px 0 0;" +
                    "font-size:0;line-height:0;\">&nbsp;</td></tr>",
            )

            append("<tr><td style=\"padding:28px 32px 4px;font-family:$FONT;\">")
            append(
                "<span style=\"display:inline-block;padding:4px 10px;border-radius:999px;" +
                    "background-color:${c.tone.soft};color:${c.tone.accent};font-size:12px;font-weight:700;" +
                    "letter-spacing:0.05em;text-transform:uppercase;\">${esc(c.badge)}</span>",
            )
            append(
                "<h1 style=\"margin:16px 0 8px;font-size:22px;line-height:1.3;font-weight:700;color:#111827;\">" +
                    "${esc(c.headline)}</h1>",
            )
            append("<p style=\"margin:0;font-size:15px;line-height:1.6;color:#4B5563;\">${esc(c.intro)}</p>")
            append("</td></tr>")

            if (c.rows.isNotEmpty()) {
                append("<tr><td style=\"padding:20px 32px ${if (details == null) "28px" else "8px"};\">")
                append(table("border-collapse:collapse;"))
                c.rows.forEachIndexed { index, row ->
                    val border = if (index == 0) "" else "border-top:1px solid #F3F4F6;"
                    val valueFont = if (row.monospace) "font-family:$MONO;font-size:13px;" else "font-family:$FONT;font-size:14px;"
                    append("<tr>")
                    append(
                        "<td valign=\"top\" width=\"36%\" style=\"padding:10px 12px 10px 0;$border" +
                            "font-family:$FONT;font-size:13px;line-height:1.5;color:#6B7280;\">${esc(row.label)}</td>",
                    )
                    append(
                        "<td valign=\"top\" style=\"padding:10px 0;$border$valueFont line-height:1.5;color:#111827;" +
                            "font-weight:500;word-break:break-word;\">${esc(row.value)}</td>",
                    )
                    append("</tr>")
                }
                append("</table>")
                append("</td></tr>")
            }

            // Error / message block
            if (details != null) {
                append("<tr><td style=\"padding:12px 32px 28px;\">")
                append(
                    "<div style=\"margin:0 0 8px;font-family:$FONT;font-size:13px;color:#6B7280;\">" +
                        "${esc(c.detailsLabel)}</div>",
                )
                append(
                    "<div style=\"background-color:#F9FAFB;border:1px solid #E5E7EB;border-left:3px solid ${c.tone.accent};" +
                        "border-radius:6px;padding:12px 14px;font-family:$MONO;font-size:13px;line-height:1.55;" +
                        "color:#1F2937;white-space:pre-wrap;word-break:break-word;\">${esc(details)}</div>",
                )
                append("</td></tr>")
            }

            append("</table>")
            append("</td></tr>")

            // Footer
            append("<tr><td style=\"padding:18px 4px 0;font-family:$FONT;font-size:12px;line-height:1.6;color:#9CA3AF;\">")
            append(esc(FOOTER_NOTE))
            append("<br>")
            append(esc(signature(c.version)))
            append("</td></tr>")

            append("</table>")
            append("</td></tr>")
            append("</table>")
            append("</body></html>")
        }

    private fun text(
        c: Content,
        details: String?,
    ): String =
        buildString {
            appendLine("[${c.badge.uppercase()}] ${c.headline}")
            appendLine()
            appendLine(c.intro)
            if (c.rows.isNotEmpty()) {
                appendLine()
                val width = c.rows.maxOf { it.label.length } + 1
                c.rows.forEach { appendLine("${(it.label + ":").padEnd(width)}  ${it.value}") }
            }
            if (details != null) {
                appendLine()
                appendLine("${c.detailsLabel}:")
                appendLine(details)
            }
            appendLine()
            appendLine("--")
            appendLine(FOOTER_NOTE)
            append(signature(c.version))
        }

    private fun signature(version: String?): String = version?.takeIf { it.isNotBlank() }?.let { "Uptime Kotlin $it" } ?: "Uptime Kotlin"

    private fun truncate(text: String): String = if (text.length <= MAX_DETAILS_LENGTH) text else text.take(MAX_DETAILS_LENGTH) + "\n…"

    private fun table(style: String): String = "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"$style\">"

    fun esc(text: String): String =
        buildString(text.length) {
            text.forEach {
                when (it) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(it)
                }
            }
        }
}
