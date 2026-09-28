package tmenier.fr.common.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SchedulerStrategyTest {
    @Test
    fun `nothing configured keeps the process idle`() {
        assertEquals(SchedulerStrategy.NONE, SchedulerStrategy.resolve(configured = null, legacy = null))
    }

    @Test
    fun `database runs the background jobs`() {
        assertEquals(SchedulerStrategy.DATABASE, SchedulerStrategy.resolve(configured = "database", legacy = null))
    }

    @Test
    fun `legacy db-lock is an alias of database so single-instance installs keep checking`() {
        assertEquals(SchedulerStrategy.DATABASE, SchedulerStrategy.resolve(configured = null, legacy = "db-lock"))
    }

    @Test
    fun `legacy none stays idle`() {
        assertEquals(SchedulerStrategy.NONE, SchedulerStrategy.resolve(configured = null, legacy = "none"))
    }

    @Test
    fun `an explicit strategy wins over the legacy property`() {
        assertEquals(SchedulerStrategy.NONE, SchedulerStrategy.resolve(configured = "none", legacy = "db-lock"))
    }

    @Test
    fun `blank explicit value falls back to the legacy property`() {
        assertEquals(SchedulerStrategy.DATABASE, SchedulerStrategy.resolve(configured = " ", legacy = "db-lock"))
    }

    @Test
    fun `an unknown strategy fails fast instead of silently disabling checks`() {
        assertThrows<IllegalArgumentException> { SchedulerStrategy.resolve(configured = "db-lock", legacy = null) }
    }
}
