package tmenier.fr.deployments

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tmenier.fr.common.config.SchedulerStrategy
import java.time.Instant

@QuarkusTest
@TestProfile(ApiOnlyProfile::class)
class ApiOnlyDeploymentTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var schedulerStrategy: SchedulerStrategy

    @Test
    fun `the API neither schedules nor runs Probe Checks nor claims a region`() {
        assertEquals(SchedulerStrategy.NONE, schedulerStrategy.value)
        val startedAt = Instant.now()
        val probeId = fixtures.createTcpProbe(ApiOnlyProfile.REGION, port = 1, intervalSeconds = 1)

        try {
            Thread.sleep(11_000)

            assertEquals(emptyList<QueuedCheck>(), fixtures.queuedChecks(probeId))
            assertEquals(emptyList<ProbeRun>(), fixtures.runs(probeId))
            assertEquals(0L, fixtures.heartbeatsInRegion(ApiOnlyProfile.REGION, since = startedAt))
        } finally {
            fixtures.disable(probeId)
        }
    }
}
