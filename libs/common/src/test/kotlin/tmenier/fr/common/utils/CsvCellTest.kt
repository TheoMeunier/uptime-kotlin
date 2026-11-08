package tmenier.fr.common.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CsvCellTest {
    @Test
    fun `plain text is written as is`() {
        assertEquals("connection refused", CsvCell.text("connection refused"))
    }

    @Test
    fun `null and empty values give an empty cell`() {
        assertEquals("", CsvCell.text(null))
        assertEquals("", CsvCell.text(""))
    }

    @Test
    fun `text that could start a formula is prefixed with a quote`() {
        listOf("=1+1", "+33 6", "-2", "@SUM(A1)", "\tcmd", "\rcmd").forEach { value ->
            assertEquals("'$value", CsvCell.text(value).removeSurrounding("\""), value)
        }
    }

    @Test
    fun `commas, quotes and line breaks are quoted`() {
        assertEquals("\"HTTP 503, \"\"Service Unavailable\"\"\"", CsvCell.text("HTTP 503, \"Service Unavailable\""))
        assertEquals("\"line 1\nline 2\"", CsvCell.text("line 1\nline 2"))
    }

    @Test
    fun `a formula with a comma is both neutralised and quoted`() {
        assertEquals("\"'=HYPERLINK(\"\"http://evil.example\"\",\"\"click\"\")\"", CsvCell.text("=HYPERLINK(\"http://evil.example\",\"click\")"))
    }
}
