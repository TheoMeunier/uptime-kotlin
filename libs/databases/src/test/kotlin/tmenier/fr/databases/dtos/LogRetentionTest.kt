package tmenier.fr.databases.dtos

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LogRetentionTest {
    @Test
    fun `a probe without override inherits the instance default`() {
        assertEquals(90, LogRetention.effective(probeDays = null, defaultDays = 90))
    }

    @Test
    fun `a probe override wins over the instance default`() {
        assertEquals(30, LogRetention.effective(probeDays = 30, defaultDays = 365))
        assertEquals(365, LogRetention.effective(probeDays = 365, defaultDays = 30))
    }

    @Test
    fun `keep forever on a probe wins over a finite default`() {
        assertNull(LogRetention.effective(probeDays = LogRetention.KEEP_FOREVER, defaultDays = 30))
    }

    @Test
    fun `nothing is purged when neither the probe nor the instance set a retention`() {
        assertNull(LogRetention.effective(probeDays = null, defaultDays = null))
    }

    @Test
    fun `the instance default is empty or between 30 and 3650 days`() {
        assertTrue(LogRetention.isValidDefault(null))
        assertTrue(LogRetention.isValidDefault(30))
        assertTrue(LogRetention.isValidDefault(3650))
        assertFalse(LogRetention.isValidDefault(29))
        assertFalse(LogRetention.isValidDefault(0))
        assertFalse(LogRetention.isValidDefault(3651))
    }

    @Test
    fun `a probe override also accepts 0 for keep forever`() {
        assertTrue(LogRetention.isValidOverride(null))
        assertTrue(LogRetention.isValidOverride(0))
        assertTrue(LogRetention.isValidOverride(30))
        assertFalse(LogRetention.isValidOverride(29))
        assertFalse(LogRetention.isValidOverride(-1))
        assertFalse(LogRetention.isValidOverride(3651))
    }
}
