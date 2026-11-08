package tmenier.fr.common.utils

object CsvCell {
    private val FORMULA_TRIGGERS = setOf('=', '+', '-', '@', '\t', '\r')

    fun text(value: String?): String {
        if (value.isNullOrEmpty()) return ""

        val inert = if (value.first() in FORMULA_TRIGGERS) "'$value" else value
        if (inert.none { it == '"' || it == ',' || it == '\n' || it == '\r' }) return inert

        return "\"${inert.replace("\"", "\"\"")}\""
    }
}
