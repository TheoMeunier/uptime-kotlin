package tmenier.fr.deployments

import io.quarkus.arc.Arc
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tmenier.fr.common.config.SchedulerStrategy
import tmenier.fr.notifications.jobs.NotificationRetryJob
import tmenier.fr.schedulers.jobs.MaintenanceMaterialiserService
import tmenier.fr.schedulers.jobs.ProbeLogRetentionService
import tmenier.fr.schedulers.jobs.RefreshTokenPurgeService
import java.time.Duration
import java.time.Instant
import java.util.UUID

@QuarkusTest
@TestProfile(SingleInstanceProfile::class)
class SingleInstanceDeploymentTest {
    @Inject
    lateinit var fixtures: QueueFixtures

    @Inject
    lateinit var schedulerStrategy: SchedulerStrategy

    @ConfigProperty(name = "quarkus.http.test-port", defaultValue = "8081")
    lateinit var httpPort: String

    private val probes = mutableListOf<UUID>()

    @AfterEach
    fun disableProbes() = probes.forEach(fixtures::disable)

    @Test
    fun `the API runs each Probe Check once per interval through the queue`() {
        val probeId = fixtures.createTcpProbe(SingleInstanceProfile.REGION, httpPort.toInt(), intervalSeconds = 2).also(probes::add)

        val runs = awaitRuns(probeId, count = 3, within = Duration.ofSeconds(30))

        assertTrue(runs.all { it.jobId != null }, "every run comes from a probe_check_jobs row: $runs")
        assertEquals(runs.size, runs.mapNotNull { it.jobId }.toSet().size, "one run per job: $runs")
        runs.zipWithNext().forEach { (previous, next) ->
            val gap = Duration.between(previous.runAt, next.runAt)
            assertTrue(gap >= Duration.ofMillis(1_500), "two runs for the same slot ($gap apart): $runs")
        }
    }

    @Test
    fun `a failed attempt leaves its retry in the database rather than in memory`() {
        val probeId =
            fixtures
                .createTcpProbe(
                    SingleInstanceProfile.REGION,
                    port = 1,
                    intervalSeconds = 3_600,
                    retry = 2,
                    intervalRetrySeconds = 300,
                ).also(probes::add)

        awaitRuns(probeId, count = 1, within = Duration.ofSeconds(20))

        val retry = awaitRetry(probeId, within = Duration.ofSeconds(10))
        assertEquals("PENDING", retry.status)
        assertTrue(
            retry.availableAt.isAfter(Instant.now().plusSeconds(200)),
            "the retry waits interval_retry (300s): available at ${retry.availableAt}",
        )
    }

    @Test
    fun `the API advertises its region and hosts every background job`() {
        assertEquals(SchedulerStrategy.DATABASE, schedulerStrategy.value)
        val startedAt = Instant.now()

        val deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos()
        while (fixtures.heartbeatsInRegion(SingleInstanceProfile.REGION, since = startedAt) == 0L && System.nanoTime() < deadline) {
            Thread.sleep(500)
        }
        assertEquals(1L, fixtures.heartbeatsInRegion(SingleInstanceProfile.REGION, since = startedAt))

        listOf(
            MaintenanceMaterialiserService::class.java,
            RefreshTokenPurgeService::class.java,
            ProbeLogRetentionService::class.java,
            NotificationRetryJob::class.java,
        ).forEach { job ->
            assertNotNull(Arc.container().instance(job).get(), "${job.simpleName} must run in a single-instance deployment")
        }
    }

    @Test
    fun `checks left in the region of a process that is gone are taken over`() {
        val (probeId, jobId) =
            fixtures
                .createTcpProbeQueuedIn(jobRegion = "it-retired-worker", probeRegion = "it-retired-worker", port = httpPort.toInt())
                .also { probes.add(it.first) }

        val runs = awaitRuns(probeId, count = 1, within = Duration.ofSeconds(20))

        assertEquals(jobId, runs.first().jobId)
    }

    @Test
    fun `checks of a region served by another live process are left to it`() {
        val liveRegion = "it-live-worker"
        fixtures.beat(liveRegion)
        val (probeId, _) =
            fixtures
                .createTcpProbeQueuedIn(jobRegion = liveRegion, probeRegion = liveRegion, port = httpPort.toInt())
                .also { probes.add(it.first) }

        repeat(5) {
            Thread.sleep(1_000)
            fixtures.beat(liveRegion)
        }

        assertEquals(emptyList<ProbeRun>(), fixtures.runs(probeId))
        assertEquals(listOf("PENDING"), fixtures.queuedChecks(probeId).map { it.status })
    }

    private fun awaitRuns(
        probeId: UUID,
        count: Int,
        within: Duration,
    ): List<ProbeRun> {
        val deadline = System.nanoTime() + within.toNanos()
        var runs = fixtures.runs(probeId)
        while (runs.size < count && System.nanoTime() < deadline) {
            Thread.sleep(250)
            runs = fixtures.runs(probeId)
        }
        assertTrue(runs.size >= count, "expected $count run(s) within $within, got ${runs.size}")
        return runs
    }

    private fun awaitRetry(
        probeId: UUID,
        within: Duration,
    ): QueuedCheck {
        val deadline = System.nanoTime() + within.toNanos()
        while (System.nanoTime() < deadline) {
            fixtures.queuedChecks(probeId).firstOrNull { it.attempt == 2 }?.let { return it }
            Thread.sleep(250)
        }
        error("no retry job for probe $probeId: ${fixtures.queuedChecks(probeId)}")
    }
}
