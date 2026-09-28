package tmenier.fr.deployments

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.config.SchedulerStrategy

/** An installation upgraded with QUARKUS_SCHEDULER_STRATEGY=db-lock keeps checking its Probes. */
@QuarkusTest
@TestProfile(LegacyDbLockProfile::class)
class LegacyDbLockDeploymentTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var schedulerStrategy: SchedulerStrategy

    @ConfigProperty(name = "quarkus.http.test-port", defaultValue = "8081")
    lateinit var httpPort: String

    @Test
    fun `db-lock is mapped to the queue engine`() {
        assertEquals(SchedulerStrategy.DATABASE, schedulerStrategy.value)
        val probeId = fixtures.createTcpProbe(LegacyDbLockProfile.REGION, httpPort.toInt(), intervalSeconds = 2)

        try {
            val deadline = System.nanoTime() + 20_000_000_000L
            var runs = fixtures.runs(probeId)
            while (runs.isEmpty() && System.nanoTime() < deadline) {
                Thread.sleep(250)
                runs = fixtures.runs(probeId)
            }
            assertTrue(runs.isNotEmpty(), "the Probe was never checked")
            assertTrue(runs.all { it.jobId != null }, "checks go through probe_check_jobs: $runs")
        } finally {
            fixtures.disable(probeId)
        }
    }
}
