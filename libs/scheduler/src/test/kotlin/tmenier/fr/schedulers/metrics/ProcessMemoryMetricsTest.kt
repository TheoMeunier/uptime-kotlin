package tmenier.fr.schedulers.metrics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProcessMemoryMetricsTest {
    @Test
    fun `reads the resident set size in bytes`() {
        val status =
            """
            Name:    application
            VmPeak:      412000 kB
            VmRSS:       51200 kB
            Threads:    42
            """.trimIndent()

        assertEquals(51_200 * 1024.0, ProcessMemoryMetrics.parseVmRss(status))
    }

    @Test
    fun `no resident set size without a VmRSS line`() {
        assertNull(ProcessMemoryMetrics.parseVmRss("Name:\tapplication\n"))
    }

    @Test
    fun `reads a cgroup memory limit`() {
        assertEquals(536_870_912.0, ProcessMemoryMetrics.parseCgroupLimit("536870912\n"))
    }

    @Test
    fun `an unlimited container has no limit`() {
        assertNull(ProcessMemoryMetrics.parseCgroupLimit("max\n"))
        assertNull(ProcessMemoryMetrics.parseCgroupLimit("9223372036854771712\n"))
    }
}
